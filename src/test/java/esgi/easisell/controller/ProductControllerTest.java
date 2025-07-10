/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : ProductControllerTest.java
 * @description : Tests unitaires pour le contrôleur de gestion des produits
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 10/07/2025
 * @package     : esgi.easisell.controller
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.controller;

import esgi.easisell.dto.ProductDTO;
import esgi.easisell.dto.ProductResponseDTO;
import esgi.easisell.service.ProductService;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour le contrôleur de gestion des produits
 *
 * Tests des méthodes :
 * - createProduct()
 * - getAllProducts()
 * - getProductById()
 * - getProductsByClient()
 * - updateProduct()
 * - deleteProduct()
 * - calculatePrice()
 * - validateQuantity()
 * - getProductStats()
 */
@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private ProductDTO productDTO;
    private ProductResponseDTO productResponseDTO;
    private UUID productId;
    private UUID clientId;

    /**
     * Configuration initiale pour chaque test
     */
    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        clientId = UUID.randomUUID();

        // Configuration du DTO d'entrée
        productDTO = new ProductDTO();
        productDTO.setName("Pommes Gala");
        productDTO.setDescription("Pommes rouges fraîches");
        productDTO.setUnitPrice(BigDecimal.valueOf(2.50));
        productDTO.setIsSoldByWeight(true);
        productDTO.setUnitLabel("kg");
        productDTO.setBarcode("1234567890123");
        productDTO.setBrand("Vergers Bio");

        // Configuration du DTO de réponse - utilisation manuelle des setters
        productResponseDTO = createMockProductResponseDTO();
    }

    /**
     * Méthode helper pour créer un ProductResponseDTO mock
     */
    private ProductResponseDTO createMockProductResponseDTO() {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setProductId(productId);
        dto.setName("Pommes Gala");
        dto.setDescription("Pommes rouges fraîches");
        dto.setUnitPrice(BigDecimal.valueOf(2.50));
        dto.setIsSoldByWeight(true);
        dto.setUnitLabel("kg");
        dto.setBarcode("1234567890123");
        dto.setBrand("Vergers Bio");
        dto.setFormattedPrice("2,50 €/kg");
        dto.setClientId(clientId);
        return dto;
    }

    // ==================== TESTS CRUD DE BASE ====================

    /**
     * Test de création de produit réussie
     */
    @Test
    @DisplayName("createProduct() - Création réussie")
    void testCreateProductSuccess() {
        // Given
        when(productService.createProduct(any(ProductDTO.class))).thenReturn(productResponseDTO);

        // When
        ResponseEntity<?> response = productController.createProduct(productDTO);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(productResponseDTO, response.getBody());
        verify(productService, times(1)).createProduct(productDTO);
    }

    /**
     * Test de création de produit avec erreur
     */
    @Test
    @DisplayName("createProduct() - Erreur de création")
    void testCreateProductError() {
        // Given
        when(productService.createProduct(any(ProductDTO.class))).thenReturn(null);

        // When
        ResponseEntity<?> response = productController.createProduct(productDTO);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Erreur lors de la création"));
    }

    /**
     * Test de récupération de tous les produits
     */
    @Test
    @DisplayName("getAllProducts() - Récupération réussie")
    void testGetAllProductsSuccess() {
        // Given
        List<ProductResponseDTO> products = Arrays.asList(productResponseDTO);
        when(productService.getAllProducts()).thenReturn(products);

        // When
        ResponseEntity<List<ProductResponseDTO>> response = productController.getAllProducts();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(products, response.getBody());
        verify(productService, times(1)).getAllProducts();
    }

    /**
     * Test de récupération d'un produit par ID
     */
    @Test
    @DisplayName("getProductById() - Produit trouvé")
    void testGetProductByIdFound() {
        // Given
        when(productService.getProductById(productId)).thenReturn(productResponseDTO);

        // When
        ResponseEntity<?> response = productController.getProductById(productId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(productResponseDTO, response.getBody());
        verify(productService, times(1)).getProductById(productId);
    }

    /**
     * Test de récupération d'un produit par ID - non trouvé
     */
    @Test
    @DisplayName("getProductById() - Produit non trouvé")
    void testGetProductByIdNotFound() {
        // Given
        when(productService.getProductById(productId)).thenReturn(null);

        // When
        ResponseEntity<?> response = productController.getProductById(productId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(productService, times(1)).getProductById(productId);
    }

    /**
     * Test de mise à jour de produit réussie
     */
    @Test
    @DisplayName("updateProduct() - Mise à jour réussie")
    void testUpdateProductSuccess() {
        // Given
        when(productService.updateProduct(eq(productId), any(ProductDTO.class))).thenReturn(productResponseDTO);

        // When
        ResponseEntity<?> response = productController.updateProduct(productId, productDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(productResponseDTO, response.getBody());
        verify(productService, times(1)).updateProduct(productId, productDTO);
    }

    /**
     * Test de suppression de produit réussie
     */
    @Test
    @DisplayName("deleteProduct() - Suppression réussie")
    void testDeleteProductSuccess() {
        // Given
        when(productService.deleteProduct(productId)).thenReturn(true);

        // When
        ResponseEntity<?> response = productController.deleteProduct(productId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("supprimé avec succès"));
        verify(productService, times(1)).deleteProduct(productId);
    }

    /**
     * Test de suppression de produit - non trouvé
     */
    @Test
    @DisplayName("deleteProduct() - Produit non trouvé")
    void testDeleteProductNotFound() {
        // Given
        when(productService.deleteProduct(productId)).thenReturn(false);

        // When
        ResponseEntity<?> response = productController.deleteProduct(productId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(productService, times(1)).deleteProduct(productId);
    }

    // ==================== TESTS DE RECHERCHE ====================

    /**
     * Test de récupération des produits par client
     */
    @Test
    @DisplayName("getProductsByClient() - Récupération réussie")
    void testGetProductsByClientSuccess() {
        // Given
        List<ProductResponseDTO> products = Arrays.asList(productResponseDTO);
        when(productService.getProductsByClient(clientId)).thenReturn(products);

        // When
        ResponseEntity<List<ProductResponseDTO>> response = productController.getProductsByClient(clientId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(products, response.getBody());
        verify(productService, times(1)).getProductsByClient(clientId);
    }

    /**
     * Test de recherche par nom
     */
    @Test
    @DisplayName("searchProductsByName() - Recherche réussie")
    void testSearchProductsByNameSuccess() {
        // Given
        String searchName = "Pommes";
        List<ProductResponseDTO> products = Arrays.asList(productResponseDTO);
        when(productService.searchProductsByName(clientId, searchName)).thenReturn(products);

        // When
        ResponseEntity<List<ProductResponseDTO>> response = productController.searchProductsByName(clientId, searchName);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(products, response.getBody());
        verify(productService, times(1)).searchProductsByName(clientId, searchName);
    }

    /**
     * Test de recherche par code-barres
     */
    @Test
    @DisplayName("findProductByBarcode() - Produit trouvé")
    void testFindProductByBarcodeFound() {
        // Given
        String barcode = "1234567890123";
        when(productService.findProductByBarcode(clientId, barcode)).thenReturn(productResponseDTO);

        // When
        ResponseEntity<?> response = productController.findProductByBarcode(clientId, barcode);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(productResponseDTO, response.getBody());
        verify(productService, times(1)).findProductByBarcode(clientId, barcode);
    }

    // ==================== TESTS DES NOUVEAUX ENDPOINTS ====================

    /**
     * Test de récupération des produits vendus au poids
     */
    @Test
    @DisplayName("getProductsByWeight() - Produits au poids")
    void testGetProductsByWeightSuccess() {
        // Given
        List<ProductResponseDTO> weightProducts = Arrays.asList(productResponseDTO);
        when(productService.getProductsByWeight(clientId)).thenReturn(weightProducts);

        // When
        ResponseEntity<List<ProductResponseDTO>> response = productController.getProductsByWeight(clientId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(weightProducts, response.getBody());
        verify(productService, times(1)).getProductsByWeight(clientId);
    }

    /**
     * Test de récupération des produits vendus à la pièce
     */
    @Test
    @DisplayName("getProductsByPiece() - Produits à la pièce")
    void testGetProductsByPieceSuccess() {
        // Given
        ProductResponseDTO pieceProduct = createMockPieceProductDTO();
        pieceProduct.setName("Pain de mie");

        List<ProductResponseDTO> pieceProducts = Arrays.asList(pieceProduct);
        when(productService.getProductsByPiece(clientId)).thenReturn(pieceProducts);

        // When
        ResponseEntity<List<ProductResponseDTO>> response = productController.getProductsByPiece(clientId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pieceProducts, response.getBody());
        verify(productService, times(1)).getProductsByPiece(clientId);
    }

    // ==================== TESTS DE CALCUL DE PRIX ====================

    /**
     * Test de calcul de prix pour produit au poids
     */
    @Test
    @DisplayName("calculatePrice() - Calcul pour produit au poids")
    void testCalculatePriceForWeightProduct() {
        // Given
        BigDecimal quantity = BigDecimal.valueOf(2.350);
        when(productService.getProductById(productId)).thenReturn(productResponseDTO);

        // When
        ResponseEntity<?> response = productController.calculatePrice(productId, quantity);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) responseBody.get("success"));
        assertEquals(BigDecimal.valueOf(5.88), responseBody.get("totalPrice"));
    }

    /**
     * Test de calcul de prix pour produit à la pièce avec quantité décimale (erreur)
     */
    @Test
    @DisplayName("calculatePrice() - Erreur quantité décimale pour produit à la pièce")
    void testCalculatePriceForPieceProductWithDecimal() {
        // Given
        BigDecimal quantity = BigDecimal.valueOf(2.5);
        ProductResponseDTO pieceProduct = createMockPieceProductDTO();

        when(productService.getProductById(productId)).thenReturn(pieceProduct);

        // When
        ResponseEntity<?> response = productController.calculatePrice(productId, quantity);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertTrue(responseBody.get("error").toString().contains("pièce"));
    }

    /**
     * Méthode helper pour créer un ProductResponseDTO de type pièce
     */
    private ProductResponseDTO createMockPieceProductDTO() {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setProductId(productId);
        dto.setName("Pain de mie");
        dto.setUnitPrice(BigDecimal.valueOf(1.50));
        dto.setIsSoldByWeight(false);
        dto.setUnitLabel("pièce");
        dto.setClientId(clientId);
        return dto;
    }

    /**
     * Test de calcul de prix avec quantité négative
     */
    @Test
    @DisplayName("calculatePrice() - Erreur quantité négative")
    void testCalculatePriceWithNegativeQuantity() {
        // Given
        BigDecimal quantity = BigDecimal.valueOf(-1.0);
        when(productService.getProductById(productId)).thenReturn(productResponseDTO);

        // When
        ResponseEntity<?> response = productController.calculatePrice(productId, quantity);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertTrue(responseBody.get("error").toString().contains("positive"));
    }

    /**
     * Test de calcul de prix pour produit inexistant
     */
    @Test
    @DisplayName("calculatePrice() - Produit inexistant")
    void testCalculatePriceForNonExistentProduct() {
        // Given
        BigDecimal quantity = BigDecimal.valueOf(1.0);
        when(productService.getProductById(productId)).thenReturn(null);

        // When
        ResponseEntity<?> response = productController.calculatePrice(productId, quantity);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ==================== TESTS DE VALIDATION DE QUANTITÉ ====================

    /**
     * Test de validation de quantité valide pour produit au poids
     */
    @Test
    @DisplayName("validateQuantity() - Quantité valide pour produit au poids")
    void testValidateQuantityValidForWeightProduct() {
        // Given
        BigDecimal quantity = BigDecimal.valueOf(2.5);
        when(productService.getProductById(productId)).thenReturn(productResponseDTO);

        // When
        ResponseEntity<?> response = productController.validateQuantity(productId, quantity);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) responseBody.get("isValid"));
        assertEquals("Quantité valide", responseBody.get("message"));
    }

    /**
     * Test de validation de quantité invalide pour produit à la pièce
     */
    @Test
    @DisplayName("validateQuantity() - Quantité invalide pour produit à la pièce")
    void testValidateQuantityInvalidForPieceProduct() {
        // Given
        BigDecimal quantity = BigDecimal.valueOf(2.5);
        ProductResponseDTO pieceProduct = createMockPieceProductDTO();
        pieceProduct.setName("Pain de mie");

        when(productService.getProductById(productId)).thenReturn(pieceProduct);

        // When
        ResponseEntity<?> response = productController.validateQuantity(productId, quantity);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("isValid"));
        assertTrue(responseBody.get("message").toString().contains("pièce"));
    }

    // ==================== TESTS DE STATISTIQUES ====================

    /**
     * Test de génération de statistiques
     */
    @Test
    @DisplayName("getProductStats() - Génération de statistiques")
    void testGetProductStatsSuccess() {
        // Given
        ProductResponseDTO pieceProduct = createMockPieceProductDTO();
        pieceProduct.setName("Pain");
        pieceProduct.setUnitPrice(BigDecimal.valueOf(1.50));

        List<ProductResponseDTO> products = Arrays.asList(productResponseDTO, pieceProduct);
        when(productService.getProductsByClient(clientId)).thenReturn(products);

        // When
        ResponseEntity<?> response = productController.getProductStats(clientId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals(2L, responseBody.get("totalProducts"));
        assertEquals(1L, responseBody.get("weightProducts"));
        assertEquals(1L, responseBody.get("pieceProducts"));
    }

    // ==================== TESTS DE GROUPEMENT PAR UNITÉ ====================

    /**
     * Test de groupement des produits par unité
     */
    @Test
    @DisplayName("getProductsByUnit() - Groupement par unité")
    void testGetProductsByUnitSuccess() {
        // Given
        ProductResponseDTO pieceProduct = createMockPieceProductDTO();
        pieceProduct.setName("Pain");

        List<ProductResponseDTO> products = Arrays.asList(productResponseDTO, pieceProduct);
        when(productService.getProductsByClient(clientId)).thenReturn(products);

        // When
        ResponseEntity<Map<String, List<ProductResponseDTO>>> response = productController.getProductsByUnit(clientId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, List<ProductResponseDTO>> groupedProducts = response.getBody();
        assertTrue(groupedProducts.containsKey("kg"));
        assertTrue(groupedProducts.containsKey("pièce"));
    }

    // ==================== TESTS DE COMPTAGE ====================

    /**
     * Test de comptage des produits par client
     */
    @Test
    @DisplayName("countProductsByClient() - Comptage réussi")
    void testCountProductsByClientSuccess() {
        // Given
        long expectedCount = 5L;
        when(productService.countProductsByClient(clientId)).thenReturn(expectedCount);

        // When
        ResponseEntity<Long> response = productController.countProductsByClient(clientId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedCount, response.getBody());
        verify(productService, times(1)).countProductsByClient(clientId);
    }

    // ==================== TESTS DE SUGGESTION DE PRIX ====================

    /**
     * Test de suggestion de prix avec produits similaires
     */
    @Test
    @DisplayName("suggestPrice() - Suggestion avec produits similaires")
    void testSuggestPriceWithSimilarProducts() {
        // Given
        String searchName = "Pommes";
        String unitLabel = "kg";

        ProductResponseDTO similarProduct = createMockProductResponseDTO();
        similarProduct.setName("Pommes Rouges");
        similarProduct.setUnitPrice(BigDecimal.valueOf(3.00));
        similarProduct.setUnitLabel("kg");

        List<ProductResponseDTO> products = Arrays.asList(productResponseDTO, similarProduct);
        when(productService.getProductsByClient(clientId)).thenReturn(products);

        // When
        ResponseEntity<?> response = productController.suggestPrice(clientId, searchName, unitLabel);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue(responseBody.get("suggestion").toString().contains("produits similaires"));
        assertNotNull(responseBody.get("recommendedPrice"));
    }

    /**
     * Test de suggestion de prix sans produits similaires
     */
    @Test
    @DisplayName("suggestPrice() - Aucun produit similaire")
    void testSuggestPriceWithNoSimilarProducts() {
        // Given
        String searchName = "Produit Inexistant";
        when(productService.getProductsByClient(clientId)).thenReturn(Arrays.asList(productResponseDTO));

        // When
        ResponseEntity<?> response = productController.suggestPrice(clientId, searchName, null);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue(responseBody.get("suggestion").toString().contains("Aucun produit similaire"));
        assertEquals(0.00, responseBody.get("recommendedPrice"));
    }

    // ==================== TESTS DE CONFIGURATION ====================

    /**
     * Test de configuration des mocks
     */
    @Test
    @DisplayName("Configuration des mocks")
    void testMockConfiguration() {
        // Vérifier que les mocks sont correctement injectés
        assertNotNull(productController);
        assertNotNull(productService);
    }
}