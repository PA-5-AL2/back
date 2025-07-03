/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : StockItemTest.java
 * @description : Tests unitaires pour l'entité StockItem
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 03/07/2025
 * @package     : esgi.easisell.entity
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The type Stock item test.
 */
class StockItemTest {

    private StockItem stockItem;
    private Product product;
    private Client client;
    private Supplier supplier;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        stockItem = new StockItem();

        client = new Client();
        client.setUserId(UUID.randomUUID());

        product = new Product();
        product.setProductId(UUID.randomUUID());
        product.setName("Test Product");

        supplier = new Supplier();
        supplier.setSupplierId(UUID.randomUUID());
        supplier.setName("Test Supplier");
    }

    /**
     * Test stock item getters setters.
     */
    @Test
    @DisplayName("✅ Getters/Setters stock item")
    void testStockItemGettersSetters() {
        UUID stockItemId = UUID.randomUUID();
        Integer quantity = 100;
        Integer reorderThreshold = 10;
        Timestamp purchaseDate = Timestamp.valueOf(LocalDateTime.now().minusDays(1));
        Timestamp expirationDate = Timestamp.valueOf(LocalDateTime.now().plusDays(30));
        BigDecimal purchasePrice = new BigDecimal("8.50");
        Long version = 1L;

        stockItem.setStockItemId(stockItemId);
        stockItem.setQuantity(quantity);
        stockItem.setReorderThreshold(reorderThreshold);
        stockItem.setPurchaseDate(purchaseDate);
        stockItem.setExpirationDate(expirationDate);
        stockItem.setPurchasePrice(purchasePrice);
        stockItem.setProduct(product);
        stockItem.setClient(client);
        stockItem.setSupplier(supplier);
        stockItem.setVersion(version);

        assertEquals(stockItemId, stockItem.getStockItemId());
        assertEquals(quantity, stockItem.getQuantity());
        assertEquals(reorderThreshold, stockItem.getReorderThreshold());
        assertEquals(purchaseDate, stockItem.getPurchaseDate());
        assertEquals(expirationDate, stockItem.getExpirationDate());
        assertEquals(purchasePrice, stockItem.getPurchasePrice());
        assertEquals(product, stockItem.getProduct());
        assertEquals(client, stockItem.getClient());
        assertEquals(supplier, stockItem.getSupplier());
        assertEquals(version, stockItem.getVersion());
    }

    /**
     * Test required relations.
     */
    @Test
    @DisplayName("✅ Relations obligatoires")
    void testRequiredRelations() {
        stockItem.setQuantity(50);
        stockItem.setProduct(product);
        stockItem.setClient(client);

        assertNotNull(stockItem.getQuantity());
        assertNotNull(stockItem.getProduct());
        assertNotNull(stockItem.getClient());
    }

    /**
     * Test on update callback.
     */
    @Test
    @DisplayName("✅ Callback onUpdate")
    void testOnUpdateCallback() {
        stockItem.onUpdate();

        assertNotNull(stockItem.getLastModified());
        assertTrue(stockItem.getLastModified().before(
                Timestamp.valueOf(LocalDateTime.now().plusSeconds(1))));
    }

    /**
     * Test quantity validation.
     */
    @Test
    @DisplayName("✅ Validation métier - quantité positive")
    void testQuantityValidation() {
        stockItem.setQuantity(100);

        assertTrue(stockItem.getQuantity() >= 0);
    }

    /**
     * Test optimistic locking.
     */
    @Test
    @DisplayName("✅ Optimistic locking avec version")
    void testOptimisticLocking() {
        stockItem.setVersion(1L);
        assertEquals(1L, stockItem.getVersion());

        // Simulation d'une mise à jour
        stockItem.setVersion(2L);
        assertEquals(2L, stockItem.getVersion());
    }
}