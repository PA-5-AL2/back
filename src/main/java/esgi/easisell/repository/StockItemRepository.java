package esgi.easisell.repository;

import esgi.easisell.entity.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
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

    // ========== NOUVELLES REQUÊTES POUR GESTION MULTI-CAISSES ==========

    /**
     * ✅ CRUCIAL : Récupère les lots de stock triés par FIFO (First In, First Out)
     * Utilisé pour la gestion "stoïque" du stock
     */
    @Query("SELECT s FROM StockItem s WHERE s.product.productId = :productId AND s.client.userId = :clientId " +
            "AND s.quantity > 0 ORDER BY s.expirationDate ASC NULLS LAST, s.purchaseDate ASC")
    List<StockItem> findByProductProductIdAndClientUserIdOrderByExpirationDateAsc(
            @Param("productId") UUID productId,
            @Param("clientId") UUID clientId
    );

    /**
     * ✅ Verrouillage pessimiste pour cas exceptionnels
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM StockItem s WHERE s.stockItemId = :stockItemId")
    Optional<StockItem> findByIdWithPessimisticLock(@Param("stockItemId") UUID stockItemId);

    /**
     * ✅ Vérification rapide de disponibilité
     */
    @Query("SELECT CASE WHEN SUM(s.quantity) >= :requiredQuantity THEN true ELSE false END FROM StockItem s " +
            "WHERE s.product.productId = :productId AND s.client.userId = :clientId")
    boolean isProductAvailable(
            @Param("productId") UUID productId,
            @Param("clientId") UUID clientId,
            @Param("requiredQuantity") int requiredQuantity
    );

    /**
     * ✅ Audit des mouvements récents
     */
    @Query("SELECT s FROM StockItem s WHERE s.client.userId = :clientId " +
            "AND s.lastModified >= :since ORDER BY s.lastModified DESC")
    List<StockItem> findRecentStockMovements(
            @Param("clientId") UUID clientId,
            @Param("since") Timestamp since
    );

    /**
     * ✅ Statistiques de stock pour monitoring
     */
    @Query("SELECT " +
            "COUNT(DISTINCT s.product.productId) as totalProducts, " +
            "SUM(s.quantity) as totalQuantity, " +
            "COUNT(CASE WHEN s.quantity <= s.reorderThreshold THEN 1 END) as lowStockItems, " +
            "COUNT(CASE WHEN s.expirationDate <= CURRENT_TIMESTAMP THEN 1 END) as expiredItems " +
            "FROM StockItem s WHERE s.client.userId = :clientId")
    Object[] getStockStatistics(@Param("clientId") UUID clientId);

    // ✅ AJOUTEZ CES MÉTHODES À VOTRE StockItemRepository.java EXISTANT

    /**
     * Items sans seuil d'alerte configuré
     */
    List<StockItem> findByClientUserIdAndReorderThresholdIsNull(UUID clientId);

    /**
     * Items en rupture de stock (quantité = 0)
     */
    List<StockItem> findByClientUserIdAndQuantity(UUID clientId, Integer quantity);

    /**
     * Items avec stock faible (quantité <= seuil)
     */
    @Query("SELECT s FROM StockItem s WHERE s.client.userId = :clientId " +
            "AND s.quantity <= s.reorderThreshold AND s.reorderThreshold IS NOT NULL")
    List<StockItem> findLowStockItemsWithThreshold(@Param("clientId") UUID clientId);

    /**
     * Recherche de stock par code-barres produit
     */
    @Query("SELECT s FROM StockItem s WHERE s.client.userId = :clientId " +
            "AND s.product.barcode = :barcode")
    List<StockItem> findByClientUserIdAndProductBarcode(@Param("clientId") UUID clientId,
                                                        @Param("barcode") String barcode);

    /**
     * Stock total disponible pour un produit
     */
    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM StockItem s " +
            "WHERE s.product.productId = :productId AND s.client.userId = :clientId " +
            "AND s.quantity > 0")
    Integer getTotalAvailableStock(@Param("productId") UUID productId,
                                   @Param("clientId") UUID clientId);
}