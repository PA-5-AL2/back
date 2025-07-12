/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : StockItemRepositoryTest.java
 * @description : Tests unitaires pour StockItemRepository avec Mockito
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 12/07/2025
 * @package     : esgi.easisell.repository
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.repository;

import esgi.easisell.entity.StockItem;
import esgi.easisell.entity.Product;
import esgi.easisell.entity.Client;
import esgi.easisell.entity.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour StockItemRepository utilisant Mockito sans H2
 * Focus sur les méthodes personnalisées et critiques du repository
 */
@ExtendWith(MockitoExtension.class)
class StockItemRepositoryTest {

    @Mock
    private StockItemRepository stockItemRepository;

    private StockItem testStockItem;
    private Product testProduct;
    private Client testClient;
    private Supplier testSupplier;
    private UUID stockItemId;
    private UUID clientId;
    private UUID productId;
    private UUID supplierId;

    @BeforeEach
    void setUp() {
        stockItemId = UUID.randomUUID();
        clientId = UUID.randomUUID();
        productId = UUID.randomUUID();
        supplierId = UUID.randomUUID();

        testClient = new Client();
        testClient.setUserId(clientId);
        testClient.setName("Test Store");

        testProduct = new Product();
        testProduct.setProductId(productId);
        testProduct.setName("Coca-Cola 1.5L");
        testProduct.setUnitPrice(new BigDecimal("2.50"));

        testSupplier = new Supplier();
        testSupplier.setSupplierId(supplierId);
        testSupplier.setName("Coca-Cola Company");

        testStockItem = new StockItem();
        testStockItem.setStockItemId(stockItemId);
        testStockItem.setQuantity(100);
        testStockItem.setReorderThreshold(10);
        testStockItem.setPurchaseDate(Timestamp.valueOf(LocalDateTime.now().minusDays(5)));
        testStockItem.setExpirationDate(Timestamp.valueOf(LocalDateTime.now().plusDays(30)));
        testStockItem.setPurchasePrice(new BigDecimal("2.00"));
        testStockItem.setProduct(testProduct);
        testStockItem.setClient(testClient);
        testStockItem.setSupplier(testSupplier);
        testStockItem.setVersion(1L);
        testStockItem.setLastModified(Timestamp.valueOf(LocalDateTime.now()));
    }

    // ==================== TESTS MÉTHODES PERSONNALISÉES ====================

    /**
     * Test findByClientUserId - items trouvés
     */
    @Test
    @DisplayName("✅ findByClientUserId() - Items de stock trouvés")
    void testFindByClientUserIdFound() {
        // Given
        StockItem stockItem2 = createTestStockItem("Pepsi 1.5L", 50);
        List<StockItem> stockItems = Arrays.asList(testStockItem, stockItem2);
        when(stockItemRepository.findByClientUserId(clientId)).thenReturn(stockItems);

        // When
        List<StockItem> result = stockItemRepository.findByClientUserId(clientId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(testStockItem));
        assertTrue(result.contains(stockItem2));
        verify(stockItemRepository, times(1)).findByClientUserId(clientId);
    }

