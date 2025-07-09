package esgi.easisell.service;

import esgi.easisell.entity.Product;
import esgi.easisell.entity.Sale;
import esgi.easisell.entity.SaleItem;
import esgi.easisell.entity.StockAuditLog;
import esgi.easisell.entity.StockItem;
import esgi.easisell.exception.InsufficientStockException;
import esgi.easisell.exception.StockUpdateException;
import esgi.easisell.repository.StockItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * ✅ SERVICE PRINCIPAL POUR GESTION MULTI-CAISSES "STOIQUE"
 * Ce service gère les conflits de concurrence entre plusieurs caisses
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OptimisticStockService {

    private final StockItemRepository stockItemRepository;
    private final StockAuditService auditService; // ✅ AJOUT DU SERVICE D'AUDIT
    private static final int MAX_RETRY_ATTEMPTS = 3;

    /**
     * ✅ VALIDATION : Vérifie si le stock est suffisant
     */
    @Transactional(readOnly = true)
    public boolean isStockSufficient(UUID productId, UUID clientId, int requestedQuantity) {
        int availableStock = getTotalStockQuantity(productId, clientId);
        boolean sufficient = availableStock >= requestedQuantity;

        log.debug("🔍 Vérification stock - Produit: {}, Demandé: {}, Disponible: {}, Suffisant: {}",
                productId, requestedQuantity, availableStock, sufficient);

        return sufficient;
    }

    /**
     * ✅ DÉCRÉMENTATION PRINCIPALE avec retry automatique
     * Cette méthode est le cœur de la gestion multi-caisses
     */
    @Retryable(
            value = {OptimisticLockingFailureException.class},
            maxAttempts = MAX_RETRY_ATTEMPTS,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void decreaseStockForSale(Sale sale) throws StockUpdateException {
        log.info("🔄 DÉBUT décrémentation stock pour vente: {}", sale.getSaleId());

        for (SaleItem saleItem : sale.getSaleItems()) {
            decreaseStockForProduct(
                    saleItem.getProduct().getProductId(),
                    sale.getClient().getUserId(),
                    saleItem.getQuantitySold().intValue()  // ✅ FIX : Conversion BigDecimal vers int
            );
        }

        log.info("✅ SUCCÈS décrémentation stock pour vente: {}", sale.getSaleId());
    }

    /**
     * ✅ DÉCRÉMENTATION POUR UN PRODUIT SPÉCIFIQUE avec AUDIT
     * Utilise la méthode FIFO (First In, First Out) pour gérer les lots
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void decreaseStockForProduct(UUID productId, UUID clientId, int quantityToDecrease)
            throws StockUpdateException {

        log.debug("🔄 Décrémentation {} unités - Produit: {}", quantityToDecrease, productId);

        // 1. Récupérer tous les lots FIFO (par date d'expiration)
        List<StockItem> stockItems = stockItemRepository
                .findByProductProductIdAndClientUserIdOrderByExpirationDateAsc(productId, clientId);

        if (stockItems.isEmpty()) {
            throw new StockUpdateException("❌ Aucun stock trouvé pour le produit: " + productId);
        }

        int remainingToDecrease = quantityToDecrease;

        // 2. Appliquer FIFO : traiter les lots par ordre d'expiration
        for (StockItem stockItem : stockItems) {
            if (remainingToDecrease <= 0) break;

            try {
                int currentQuantity = stockItem.getQuantity();

                if (currentQuantity > 0) {
                    int toDecrease = Math.min(currentQuantity, remainingToDecrease);

                    // ✅ CRÉER UNE COPIE POUR L'AUDIT
                    StockItem oldItem = createCopyForAudit(stockItem);

                    stockItem.setQuantity(currentQuantity - toDecrease);

                    // ⚡ POINT CRITIQUE : Sauvegarde avec versioning optimiste
                    StockItem savedItem = stockItemRepository.save(stockItem);

                    // ✅ AUDIT AUTOMATIQUE
                    auditService.logStockUpdate(oldItem, savedItem, StockAuditLog.OperationType.UPDATE);

                    remainingToDecrease -= toDecrease;

                    log.debug("✅ Décrémenté {} unités du lot {}, nouveau stock: {}",
                            toDecrease, stockItem.getStockItemId(), stockItem.getQuantity());
                }
            } catch (OptimisticLockingFailureException e) {
                // 🔄 Conflit détecté : une autre caisse a modifié ce stock
                log.warn("⚠️ CONFLIT de concurrence sur lot: {}. Retry automatique...",
                        stockItem.getStockItemId());
                throw e; // Le @Retryable va relancer automatiquement
            }
        }

        // 3. Vérifier que la décrémentation est complète
        if (remainingToDecrease > 0) {
            throw new InsufficientStockException(
                    String.format("❌ Stock insuffisant. Demandé: %d, Manque: %d",
                            quantityToDecrease, remainingToDecrease)
            );
        }
    }

    /**
     * ✅ INCRÉMENTATION DE STOCK (réception de marchandises)
     */
    @Retryable(
            value = {OptimisticLockingFailureException.class},
            maxAttempts = MAX_RETRY_ATTEMPTS,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void increaseStock(UUID stockItemId, int quantityToAdd) throws StockUpdateException {
        log.info("📈 Augmentation stock +{} unités - Lot: {}", quantityToAdd, stockItemId);

        try {
            StockItem stockItem = stockItemRepository.findById(stockItemId)
                    .orElseThrow(() -> new StockUpdateException("❌ Lot de stock introuvable: " + stockItemId));

            // ✅ CRÉER UNE COPIE POUR L'AUDIT
            StockItem oldItem = createCopyForAudit(stockItem);

            stockItem.setQuantity(stockItem.getQuantity() + quantityToAdd);
            StockItem savedItem = stockItemRepository.save(stockItem);

            // ✅ AUDIT AUTOMATIQUE
            auditService.logStockUpdate(oldItem, savedItem, StockAuditLog.OperationType.UPDATE);

            log.info("✅ Stock augmenté. Nouvelle quantité: {}", stockItem.getQuantity());

        } catch (OptimisticLockingFailureException e) {
            log.warn("⚠️ Conflit lors augmentation stock. Retry automatique...");
            throw e;
        }
    }

    /**
     * ✅ CONSULTATION DU STOCK TOTAL (lecture seule)
     */
    @Transactional(readOnly = true)
    public int getTotalStockQuantity(UUID productId, UUID clientId) {
        return stockItemRepository.getTotalStockQuantityByProduct(clientId, productId);
    }

    /**
     * ✅ VALIDATION AVANT VENTE (appelée juste avant le paiement)
     */
    @Transactional(readOnly = true)
    public void validateStockBeforeSale(Sale sale) throws InsufficientStockException {
        log.info("🔍 VALIDATION FINALE stock avant paiement - Vente: {}", sale.getSaleId());

        for (SaleItem saleItem : sale.getSaleItems()) {
            Product product = saleItem.getProduct();
            int requestedQuantity = saleItem.getQuantitySold().intValue();  // ✅ FIX : Conversion

            if (!isStockSufficient(product.getProductId(), sale.getClient().getUserId(), requestedQuantity)) {
                int availableStock = getTotalStockQuantity(product.getProductId(), sale.getClient().getUserId());
                throw new InsufficientStockException(
                        String.format("❌ Stock insuffisant pour '%s'. Disponible: %d, Demandé: %d",
                                product.getName(), availableStock, requestedQuantity)
                );
            }
        }

        log.info("✅ VALIDATION stock réussie pour vente: {}", sale.getSaleId());
    }

    /**
     * ✅ RÉSERVATION TEMPORAIRE (optionnel, pour ventes en attente)
     */
    @Retryable(
            value = {OptimisticLockingFailureException.class},
            maxAttempts = MAX_RETRY_ATTEMPTS,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean reserveStock(UUID productId, UUID clientId, int quantityToReserve) {
        log.info("🔒 TENTATIVE réservation {} unités - Produit: {}", quantityToReserve, productId);

        try {
            if (!isStockSufficient(productId, clientId, quantityToReserve)) {
                log.warn("❌ Stock insuffisant pour réservation");
                return false;
            }

            // Pour la réservation, on décrémente directement
            // (dans un système plus complexe, on pourrait avoir un champ reserved_quantity)
            decreaseStockForProduct(productId, clientId, quantityToReserve);

            log.info("✅ RÉSERVATION réussie pour {} unités", quantityToReserve);
            return true;

        } catch (Exception e) {
            log.error("❌ ÉCHEC réservation: {}", e.getMessage());
            return false;
        }
    }

    /**
     * ✅ MÉTHODE UTILITAIRE : Créer une copie pour l'audit
     */
    private StockItem createCopyForAudit(StockItem original) {
        StockItem copy = new StockItem();
        copy.setStockItemId(original.getStockItemId());
        copy.setQuantity(original.getQuantity());
        copy.setVersion(original.getVersion());
        copy.setProduct(original.getProduct());
        copy.setClient(original.getClient());
        copy.setLastModified(original.getLastModified());
        return copy;
    }
}