package esgi.easisell.repository;

import esgi.easisell.entity.StockAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * ✅ REPOSITORY pour les logs d'audit de stock
 */
@Repository
public interface StockAuditLogRepository extends JpaRepository<StockAuditLog, Long> {

    /**
     * Historique des modifications d'un produit
     */
    List<StockAuditLog> findByProductIdOrderByModifiedAtDesc(UUID productId);

    /**
     * Modifications récentes pour un client
     */
    @Query("SELECT sal FROM StockAuditLog sal WHERE sal.clientId = :clientId " +
            "AND sal.modifiedAt >= :since ORDER BY sal.modifiedAt DESC")
    List<StockAuditLog> findRecentModificationsByClient(
            @Param("clientId") UUID clientId,
            @Param("since") Timestamp since
    );

    /**
     * ✅ STATISTIQUES DE CONCURRENCE - VERSION NATIVE SQL
     */
    @Query(value = "SELECT " +
            "DATE(sal.modified_at) as activity_date, " +
            "HOUR(sal.modified_at) as activity_hour, " +
            "sal.client_id, " +
            "COUNT(DISTINCT sal.stock_item_id) as concurrent_modifications, " +
            "COUNT(*) as total_operations " +
            "FROM stock_audit_log sal " +
            "WHERE sal.client_id = UNHEX(REPLACE(:clientId, '-', '')) " +
            "AND sal.modified_at >= :since " +
            "GROUP BY DATE(sal.modified_at), HOUR(sal.modified_at), sal.client_id " +
            "HAVING concurrent_modifications > 1 " +
            "ORDER BY activity_date DESC, activity_hour DESC",
            nativeQuery = true)
    List<Object[]> findConcurrencyStatistics(
            @Param("clientId") String clientId,
            @Param("since") Timestamp since
    );

    /**
     * Logs d'audit par type d'opération
     */
    List<StockAuditLog> findByClientIdAndOperationTypeOrderByModifiedAtDesc(
            UUID clientId, StockAuditLog.OperationType operationType);

    /**
     * Logs d'audit pour un stock item spécifique
     */
    List<StockAuditLog> findByStockItemIdOrderByModifiedAtDesc(UUID stockItemId);
}