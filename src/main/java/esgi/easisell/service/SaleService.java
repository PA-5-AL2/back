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

// SERVICE PRINCIPAL - Open/Closed Principle (extensible sans modification)
@Service
@RequiredArgsConstructor
@Slf4j
public class SaleService implements ISaleCreationService, ISaleItemService, ISalePaymentService, ISaleQueryService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final ClientRepository clientRepository;

    // Dependency Inversion - dépend d'abstractions
    private final IStockValidationService stockValidationService;
    private final IStockUpdateService stockUpdateService;
    private final ISaleValidationService saleValidationService;
    private final ISalePriceCalculator priceCalculator;
    private final ApplicationEventPublisher eventPublisher;

    // ========== CRÉATION DE VENTE ==========
    @Override
    @Transactional
    public SaleResponseDTO createNewSale(UUID clientId) {
        log.info("Création d'une nouvelle vente pour le client: {}", clientId);

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));

        // ✅ Construction manuelle garantie de fonctionner
        Sale sale = new Sale();
        sale.setClient(client);
        sale.setSaleTimestamp(Timestamp.valueOf(LocalDateTime.now()));
        sale.setTotalAmount(BigDecimal.ZERO);
        sale.setIsDeferred(false);
        sale.setSaleItems(new ArrayList<>());
        sale.setPayments(new ArrayList<>());

        Sale savedSale = saleRepository.save(sale);
        return SaleMapper.toResponseDTO(savedSale);
    }

    // ========== GESTION DES ARTICLES ==========
    @Override
    @Transactional
    public SaleItemResponseDTO addProductToSale(UUID saleId, String barcode, int quantity) {
        log.info("Scan du produit {} pour la vente {}", barcode, saleId);

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
    public SaleItemResponseDTO addProductByIdToSale(UUID saleId, UUID productId, int quantity) {
        log.info("Ajout manuel du produit {} à la vente {}", productId, saleId);

        Sale sale = findSaleOrThrow(saleId);
        saleValidationService.validateSaleNotFinalized(sale);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("ID: " + productId));

        validateProductBelongsToClient(product, sale.getClient());

        return addProductInternal(sale, product, quantity);
    }

    @Override
    @Transactional
    public SaleItemResponseDTO updateItemQuantity(UUID saleItemId, int newQuantity) {
        if (newQuantity <= 0) {
            throw new InvalidQuantityException("La quantité doit être supérieure à 0");
        }

        SaleItem saleItem = saleItemRepository.findById(saleItemId)
                .orElseThrow(() -> new SaleItemNotFoundException(saleItemId));

        saleValidationService.validateSaleNotFinalized(saleItem.getSale());
        stockValidationService.validateStockAvailable(
                saleItem.getProduct(),
                saleItem.getSale().getClient().getUserId(),
                newQuantity
        );

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

    // ========== PAIEMENT ==========
    @Override
    @Transactional
    public PaymentResultDTO processPayment(UUID saleId, String paymentType,
                                           BigDecimal amountReceived, String currency) {
        log.info("Traitement du paiement pour la vente: {}", saleId);

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

        // Mettre à jour le stock
        stockUpdateService.decreaseStockForSale(sale);

        // Publier l'événement
        eventPublisher.publishEvent(new SaleCompletedEvent(sale));

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

    // ========== MÉTHODE MANQUANTE ==========
    public BigDecimal getTodayTotalSales(UUID clientId) {
        return saleRepository.getTodayTotalSales(clientId);
    }

    // ========== MÉTHODE POUR LE CONTROLLER ==========
    public boolean canAccessSale(UUID saleId, UUID userId) {
        return saleValidationService.canAccessSale(saleId, userId);
    }

    // ========== MÉTHODES PRIVÉES ==========
    private Sale findSaleOrThrow(UUID saleId) {
        return saleRepository.findById(saleId)
                .orElseThrow(() -> new SaleNotFoundException(saleId));
    }

    private SaleItemResponseDTO addProductInternal(Sale sale, Product product, int quantity) {
        stockValidationService.validateStockAvailable(
                product, sale.getClient().getUserId(), quantity);

        Optional<SaleItem> existingItem = findExistingItem(sale, product);

        if (existingItem.isPresent()) {
            return updateExistingItem(existingItem.get(), quantity);
        } else {
            return createNewItem(sale, product, quantity);
        }
    }

    private Optional<SaleItem> findExistingItem(Sale sale, Product product) {
        return sale.getSaleItems().stream()
                .filter(item -> item.getProduct().getProductId().equals(product.getProductId()))
                .findFirst();
    }

    private SaleItemResponseDTO updateExistingItem(SaleItem item, int additionalQuantity) {
        int newQuantity = item.getQuantitySold() + additionalQuantity;

        stockValidationService.validateStockAvailable(
                item.getProduct(),
                item.getSale().getClient().getUserId(),
                newQuantity
        );

        item.setQuantitySold(newQuantity);
        item.setPriceAtSale(priceCalculator.calculateItemPrice(
                item.getProduct(), newQuantity));

        saleItemRepository.save(item);
        updateSaleTotal(item.getSale());

        return SaleItemMapper.toResponseDTO(item);
    }

    private SaleItemResponseDTO createNewItem(Sale sale, Product product, int quantity) {
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
}
