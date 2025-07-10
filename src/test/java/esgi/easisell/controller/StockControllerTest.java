/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : StockControllerTest.java
 * @description : Tests unitaires pour le contrôleur de gestion du stock
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 10/07/2025
 * @package     : esgi.easisell.controller
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.controller;

import esgi.easisell.dto.CreateStockItemDTO;
import esgi.easisell.dto.StockItemResponseDTO;
import esgi.easisell.dto.UpdateStockItemDTO;
import esgi.easisell.entity.StockItem;
import esgi.easisell.entity.Product;
import esgi.easisell.entity.Client;
import esgi.easisell.service.StockItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour le contrôleur de gestion du stock
 *
 * Tests des méthodes :
 * - createStockItem()
 * - getStockItemsByClient()
 * - getStockItemById()
 * - updateStockItem()
 * - adjustStockQuantity()
 * - deleteStockItem()
 * - getLowStockItems()
 * - getExpiringItems()
 */
@ExtendWith(MockitoExtension.class)
class StockControllerTest {

    @Mock
    private StockItemService stockItemService;

    @InjectMocks
    private StockController stockController;

    private UUID stockItemId;
    private UUID clientId;
    private UUID productId;
    private StockItem mockStockItem;
    private CreateStockItemDTO createStockItemDTO;
    private UpdateStockItemDTO updateStockItemDTO;

    /**
     * Configuration initiale pour chaque test
     */
    @BeforeEach
    void setUp() {
        stockItemId = UUID.randomUUID();
        clientId = UUID.randomUUID();
        productId = UUID.randomUUID();

        // Configuration du StockItem mock
        mockStockItem = createMockStockItem();

        // Configuration des DTOs
        createStockItemDTO = new CreateStockItemDTO();
        updateStockItemDTO = new UpdateStockItemDTO();
    }

    /**
     * Méthode helper pour créer un StockItem mock
     */
    private StockItem createMockStockItem() {
        StockItem stockItem = new StockItem();
        stockItem.setStockItemId(stockItemId);
        stockItem.setQuantity(100);
        stockItem.setReorderThreshold(10);
        stockItem.setPurchasePrice(BigDecimal.valueOf(25.50));

        // Mock Product
        Product product = new Product();
        product.setProductId(productId);
        product.setName("Test Product");
        product.setBarcode("1234567890123");
        stockItem.setProduct(product);

        // Mock Client
        Client client = new Client();
        client.setUserId(clientId);
        stockItem.setClient(client);

        return stockItem;
    }

    // ==================== TESTS CRUD DE BASE ====================

    /**
     * Test de création d'item de stock réussie
     */
    @Test
    @DisplayName("createStockItem() - Création réussie")
    void testCreateStockItemSuccess() {
        // Given
        when(stockItemService.createStockItem(any(CreateStockItemDTO.class))).thenReturn(mockStockItem);

        // When
        ResponseEntity<?> response = stockController.createStockItem(createStockItemDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof StockItemResponseDTO);
        verify(stockItemService, times(1)).createStockItem(createStockItemDTO);
    }

    /**
     * Test de création d'item de stock avec erreur
     */
    @Test
    @DisplayName("createStockItem() - Erreur de création")
    void testCreateStockItemError() {
        // Given
        when(stockItemService.createStockItem(any(CreateStockItemDTO.class)))
                .thenThrow(new RuntimeException("Erreur de création"));

        // When
        ResponseEntity<?> response = stockController.createStockItem(createStockItemDTO);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue(responseBody.get("error").toString().contains("Erreur de création"));
    }

