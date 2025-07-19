package esgi.easisell.service;

import esgi.easisell.dto.*;
import esgi.easisell.entity.*;
import esgi.easisell.event.SaleCompletedEvent;
import esgi.easisell.exception.*;
import esgi.easisell.mapper.*;
import esgi.easisell.model.PaymentResult;
import esgi.easisell.repository.*;
import esgi.easisell.service.interfaces.*;
import esgi.easisell.service.payment.PaymentProcessor;
import esgi.easisell.service.payment.PaymentProcessorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaleService implements ISaleCreationService, ISaleItemService, ISalePaymentService, ISaleQueryService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final ClientRepository clientRepository;

    // Services existants conservés
    private final ISaleValidationService saleValidationService;
    private final ISalePriceCalculator priceCalculator;
    private final ApplicationEventPublisher eventPublisher;

    // ========== CRÉATION DE VENTE ==========
    @Override
    @Transactional
    public SaleResponseDTO createNewSale(UUID clientId) {
        log.info(" Création d'une nouvelle vente pour le client: {}", clientId);

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));

        Sale sale = new Sale();
        sale.setClient(client);
        sale.setSaleTimestamp(Timestamp.valueOf(LocalDateTime.now()));
        sale.setTotalAmount(BigDecimal.ZERO);
        sale.setIsDeferred(false);
        sale.setSaleItems(new ArrayList<>());
        sale.setPayments(new ArrayList<>());

        Sale savedSale = saleRepository.save(sale);
        log.info(" Vente créée avec l'ID: {}", savedSale.getSaleId());

        return SaleMapper.toResponseDTO(savedSale);
    }

    // ========== GESTION DES ARTICLES AVEC VALIDATION DE STOCK ==========
    @Override
    @Transactional
    public SaleItemResponseDTO addProductToSale(UUID saleId, String barcode, BigDecimal quantity) {
        log.info(" Scan du produit {} (quantité: {}) pour la vente {}", barcode, quantity, saleId);

        Sale sale = findSaleOrThrow(saleId);
        saleValidationService.validateSaleNotFinalized(sale);

        Product product = productRepository.findByClientAndBarcode(
                sale.getClient().getUserId(), barcode);

        if (product == null) {
            throw new ProductNotFoundException("Code-barres: " + barcode);
        }

        return addProductInternal(sale, product, quantity);
    }

    @Override
    @Transactional
    public SaleItemResponseDTO addProductByIdToSale(UUID saleId, UUID productId, BigDecimal quantity) {
        log.info(" Ajout manuel du produit {} (quantité: {}) à la vente {}", productId, quantity, saleId);

        Sale sale = findSaleOrThrow(saleId);
        saleValidationService.validateSaleNotFinalized(sale);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("ID: " + productId));

        validateProductBelongsToClient(product, sale.getClient());

        return addProductInternal(sale, product, quantity);
    }

    @Override
    @Transactional
    public SaleItemResponseDTO updateItemQuantity(UUID saleItemId, BigDecimal newQuantity) {
        if (newQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidQuantityException("La quantité doit être supérieure à 0");
        }

        SaleItem saleItem = saleItemRepository.findById(saleItemId)
                .orElseThrow(() -> new SaleItemNotFoundException(saleItemId));

        saleValidationService.validateSaleNotFinalized(saleItem.getSale());

        //  VALIDATION AVEC LE SERVICE OPTIMISTE
        validateStockAvailability(saleItem.getProduct(), saleItem.getSale().getClient().getUserId(), newQuantity.intValue());

        saleItem.setQuantitySold(newQuantity);
        saleItem.setPriceAtSale(priceCalculator.calculateItemPrice(
                saleItem.getProduct(), newQuantity));

        saleItemRepository.save(saleItem);
        updateSaleTotal(saleItem.getSale());

        return SaleItemMapper.toResponseDTO(saleItem);
    }

    @Override
    @Transactional
    public void removeItemFromSale(UUID saleItemId) {
        SaleItem saleItem = saleItemRepository.findById(saleItemId)
                .orElseThrow(() -> new SaleItemNotFoundException(saleItemId));

        Sale sale = saleItem.getSale();
        saleValidationService.validateSaleNotFinalized(sale);

        sale.getSaleItems().remove(saleItem);
        saleItemRepository.delete(saleItem);

        updateSaleTotal(sale);
    }
    // ========== PAIEMENT AVEC GESTION OPTIMISTE CORRIGÉE ==========
    @Transactional
    public PaymentResultDTO processPaymentOptimistic(UUID saleId, String paymentType,
                                                     BigDecimal amountReceived, String currency) {
        log.info(" Traitement du paiement pour la vente: {} - Type: {}, Montant: {}",
                saleId, paymentType, amountReceived);

        Sale sale = findSaleOrThrow(saleId);
        saleValidationService.validateSaleNotFinalized(sale);

        if (sale.getSaleItems().isEmpty()) {
            throw new EmptySaleException("Impossible de finaliser une vente vide");
        }

        PaymentProcessor paymentProcessor = PaymentProcessorFactory.create(paymentType);
        PaymentResult result = paymentProcessor.processPayment(
                sale.getTotalAmount(), amountReceived, currency);

        if (!result.isSuccessful()) {
            throw new PaymentFailedException(result.getErrorMessage());
        }

        // Enregistrer le paiement
        Payment payment = Payment.builder()
                .sale(sale)
                .type(paymentType)
                .amount(result.getAmountPaid())
                .currency(currency)
                .paymentDate(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        paymentRepository.save(payment);
        sale.getPayments().add(payment);

        // ========== GESTION OPTIMISTE DU STOCK ==========
        try {
            for (SaleItem saleItem : sale.getSaleItems()) {
                Product product = saleItem.getProduct();
                int requestedQuantity = saleItem.getQuantitySold().intValue();

                // APPROCHE OPTIMISTE: on décrémente directement
                int newQuantity = product.getQuantity() - requestedQuantity;

                // Vérification métier (optionnelle selon vos règles)
                if (newQuantity < 0) {
                    throw new PaymentFailedException(
                            "Stock insuffisant pour " + product.getName() +
                                    " (stock actuel: " + product.getQuantity() +
                                    ", demandé: " + requestedQuantity + ")"
                    );
                }

                product.setQuantity(newQuantity);
                // Le save() déclenchera automatiquement la vérification de version
                productRepository.save(product);
            }

        } catch (OptimisticLockingFailureException e) {
            // Gestion du conflit optimiste
            log.warn("Conflit détecté lors de la mise à jour du stock pour la vente: {}", saleId);
            throw new PaymentFailedException(
                    "Le stock a été modifié par une autre transaction. Veuillez réessayer."
            );
        }

        eventPublisher.publishEvent(new SaleCompletedEvent(sale));

        log.info(" Paiement finalisé avec succès pour la vente: {}", saleId);
        return PaymentResultMapper.toDTO(result, sale);
    }

    // ========== VERSION AVANCÉE AVEC RETRY AUTOMATIQUE ==========
    @Retryable(
            value = {OptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Override
    @Transactional
    public PaymentResultDTO processPayment(UUID saleId, String paymentType,
                                           BigDecimal amountReceived, String currency) {
        log.info("💳 Traitement du paiement avec retry pour la vente: {}", saleId);

        return processPaymentOptimistic(saleId, paymentType, amountReceived, currency);
    }

    // ========== PAIEMENT AVEC GESTION OPTIMISTE ==========
    @Transactional
    public PaymentResultDTO processPayment2(UUID saleId, String paymentType,
                                           BigDecimal amountReceived, String currency) {
        log.info(" Traitement du paiement pour la vente: {} - Type: {}, Montant: {}",
                saleId, paymentType, amountReceived);

        Sale sale = findSaleOrThrow(saleId);
        saleValidationService.validateSaleNotFinalized(sale);

        if (sale.getSaleItems().isEmpty()) {
            throw new EmptySaleException("Impossible de finaliser une vente vide");
        }

        PaymentProcessor paymentProcessor = PaymentProcessorFactory.create(paymentType);
        PaymentResult result = paymentProcessor.processPayment(
                sale.getTotalAmount(), amountReceived, currency);

        if (!result.isSuccessful()) {
            throw new PaymentFailedException(result.getErrorMessage());
        }

        // Enregistrer le paiement
        Payment payment = Payment.builder()
                .sale(sale)
                .type(paymentType)
                .amount(result.getAmountPaid())
                .currency(currency)
                .paymentDate(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        paymentRepository.save(payment);
        sale.getPayments().add(payment);
        for (SaleItem saleItem : sale.getSaleItems()) {
            Product product = saleItem.getProduct();
            int requestedQuantity = saleItem.getQuantitySold().intValue();

            if (product.getQuantity() < requestedQuantity) {
                throw new PaymentFailedException("Stock insuffisant pour " + product.getName());
            }

            // Décrémenter le stock
            product.setQuantity(product.getQuantity() - requestedQuantity);
            productRepository.save(product);
        }

        // Publier l'événement
        eventPublisher.publishEvent(new SaleCompletedEvent(sale));

        log.info("🎉 Paiement finalisé avec succès pour la vente: {}", saleId);
        return PaymentResultMapper.toDTO(result, sale);
    }

    // ========== REQUÊTES ==========
    @Override
    public SaleDetailsDTO getSaleDetails(UUID saleId) {
        Sale sale = findSaleOrThrow(saleId);
        return SaleDetailsMapper.toDTO(sale);
    }

    @Override
    public SaleTotalDTO calculateSaleTotal(UUID saleId) {
        Sale sale = findSaleOrThrow(saleId);
        return priceCalculator.calculateSaleTotal(sale);
    }

    @Override
    public List<SaleResponseDTO> getSalesByClient(UUID clientId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Sale> sales = saleRepository.findByClientUserIdOrderBySaleTimestampDesc(clientId, pageable);

        return sales.stream()
                .map(SaleMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleResponseDTO> getTodaySales(UUID clientId) {
        return saleRepository.findTodaySalesByClient(clientId).stream()
                .map(SaleMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ReceiptDTO generateReceiptData(UUID saleId) {
        Sale sale = findSaleOrThrow(saleId);

        if (sale.getPayments().isEmpty()) {
            throw new SaleNotPaidException("Cette vente n'a pas été payée");
        }

        return ReceiptGenerator.generate(sale);
    }
    public int getCurrentStock(UUID productId, UUID clientId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent() && productOpt.get().getClient().getUserId().equals(clientId)) {
            return productOpt.get().getQuantity();
        }
        return 0;
    }

    // ========== MÉTHODES SPÉCIFIQUES À LA GESTION MULTI-CAISSES ==========


    // ========== MÉTHODES EXISTANTES CONSERVÉES ==========

    public BigDecimal getTodayTotalSales(UUID clientId) {
        return saleRepository.getTodayTotalSales(clientId);
    }

    public boolean canAccessSale(UUID saleId, UUID userId) {
        return saleValidationService.canAccessSale(saleId, userId);
    }

    // ========== MÉTHODES PRIVÉES ==========

    private Sale findSaleOrThrow(UUID saleId) {
        return saleRepository.findById(saleId)
                .orElseThrow(() -> new SaleNotFoundException(saleId));
    }

    /**
     * AJOUT DE PRODUIT AVEC VALIDATION OPTIMISTE
     */
    private SaleItemResponseDTO addProductInternal(Sale sale, Product product, BigDecimal quantity) {
        // 1. Validation immédiate du stock disponible
        validateStockAvailability(product, sale.getClient().getUserId(), quantity.intValue());

        Optional<SaleItem> existingItem = findExistingItem(sale, product);

        if (existingItem.isPresent()) {
            return updateExistingItem(existingItem.get(), quantity);
        } else {
            return createNewItem(sale, product, quantity);
        }
    }

    /**
     *  VALIDATION DE STOCK AVEC SERVICE OPTIMISTE
     */
    private void validateStockAvailability(Product product, UUID clientId, int requestedQuantity) {
        if (product.getQuantity() < requestedQuantity) {
            throw new InsufficientStockException(
                    String.format("Stock insuffisant pour %s. Disponible: %d, Demandé: %d",
                            product.getName(), product.getQuantity(), requestedQuantity));
        }
    }

    private Optional<SaleItem> findExistingItem(Sale sale, Product product) {
        return sale.getSaleItems().stream()
                .filter(item -> item.getProduct().getProductId().equals(product.getProductId()))
                .findFirst();
    }

    /**
     *  MISE À JOUR D'ARTICLE EXISTANT AVEC VALIDATION
     */
    private SaleItemResponseDTO updateExistingItem(SaleItem item, BigDecimal additionalQuantity) {
        BigDecimal newQuantity = item.getQuantitySold().add(additionalQuantity);

        // Validation du stock pour la nouvelle quantité totale
        validateStockAvailability(
                item.getProduct(),
                item.getSale().getClient().getUserId(),
                newQuantity.intValue()
        );

        item.setQuantitySold(newQuantity);
        item.setPriceAtSale(priceCalculator.calculateItemPrice(
                item.getProduct(), newQuantity));

        saleItemRepository.save(item);
        updateSaleTotal(item.getSale());

        return SaleItemMapper.toResponseDTO(item);
    }

    /**
     *  CRÉATION DE NOUVEL ARTICLE AVEC VALIDATION
     */
    private SaleItemResponseDTO createNewItem(Sale sale, Product product, BigDecimal quantity) {
        SaleItem newItem = SaleItem.builder()
                .sale(sale)
                .product(product)
                .quantitySold(quantity)
                .priceAtSale(priceCalculator.calculateItemPrice(product, quantity))
                .build();

        sale.getSaleItems().add(newItem);
        saleItemRepository.save(newItem);
        updateSaleTotal(sale);

        return SaleItemMapper.toResponseDTO(newItem);
    }

    private void updateSaleTotal(Sale sale) {
        BigDecimal total = priceCalculator.calculateTotal(sale.getSaleItems());
        sale.setTotalAmount(total);
        saleRepository.save(sale);
    }

    private void validateProductBelongsToClient(Product product, Client client) {
        if (!product.getClient().getUserId().equals(client.getUserId())) {
            throw new ProductNotBelongToClientException(
                    "Ce produit n'appartient pas à ce client");
        }
    }

    //  AJOUTEZ CES MÉTHODES À VOTRE SaleService.java EXISTANT

    /**
     * Récupère les ventes en attente (non payées)
     */
    public List<SaleResponseDTO> getPendingSales(UUID clientId) {
        return saleRepository.findPendingSalesByClient(clientId).stream()
                .map(SaleMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    //  MODIFIEZ CES MÉTHODES DANS VOTRE SaleService.java

    /**
     * Top produits vendus aujourd'hui - ADAPTÉ
     */
    public List<Object[]> getTodayTopProducts(UUID clientId, int limit) {
        List<Object[]> allProducts = saleRepository.findTodayTopSellingProducts(clientId);

        // Limiter manuellement les résultats
        return allProducts.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Statistiques de ventes par heure - ADAPTÉ
     */
    public List<Object[]> getTodayHourlySalesStats(UUID clientId) {
        //  Passer l'UUID comme String pour la requête native
        return saleRepository.findTodayHourlySalesStats(clientId.toString());
    }

    /**
     * Vérification de disponibilité de stock avant ajout
     */
    public boolean checkProductAvailability(UUID productId, UUID clientId, BigDecimal quantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent() && productOpt.get().getClient().getUserId().equals(clientId)) {
            return productOpt.get().getQuantity() >= quantity.intValue();
        }
        return false;
    }

    /**
     * Informations de stock en temps réel
     */
    public Map<String, Object> getRealtimeStockInfo(UUID productId, UUID clientId) {
        int currentStock = getCurrentStock(productId, clientId);

        // Récupérer le produit pour plus d'infos
        Optional<Product> productOpt = productRepository.findById(productId);

        Map<String, Object> stockInfo = new HashMap<>();
        stockInfo.put("productId", productId);
        stockInfo.put("currentStock", currentStock);
        stockInfo.put("available", currentStock > 0);
        stockInfo.put("lastUpdated", System.currentTimeMillis());

        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            stockInfo.put("productName", product.getName());
            stockInfo.put("barcode", product.getBarcode());
            stockInfo.put("unitPrice", product.getUnitPrice());
        }

        return stockInfo;
    }
}