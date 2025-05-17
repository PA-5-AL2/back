package esgi.easisell.repository;

import esgi.easisell.entity.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Repository
public interface StockItemRepository extends JpaRepository<StockItem, UUID> {

    List<StockItem> findByClientUserId(UUID clientId);

    List<StockItem> findByProductProductId(UUID productId);

    @Query("SELECT s FROM StockItem s WHERE s.client.userId = :clientId AND s.quantity <= s.reorderThreshold")
    List<StockItem> findLowStockItems(@Param("clientId") UUID clientId);

    @Query("SELECT s FROM StockItem s WHERE s.client.userId = :clientId AND s.expirationDate <= :futureDate AND s.expirationDate IS NOT NULL")
    List<StockItem> findExpiringItems(@Param("clientId") UUID clientId, @Param("futureDate") Timestamp futureDate);

    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM StockItem s WHERE s.client.userId = :clientId AND s.product.productId = :productId")
    int getTotalStockQuantityByProduct(@Param("clientId") UUID clientId, @Param("productId") UUID productId);

    List<StockItem> findByClientUserIdAndProductNameContainingIgnoreCase(UUID clientId, String productName);

    List<StockItem> findBySupplierSupplierId(UUID supplierId);

    @Query("SELECT s FROM StockItem s WHERE s.client.userId = :clientId AND s.supplier IS NULL")
    List<StockItem> findItemsWithoutSupplier(@Param("clientId") UUID clientId);

    @Query("SELECT COUNT(s) FROM StockItem s WHERE s.client.userId = :clientId")
    long countByClientId(@Param("clientId") UUID clientId);
}