/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : StockItemRepositoryTest.java
 * @description : Tests d'intégration pour StockItemRepository
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 11/07/2025
 * @package     : esgi.easisell.repository
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.repository;

import esgi.easisell.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour StockItemRepository
 * Teste les requêtes personnalisées importantes
 */
@DataJpaTest
class StockItemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StockItemRepository stockItemRepository;

    private Client testClient;
    private Product testProduct;
    private Category testCategory;
    private Supplier testSupplier;

    /**
     * Configuration initiale pour chaque test
     */
    @BeforeEach
    void setUp() {
        // Client
        testClient = new Client();
        testClient.setUserId(UUID.randomUUID());
        testClient.setUsername("test@example.com");
        testClient.setPassword("password123");
        testClient.setName("Test Store");
        testClient.setFirstName("Test");
        testClient.setAddress("123 Test Street");
        testClient.setContractStatus("ACTIVE");
        testClient.setCurrencyPreference("EUR");
        testClient.setAccessCode("TEST123");
        testClient = entityManager.persistAndFlush(testClient);

        // Catégorie
        testCategory = new Category();
        testCategory.setCategoryId(UUID.randomUUID());
        testCategory.setName("Test Category");
        testCategory.setClient(testClient);
        testCategory = entityManager.persistAndFlush(testCategory);

        // Produit
        testProduct = new Product();
        testProduct.setProductId(UUID.randomUUID());
        testProduct.setName("Test Product");
        testProduct.setBarcode("1234567890123");
        testProduct.setUnitPrice(BigDecimal.valueOf(10.50));
        testProduct.setIsSoldByWeight(false);
        testProduct.setUnitLabel("pièce");
        testProduct.setClient(testClient);
        testProduct.setCategory(testCategory);
        testProduct = entityManager.persistAndFlush(testProduct);

        // Fournisseur
        testSupplier = new Supplier();
        testSupplier.setSupplierId(UUID.randomUUID());
        testSupplier.setName("Test Supplier");
        testSupplier.setFirstName("Supplier");
        testSupplier.setContactInfo("supplier@test.com");
        testSupplier.setClient(testClient);
        testSupplier = entityManager.persistAndFlush(testSupplier);
    }

    /**
     * Test findLowStockItems() - Stock faible
     */
    @Test
    @DisplayName("✅ Stock faible - findLowStockItems")
    void testFindLowStockItems() {
        // Given
        StockItem lowStock = createStockItem(5, 10); // quantité < seuil
        StockItem normalStock = createStockItem(20, 10); // quantité > seuil

        entityManager.persistAndFlush(lowStock);
        entityManager.persistAndFlush(normalStock);

        // When
        List<StockItem> result = stockItemRepository.findLowStockItems(testClient.getUserId());

        // Then
        assertEquals(1, result.size());
        assertEquals(lowStock.getStockItemId(), result.get(0).getStockItemId());
    }

    /**
     * Test getTotalStockQuantityByProduct() - Stock total
     */
    @Test
    @DisplayName("✅ Stock total par produit - getTotalStockQuantityByProduct")
    void testGetTotalStockQuantityByProduct() {
        // Given
        StockItem lot1 = createStockItem(100, 10);
        StockItem lot2 = createStockItem(50, 5);
        StockItem lot3 = createStockItem(25, 3);

        entityManager.persistAndFlush(lot1);
        entityManager.persistAndFlush(lot2);
        entityManager.persistAndFlush(lot3);

        // When
        int totalStock = stockItemRepository.getTotalStockQuantityByProduct(
                testClient.getUserId(), testProduct.getProductId());

        // Then
        assertEquals(175, totalStock); // 100 + 50 + 25
    }

    /**
     * Test FIFO - findByProductProductIdAndClientUserIdOrderByExpirationDateAsc()
     */
    @Test
    @DisplayName("✅ FIFO - Tri par date d'expiration")
    void testFifoStockRetrieval() {
        // Given
        StockItem oldestLot = createStockItem(30, 5);
        oldestLot.setExpirationDate(Timestamp.valueOf(LocalDateTime.now().plusDays(5)));

        StockItem newestLot = createStockItem(50, 10);
        newestLot.setExpirationDate(Timestamp.valueOf(LocalDateTime.now().plusDays(15)));

        StockItem middleLot = createStockItem(40, 8);
        middleLot.setExpirationDate(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));

        entityManager.persistAndFlush(newestLot);
        entityManager.persistAndFlush(oldestLot);
        entityManager.persistAndFlush(middleLot);

        // When
        List<StockItem> fifoLots = stockItemRepository
                .findByProductProductIdAndClientUserIdOrderByExpirationDateAsc(
                        testProduct.getProductId(), testClient.getUserId());

        // Then
        assertEquals(3, fifoLots.size());
        assertEquals(oldestLot.getStockItemId(), fifoLots.get(0).getStockItemId());
        assertEquals(middleLot.getStockItemId(), fifoLots.get(1).getStockItemId());
        assertEquals(newestLot.getStockItemId(), fifoLots.get(2).getStockItemId());
    }

    /**
     * Test isProductAvailable() - Disponibilité
     */
    @Test
    @DisplayName("✅ Disponibilité produit - isProductAvailable")
    void testIsProductAvailable() {
        // Given
        StockItem lot1 = createStockItem(100, 10);
        StockItem lot2 = createStockItem(50, 5);

        entityManager.persistAndFlush(lot1);
        entityManager.persistAndFlush(lot2);

        // When & Then
        assertTrue(stockItemRepository.isProductAvailable(
                testProduct.getProductId(), testClient.getUserId(), 100));

        assertTrue(stockItemRepository.isProductAvailable(
                testProduct.getProductId(), testClient.getUserId(), 150));

        assertFalse(stockItemRepository.isProductAvailable(
                testProduct.getProductId(), testClient.getUserId(), 200));
    }

    /**
     * Test findExpiringItems() - Produits expirants
     */
    @Test
    @DisplayName("✅ Produits expirants - findExpiringItems")
    void testFindExpiringItems() {
        // Given
        StockItem expiringItem = createStockItem(100, 10);
        expiringItem.setExpirationDate(Timestamp.valueOf(LocalDateTime.now().plusDays(2)));

        StockItem futureItem = createStockItem(50, 5);
        futureItem.setExpirationDate(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));

        entityManager.persistAndFlush(expiringItem);
        entityManager.persistAndFlush(futureItem);

        // When
        Timestamp futureDate = Timestamp.valueOf(LocalDateTime.now().plusDays(5));
        List<StockItem> expiringItems = stockItemRepository.findExpiringItems(
                testClient.getUserId(), futureDate);

        // Then
        assertEquals(1, expiringItems.size());
        assertEquals(expiringItem.getStockItemId(), expiringItems.get(0).getStockItemId());
    }

    /**
     * Test getStockStatistics() - Statistiques
     */
    @Test
    @DisplayName("✅ Statistiques de stock - getStockStatistics")
    void testGetStockStatistics() {
        // Given
        StockItem normalItem = createStockItem(100, 10);
        StockItem lowStockItem = createStockItem(5, 20); // quantité < seuil
        StockItem expiredItem = createStockItem(30, 5);
        expiredItem.setExpirationDate(Timestamp.valueOf(LocalDateTime.now().minusDays(1)));

        entityManager.persistAndFlush(normalItem);
        entityManager.persistAndFlush(lowStockItem);
        entityManager.persistAndFlush(expiredItem);

        // When
        Object[] stats = stockItemRepository.getStockStatistics(testClient.getUserId());

        // Then
        assertNotNull(stats);
        assertEquals(1L, stats[0]); // totalProducts
        assertEquals(135L, stats[1]); // totalQuantity
        assertEquals(1L, stats[2]); // lowStockItems
        assertEquals(1L, stats[3]); // expiredItems
    }

    /**
     * Méthode utilitaire pour créer un StockItem
     */
    private StockItem createStockItem(int quantity, int reorderThreshold) {
        StockItem stockItem = new StockItem();
        stockItem.setStockItemId(UUID.randomUUID());
        stockItem.setQuantity(quantity);
        stockItem.setReorderThreshold(reorderThreshold);
        stockItem.setPurchasePrice(BigDecimal.valueOf(8.00));
        stockItem.setPurchaseDate(Timestamp.valueOf(LocalDateTime.now().minusDays(7)));
        stockItem.setLastModified(Timestamp.valueOf(LocalDateTime.now()));
        stockItem.setProduct(testProduct);
        stockItem.setClient(testClient);
        stockItem.setSupplier(testSupplier);
        return stockItem;
    }
}