    /**
     * Test de récupération des items de stock par client
     */
    @Test
    @DisplayName("getStockItemsByClient() - Récupération réussie")
    void testGetStockItemsByClientSuccess() {
        // Given
        List<StockItem> stockItems = Arrays.asList(mockStockItem);
        when(stockItemService.getStockItemsByClient(clientId)).thenReturn(stockItems);

        // When
        ResponseEntity<List<StockItemResponseDTO>> response = stockController.getStockItemsByClient(clientId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(stockItemService, times(1)).getStockItemsByClient(clientId);
    }

    /**
     * Test de récupération d'un item de stock par ID
     */
    @Test
    @DisplayName("getStockItemById() - Item trouvé")
    void testGetStockItemByIdFound() {
        // Given
        when(stockItemService.getStockItemById(stockItemId)).thenReturn(Optional.of(mockStockItem));

        // When
        ResponseEntity<?> response = stockController.getStockItemById(stockItemId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof StockItemResponseDTO);
        verify(stockItemService, times(1)).getStockItemById(stockItemId);
    }

    /**
     * Test de récupération d'un item de stock par ID - non trouvé
     */
    @Test
    @DisplayName("getStockItemById() - Item non trouvé")
    void testGetStockItemByIdNotFound() {
        // Given
        when(stockItemService.getStockItemById(stockItemId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = stockController.getStockItemById(stockItemId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(stockItemService, times(1)).getStockItemById(stockItemId);
    }

    /**
     * Test de mise à jour d'item de stock réussie
     */
    @Test
    @DisplayName("updateStockItem() - Mise à jour réussie")
    void testUpdateStockItemSuccess() {
        // Given
        when(stockItemService.updateStockItem(eq(stockItemId), any(UpdateStockItemDTO.class)))
                .thenReturn(Optional.of(mockStockItem));

        // When
        ResponseEntity<?> response = stockController.updateStockItem(stockItemId, updateStockItemDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof StockItemResponseDTO);
        verify(stockItemService, times(1)).updateStockItem(stockItemId, updateStockItemDTO);
    }

    /**
     * Test de suppression d'item de stock réussie
     */
    @Test
    @DisplayName("deleteStockItem() - Suppression réussie")
    void testDeleteStockItemSuccess() {
        // Given
        when(stockItemService.deleteStockItem(stockItemId)).thenReturn(true);

        // When
        ResponseEntity<?> response = stockController.deleteStockItem(stockItemId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue(responseBody.get("message").toString().contains("supprimé avec succès"));
        verify(stockItemService, times(1)).deleteStockItem(stockItemId);
    }

    // ==================== TESTS DE RECHERCHE ====================

    /**
     * Test de récupération des items de stock par produit
     */
    @Test
    @DisplayName("getStockItemsByProduct() - Récupération réussie")
    void testGetStockItemsByProductSuccess() {
        // Given
        List<StockItem> stockItems = Arrays.asList(mockStockItem);
        when(stockItemService.getStockItemsByProduct(productId)).thenReturn(stockItems);

        // When
        ResponseEntity<List<StockItemResponseDTO>> response = stockController.getStockItemsByProduct(productId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(stockItemService, times(1)).getStockItemsByProduct(productId);
    }

    /**
     * Test de recherche par nom de produit
     */
    @Test
    @DisplayName("searchStockItems() - Recherche réussie")
    void testSearchStockItemsSuccess() {
        // Given
        String productName = "Test Product";
        List<StockItem> stockItems = Arrays.asList(mockStockItem);
        when(stockItemService.searchStockItemsByProductName(clientId, productName)).thenReturn(stockItems);

        // When
        ResponseEntity<List<StockItemResponseDTO>> response = stockController.searchStockItems(clientId, productName);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(stockItemService, times(1)).searchStockItemsByProductName(clientId, productName);
    }

    /**
     * Test de recherche par code-barres
     */
    @Test
    @DisplayName("findStockByBarcode() - Recherche réussie")
    void testFindStockByBarcodeSuccess() {
        // Given
        String barcode = "1234567890123";
        List<StockItem> stockItems = Arrays.asList(mockStockItem);
        when(stockItemService.findStockByProductBarcode(clientId, barcode)).thenReturn(stockItems);

        // When
        ResponseEntity<List<StockItemResponseDTO>> response = stockController.findStockByBarcode(clientId, barcode);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(stockItemService, times(1)).findStockByProductBarcode(clientId, barcode);
    }

    // ==================== TESTS DE GESTION DES QUANTITÉS ====================

    /**
     * Test d'ajustement de quantité réussi
     */
    @Test
    @DisplayName("adjustStockQuantity() - Ajustement réussi")
    void testAdjustStockQuantitySuccess() {
        // Given
        int quantityChange = 50;
        when(stockItemService.adjustStockQuantity(stockItemId, quantityChange)).thenReturn(true);

        // When
        ResponseEntity<?> response = stockController.adjustStockQuantity(stockItemId, quantityChange);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue(responseBody.get("message").toString().contains("ajustée avec succès"));
        verify(stockItemService, times(1)).adjustStockQuantity(stockItemId, quantityChange);
    }

    /**
     * Test d'ajustement de quantité avec échec
     */
    @Test
    @DisplayName("adjustStockQuantity() - Ajustement échoué")
    void testAdjustStockQuantityFailure() {
        // Given
        int quantityChange = -200; // Quantité négative qui pourrait causer un échec
        when(stockItemService.adjustStockQuantity(stockItemId, quantityChange)).thenReturn(false);

        // When
        ResponseEntity<?> response = stockController.adjustStockQuantity(stockItemId, quantityChange);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue(responseBody.get("error").toString().contains("Impossible d'ajuster"));
    }

    /**
     * Test de récupération de la quantité totale
     */
    @Test
    @DisplayName("getTotalStockQuantity() - Récupération réussie")
    void testGetTotalStockQuantitySuccess() {
        // Given
        int totalQuantity = 150;
        when(stockItemService.getTotalStockQuantityByProduct(clientId, productId)).thenReturn(totalQuantity);

        // When
        ResponseEntity<Map<String, Integer>> response = stockController.getTotalStockQuantity(clientId, productId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(totalQuantity, response.getBody().get("totalQuantity"));
        verify(stockItemService, times(1)).getTotalStockQuantityByProduct(clientId, productId);
    }

    // ==================== TESTS D'ALERTES ====================

    /**
     * Test de récupération des items en stock faible
     */
    @Test
    @DisplayName("getLowStockItems() - Récupération réussie")
    void testGetLowStockItemsSuccess() {
        // Given
        List<StockItem> lowStockItems = Arrays.asList(mockStockItem);
        when(stockItemService.getLowStockItems(clientId)).thenReturn(lowStockItems);

        // When
        ResponseEntity<List<StockItemResponseDTO>> response = stockController.getLowStockItems(clientId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(stockItemService, times(1)).getLowStockItems(clientId);
    }

    /**
     * Test de récupération des items qui expirent bientôt
     */
    @Test
    @DisplayName("getExpiringItems() - Récupération réussie")
    void testGetExpiringItemsSuccess() {
        // Given
        int daysUntilExpiration = 7;
        List<StockItem> expiringItems = Arrays.asList(mockStockItem);
        when(stockItemService.getExpiringItems(clientId, daysUntilExpiration)).thenReturn(expiringItems);

        // When
        ResponseEntity<List<StockItemResponseDTO>> response = stockController.getExpiringItems(clientId, daysUntilExpiration);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(stockItemService, times(1)).getExpiringItems(clientId, daysUntilExpiration);
    }

    /**
     * Test de configuration du seuil d'alerte
     */
    @Test
    @DisplayName("setAlertThreshold() - Configuration réussie")
    void testSetAlertThresholdSuccess() {
        // Given
        int threshold = 20;
        when(stockItemService.updateReorderThreshold(stockItemId, threshold)).thenReturn(true);

        // When
        ResponseEntity<?> response = stockController.setAlertThreshold(stockItemId, threshold);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue(responseBody.get("message").toString().contains("configuré avec succès"));
        assertEquals(threshold, responseBody.get("threshold"));
        verify(stockItemService, times(1)).updateReorderThreshold(stockItemId, threshold);
    }

    /**
     * Test du tableau de bord des alertes
     */
    @Test
    @DisplayName("getAlertsDashboard() - Récupération réussie")
    void testGetAlertsDashboardSuccess() {
        // Given
        List<StockItem> lowStockItems = Arrays.asList(mockStockItem);
        List<StockItem> expiringItems = Arrays.asList(mockStockItem);
        List<StockItem> outOfStockItems = Arrays.asList();

        when(stockItemService.getLowStockItems(clientId)).thenReturn(lowStockItems);
        when(stockItemService.getExpiringItems(clientId, 7)).thenReturn(expiringItems);
        when(stockItemService.getOutOfStockItems(clientId)).thenReturn(outOfStockItems);

        // When
        ResponseEntity<?> response = stockController.getAlertsDashboard(clientId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue(responseBody.containsKey("lowStock"));
        assertTrue(responseBody.containsKey("expiringSoon"));
        assertTrue(responseBody.containsKey("outOfStock"));
        assertTrue(responseBody.containsKey("summary"));

        verify(stockItemService, times(1)).getLowStockItems(clientId);
        verify(stockItemService, times(1)).getExpiringItems(clientId, 7);
        verify(stockItemService, times(1)).getOutOfStockItems(clientId);
    }

    // ==================== TESTS AVEC VALEURS PAR DÉFAUT ====================

    /**
     * Test d'expiration avec valeur par défaut
     */
    @Test
    @DisplayName("getExpiringItems() - Valeur par défaut")
    void testGetExpiringItemsDefaultDays() {
        // Given
        int defaultDays = 7; // Valeur par défaut
        List<StockItem> expiringItems = Arrays.asList(mockStockItem);
        when(stockItemService.getExpiringItems(clientId, defaultDays)).thenReturn(expiringItems);

        // When - Simuler un appel sans spécifier le paramètre days
        ResponseEntity<List<StockItemResponseDTO>> response = stockController.getExpiringItems(clientId, defaultDays);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(stockItemService, times(1)).getExpiringItems(clientId, defaultDays);
    }

    // ==================== TESTS DE CONFIGURATION ====================

    /**
     * Test de configuration des mocks
     */
    @Test
    @DisplayName("Configuration des mocks")
    void testMockConfiguration() {
        // Vérifier que les mocks sont correctement injectés
        assertNotNull(stockController);
        assertNotNull(stockItemService);
    }
}