package esgi.easisell.service;

import esgi.easisell.entity.StockAuditLog;
import esgi.easisell.entity.StockItem;
import esgi.easisell.repository.StockAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * ✅ SERVICE D'AUDIT pour traçabilité des modifications de stock
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StockAuditService {

    private final StockAuditLogRepository auditRepository;

    /**
     * ✅ Enregistre une modification de stock
     */
    @Transactional
    public void logStockUpdate(StockItem oldItem, StockItem newItem, StockAuditLog.OperationType operation) {
        try {
            StockAuditLog auditLog = StockAuditLog.builder()
                    .stockItemId(newItem.getStockItemId())
                    .productId(newItem.getProduct().getProductId())
                    .clientId(newItem.getClient().getUserId())
                    .operationType(operation)
                    .oldQuantity(oldItem != null ? oldItem.getQuantity() : null)
                    .newQuantity(newItem.getQuantity())
                    .oldVersion(oldItem != null ? oldItem.getVersion() : null)
                    .newVersion(newItem.getVersion())
                    .build();

            auditRepository.save(auditLog);

            log.debug("📝 Audit enregistré : {} {} -> {}",
                    operation,
                    oldItem != null ? oldItem.getQuantity() : "NULL",
                    newItem.getQuantity());

        } catch (Exception e) {
            log.error("❌ Erreur lors de l'audit : {}", e.getMessage());
        }
    }

    /**
     * ✅ Enregistre une création de stock
     */
    @Transactional
    public void logStockCreation(StockItem newItem) {
        logStockUpdate(null, newItem, StockAuditLog.OperationType.INSERT);
    }

    /**
     * ✅ Enregistre une suppression de stock
     */
    @Transactional
    public void logStockDeletion(StockItem deletedItem) {
        try {
            StockAuditLog auditLog = StockAuditLog.builder()
                    .stockItemId(deletedItem.getStockItemId())
                    .productId(deletedItem.getProduct().getProductId())
                    .clientId(deletedItem.getClient().getUserId())
                    .operationType(StockAuditLog.OperationType.DELETE)
                    .oldQuantity(deletedItem.getQuantity())
                    .newQuantity(0)
                    .oldVersion(deletedItem.getVersion())
                    .newVersion(null)
                    .build();

            auditRepository.save(auditLog);

        } catch (Exception e) {
            log.error("❌ Erreur lors de l'audit de suppression : {}", e.getMessage());
        }
    }

    /**
     * ✅ Récupère l'historique des modifications d'un produit
     */
    public List<StockAuditLog> getProductHistory(UUID productId) {
        return auditRepository.findByProductIdOrderByModifiedAtDesc(productId);
    }

    /**
     * ✅ Récupère les modifications récentes d'un client
     */
    public List<StockAuditLog> getRecentModifications(UUID clientId, int hours) {
        Timestamp since = Timestamp.valueOf(LocalDateTime.now().minusHours(hours));
        return auditRepository.findRecentModificationsByClient(clientId, since);
    }

    /**
     * ✅ Statistiques de concurrence pour monitoring
     */
    public List<Object[]> getConcurrencyStatistics(UUID clientId, int hours) {
        Timestamp since = Timestamp.valueOf(LocalDateTime.now().minusHours(hours));
        return auditRepository.findConcurrencyStatistics(clientId.toString(), since); // ✅ AJOUT .toString()
    }

    /**
     * ✅ Logs par type d'opération
     */
    public List<StockAuditLog> getLogsByOperation(UUID clientId, StockAuditLog.OperationType operation) {
        return auditRepository.findByClientIdAndOperationTypeOrderByModifiedAtDesc(clientId, operation);
    }

    /**
     * ✅ Historique d'un stock item spécifique
     */
    public List<StockAuditLog> getStockItemHistory(UUID stockItemId) {
        return auditRepository.findByStockItemIdOrderByModifiedAtDesc(stockItemId);
    }
}