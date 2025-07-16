package esgi.easisell.repository;

import esgi.easisell.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByClientUserId(UUID clientId);

    List<Product> findByCategoryCategoryId(UUID categoryId);

    List<Product> findByClientUserIdAndNameContainingIgnoreCase(UUID clientId, String name);

    @Query("SELECT p FROM Product p WHERE p.client.userId = :clientId AND p.barcode = :barcode")
    Product findByClientAndBarcode(@Param("clientId") UUID clientId, @Param("barcode") String barcode);

    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.client.userId = :clientId AND p.barcode = :barcode")
    boolean existsByClientIdAndBarcode(@Param("clientId") UUID clientId, @Param("barcode") String barcode);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.client.userId = :clientId")
    long countByClientUserId(@Param("clientId") UUID clientId);

    List<Product> findByClientUserIdAndBrandIgnoreCase(UUID clientId, String brand);

    List<Product> findByClientUserIdAndBarcodeIsNotNull(UUID clientId);

    List<Product> findByClientUserIdAndBarcodeIsNull(UUID clientId);

    @Query("SELECT p FROM Product p WHERE p.client.userId = :clientId AND p.quantity <= p.reorderThreshold AND p.reorderThreshold IS NOT NULL")
    List<Product> findLowStockProducts(@Param("clientId") UUID clientId);

    @Query("SELECT p FROM Product p WHERE p.client.userId = :clientId AND p.expirationDate <= :futureDate AND p.expirationDate IS NOT NULL")
    List<Product> findExpiringProducts(@Param("clientId") UUID clientId, @Param("futureDate") Timestamp futureDate);

    @Query("SELECT p FROM Product p WHERE p.client.userId = :clientId AND p.quantity = 0")
    List<Product> findOutOfStockProducts(@Param("clientId") UUID clientId);

    @Query("SELECT p FROM Product p WHERE p.client.userId = :clientId AND p.reorderThreshold IS NULL")
    List<Product> findProductsWithoutThreshold(@Param("clientId") UUID clientId);

    List<Product> findBySupplierSupplierId(UUID supplierId);
}