    /**
     * Test findByProductProductId - items par produit
     */
    @Test
    @DisplayName("✅ findByProductProductId() - Items par produit")
    void testFindByProductProductIdFound() {
        // Given
        StockItem stockItem2 = createTestStockItem("Coca-Cola 1.5L", 25); // Même produit, lot différent
        stockItem2.getProduct().setProductId(productId); // Assurer le même productId

        List<StockItem> stockItems = Arrays.asList(testStockItem, stockItem2);
        when(stockItemRepository.findByProductProductId(productId)).thenReturn(stockItems);

        // When
        List<StockItem> result = stockItemRepository.findByProductProductId(productId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        // Vérifier que tous les items ont le bon productId
        for (StockItem item : result) {
            assertEquals(productId, item.getProduct().getProductId());
        }
        verify(stockItemRepository, times(1)).findByProductProductId(productId);
    }

    /**
     * Test findLowStockItems - stock faible détecté
     */
    @Test
    @DisplayName("🔴 findLowStockItems() - Stock faible détecté")
    void testFindLowStockItems() {
        // Given
        StockItem lowStockItem = createTestStockItem("Produit en rupture", 5);
        lowStockItem.setReorderThreshold(10); // Quantité (5) <= seuil (10)

        List<StockItem> lowStockItems = Arrays.asList(lowStockItem);
        when(stockItemRepository.findLowStockItems(clientId)).thenReturn(lowStockItems);

        // When
        List<StockItem> result = stockItemRepository.findLowStockItems(clientId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        StockItem item = result.get(0);
        assertTrue(item.getQuantity() <= item.getReorderThreshold());
        verify(stockItemRepository, times(1)).findLowStockItems(clientId);
    }

    /**
     * Test findExpiringItems - produits périmés bientôt
     */
    @Test
    @DisplayName("⏰ findExpiringItems() - Produits bientôt périmés")
    void testFindExpiringItems() {
        // Given
        Timestamp futureDate = Timestamp.valueOf(LocalDateTime.now().plusDays(7));
        StockItem expiringItem = createTestStockItem("Produit périssable", 20);
        expiringItem.setExpirationDate(Timestamp.valueOf(LocalDateTime.now().plusDays(3))); // Expire dans 3 jours

        List<StockItem> expiringItems = Arrays.asList(expiringItem);
        when(stockItemRepository.findExpiringItems(clientId, futureDate)).thenReturn(expiringItems);

        // When
        List<StockItem> result = stockItemRepository.findExpiringItems(clientId, futureDate);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.get(0).getExpirationDate());
        assertTrue(result.get(0).getExpirationDate().before(futureDate));
        verify(stockItemRepository, times(1)).findExpiringItems(clientId, futureDate);
    }

    /**
     * Test getTotalStockQuantityByProduct - quantité totale calculée
     */
    @Test
    @DisplayName("📊 getTotalStockQuantityByProduct() - Quantité totale")
    void testGetTotalStockQuantityByProduct() {
        // Given
        int expectedTotal = 175; // 100 + 75 de deux lots différents
        when(stockItemRepository.getTotalStockQuantityByProduct(clientId, productId))
                .thenReturn(expectedTotal);

        // When
        int totalQuantity = stockItemRepository.getTotalStockQuantityByProduct(clientId, productId);

        // Then
        assertEquals(expectedTotal, totalQuantity);
        verify(stockItemRepository, times(1)).getTotalStockQuantityByProduct(clientId, productId);
    }

    /**
     * Test findByClientUserIdAndProductNameContainingIgnoreCase - recherche par nom
     */
    @Test
    @DisplayName("🔍 findByClientUserIdAndProductNameContainingIgnoreCase() - Recherche par nom")
    void testFindByProductNameContaining() {
        // Given
        String searchTerm = "coca";
        List<StockItem> foundItems = Arrays.asList(testStockItem);
        when(stockItemRepository.findByClientUserIdAndProductNameContainingIgnoreCase(clientId, searchTerm))
                .thenReturn(foundItems);

        // When
        List<StockItem> result = stockItemRepository.findByClientUserIdAndProductNameContainingIgnoreCase(clientId, searchTerm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getProduct().getName().toLowerCase().contains(searchTerm));
        verify(stockItemRepository, times(1))
                .findByClientUserIdAndProductNameContainingIgnoreCase(clientId, searchTerm);
    }

    /**
     * Test findBySupplierSupplierId - items par fournisseur
     */
    @Test
    @DisplayName("✅ findBySupplierSupplierId() - Items par fournisseur")
    void testFindBySupplierSupplierId() {
        // Given
        List<StockItem> supplierItems = Arrays.asList(testStockItem);
        when(stockItemRepository.findBySupplierSupplierId(supplierId)).thenReturn(supplierItems);

        // When
        List<StockItem> result = stockItemRepository.findBySupplierSupplierId(supplierId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(supplierId, result.get(0).getSupplier().getSupplierId());
        verify(stockItemRepository, times(1)).findBySupplierSupplierId(supplierId);
    }

    /**
     * Test findItemsWithoutSupplier - items sans fournisseur
     */
    @Test
    @DisplayName("❓ findItemsWithoutSupplier() - Items sans fournisseur")
    void testFindItemsWithoutSupplier() {
        // Given
        StockItem itemWithoutSupplier = createTestStockItem("Produit sans fournisseur", 30);
        itemWithoutSupplier.setSupplier(null);

        List<StockItem> itemsWithoutSupplier = Arrays.asList(itemWithoutSupplier);
        when(stockItemRepository.findItemsWithoutSupplier(clientId)).thenReturn(itemsWithoutSupplier);

        // When
        List<StockItem> result = stockItemRepository.findItemsWithoutSupplier(clientId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getSupplier());
        verify(stockItemRepository, times(1)).findItemsWithoutSupplier(clientId);
    }

    /**
     * Test countByClientId - comptage par client
     */
    @Test
    @DisplayName("📈 countByClientId() - Comptage des items")
    void testCountByClientId() {
        // Given
        long expectedCount = 25L;
        when(stockItemRepository.countByClientId(clientId)).thenReturn(expectedCount);

        // When
        long count = stockItemRepository.countByClientId(clientId);

        // Then
        assertEquals(expectedCount, count);
        verify(stockItemRepository, times(1)).countByClientId(clientId);
    }

    // ==================== TESTS MÉTHODES AVANCÉES (FIFO/GESTION MULTI-CAISSES) ====================

    /**
     * Test findByProductProductIdAndClientUserIdOrderByExpirationDateAsc - FIFO
     */
    @Test
    @DisplayName("📦 FIFO - Tri par date d'expiration")
    void testFindByProductAndClientOrderByExpirationDate() {
        // Given
        StockItem item1 = createTestStockItem("Produit FIFO 1", 50);
        item1.setExpirationDate(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));

        StockItem item2 = createTestStockItem("Produit FIFO 2", 30);
        item2.setExpirationDate(Timestamp.valueOf(LocalDateTime.now().plusDays(5))); // Expire plus tôt

        List<StockItem> fifoItems = Arrays.asList(item2, item1); // item2 en premier (expire plus tôt)
        when(stockItemRepository.findByProductProductIdAndClientUserIdOrderByExpirationDateAsc(productId, clientId))
                .thenReturn(fifoItems);

        // When
        List<StockItem> result = stockItemRepository.findByProductProductIdAndClientUserIdOrderByExpirationDateAsc(productId, clientId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        // Vérifier que le premier item expire avant le second
        assertTrue(result.get(0).getExpirationDate().before(result.get(1).getExpirationDate()));
        verify(stockItemRepository, times(1))
                .findByProductProductIdAndClientUserIdOrderByExpirationDateAsc(productId, clientId);
    }

    /**
     * Test findByIdWithPessimisticLock - verrouillage pessimiste
     */
    @Test
    @DisplayName("🔒 findByIdWithPessimisticLock() - Verrouillage pessimiste")
    void testFindByIdWithPessimisticLock() {
        // Given
        when(stockItemRepository.findByIdWithPessimisticLock(stockItemId))
                .thenReturn(Optional.of(testStockItem));

        // When
        Optional<StockItem> result = stockItemRepository.findByIdWithPessimisticLock(stockItemId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(stockItemId, result.get().getStockItemId());
        verify(stockItemRepository, times(1)).findByIdWithPessimisticLock(stockItemId);
    }

    /**
     * Test isProductAvailable - vérification disponibilité
     */
    @Test
    @DisplayName("✅ isProductAvailable() - Vérification disponibilité")
    void testIsProductAvailable() {
        // Given
        int requiredQuantity = 50;
        when(stockItemRepository.isProductAvailable(productId, clientId, requiredQuantity))
                .thenReturn(true);

        // When
        boolean isAvailable = stockItemRepository.isProductAvailable(productId, clientId, requiredQuantity);

        // Then
        assertTrue(isAvailable);
        verify(stockItemRepository, times(1)).isProductAvailable(productId, clientId, requiredQuantity);
    }

    /**
     * Test isProductAvailable - quantité insuffisante
     */
    @Test
    @DisplayName("❌ isProductAvailable() - Quantité insuffisante")
    void testIsProductNotAvailable() {
        // Given
        int requiredQuantity = 200; // Plus que le stock disponible
        when(stockItemRepository.isProductAvailable(productId, clientId, requiredQuantity))
                .thenReturn(false);

        // When
        boolean isAvailable = stockItemRepository.isProductAvailable(productId, clientId, requiredQuantity);

        // Then
        assertFalse(isAvailable);
        verify(stockItemRepository, times(1)).isProductAvailable(productId, clientId, requiredQuantity);
    }

    /**
     * Test findRecentStockMovements - mouvements récents
     */
    @Test
    @DisplayName("⏱️ findRecentStockMovements() - Mouvements récents")
    void testFindRecentStockMovements() {
        // Given
        Timestamp since = Timestamp.valueOf(LocalDateTime.now().minusHours(24));
        List<StockItem> recentMovements = Arrays.asList(testStockItem);
        when(stockItemRepository.findRecentStockMovements(clientId, since))
                .thenReturn(recentMovements);

        // When
        List<StockItem> result = stockItemRepository.findRecentStockMovements(clientId, since);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getLastModified().after(since));
        verify(stockItemRepository, times(1)).findRecentStockMovements(clientId, since);
    }

    // ==================== TESTS MÉTHODES JPA STANDARD ====================

    /**
     * Test save - sauvegarde réussie
     */
    @Test
    @DisplayName("✅ save() - Sauvegarde réussie")
    void testSaveStockItem() {
        // Given
        when(stockItemRepository.save(any(StockItem.class))).thenReturn(testStockItem);

        // When
        StockItem savedItem = stockItemRepository.save(testStockItem);

        // Then
        assertNotNull(savedItem);
        assertEquals(testStockItem.getStockItemId(), savedItem.getStockItemId());
        assertEquals(testStockItem.getQuantity(), savedItem.getQuantity());
        verify(stockItemRepository, times(1)).save(testStockItem);
    }

    /**
     * Test findById - item trouvé
     */
    @Test
    @DisplayName("✅ findById() - Item trouvé")
    void testFindByIdFound() {
        // Given
        when(stockItemRepository.findById(stockItemId)).thenReturn(Optional.of(testStockItem));

        // When
        Optional<StockItem> result = stockItemRepository.findById(stockItemId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(stockItemId, result.get().getStockItemId());
        verify(stockItemRepository, times(1)).findById(stockItemId);
    }

    // ==================== TESTS DE VALIDATION MÉTIER ====================

    /**
     * Test validation des données obligatoires
     */
    @Test
    @DisplayName("✅ Validation - Données obligatoires")
    void testRequiredStockItemData() {
        // Given
        when(stockItemRepository.save(any(StockItem.class))).thenReturn(testStockItem);

        // When
        StockItem savedItem = stockItemRepository.save(testStockItem);

        // Then
        assertNotNull(savedItem.getQuantity());
        assertNotNull(savedItem.getProduct());
        assertNotNull(savedItem.getClient());
        assertTrue(savedItem.getQuantity() >= 0);
        assertNotNull(savedItem.getVersion());
    }

    /**
     * Test gestion des versions pour Optimistic Locking
     */
    @Test
    @DisplayName("🔄 Validation - Optimistic Locking")
    void testOptimisticLocking() {
        // Given
        testStockItem.setVersion(2L);
        when(stockItemRepository.save(any(StockItem.class))).thenReturn(testStockItem);

        // When
        StockItem savedItem = stockItemRepository.save(testStockItem);

        // Then
        assertNotNull(savedItem.getVersion());
        assertEquals(2L, savedItem.getVersion());
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Méthode utilitaire pour créer un item de stock de test
     */
    private StockItem createTestStockItem(String productName, Integer quantity) {
        Product product = new Product();
        product.setProductId(UUID.randomUUID());
        product.setName(productName);
        product.setUnitPrice(new BigDecimal("3.00"));

        StockItem stockItem = new StockItem();
        stockItem.setStockItemId(UUID.randomUUID());
        stockItem.setQuantity(quantity);
        stockItem.setReorderThreshold(5);
        stockItem.setPurchaseDate(Timestamp.valueOf(LocalDateTime.now().minusDays(2)));
        stockItem.setExpirationDate(Timestamp.valueOf(LocalDateTime.now().plusDays(15)));
        stockItem.setPurchasePrice(new BigDecimal("2.50"));
        stockItem.setProduct(product);
        stockItem.setClient(testClient);
        stockItem.setSupplier(testSupplier);
        stockItem.setVersion(1L);
        stockItem.setLastModified(Timestamp.valueOf(LocalDateTime.now()));

        return stockItem;
    }
}