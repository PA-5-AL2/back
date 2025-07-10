/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : SaleControllerTest.java
 * @description : Tests unitaires pour le contrôleur de gestion des ventes
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 10/07/2025
 * @package     : esgi.easisell.controller
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.controller;

import esgi.easisell.dto.*;
import esgi.easisell.service.SaleService;
import esgi.easisell.service.OptimisticStockService;
import esgi.easisell.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour le contrôleur de gestion des ventes
 * Tests des méthodes :
 * - createNewSaleForCurrentClient()
 * - createNewSale()
 * - scanProduct()
 * - addProductById()
 * - updateItemQuantity()
 * - removeItem()
 * - processPayment()
 * - getSaleDetails()
 * - calculateTotal()
 * - getSalesByClient()
 */
@ExtendWith(MockitoExtension.class)
class SaleControllerTest {

    @Mock
    private SaleService saleService;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private OptimisticStockService optimisticStockService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private SaleController saleController;

    private UUID saleId;
    private UUID clientId;
    private UUID productId;
    private UUID saleItemId;
    private SaleResponseDTO saleResponseDTO;
    private SaleItemResponseDTO saleItemResponseDTO;

    /**
     * Configuration initiale pour chaque test
     */
    @BeforeEach
    void setUp() {
        saleId = UUID.randomUUID();
        clientId = UUID.randomUUID();
        productId = UUID.randomUUID();
        saleItemId = UUID.randomUUID();

        // Configuration du DTO de vente - selon la vraie structure
        saleResponseDTO = new SaleResponseDTO();
        saleResponseDTO.setSaleId(saleId);
        saleResponseDTO.setClientId(clientId);
        saleResponseDTO.setClientName("Test Store");
        saleResponseDTO.setClientUsername("test@store.com");
        saleResponseDTO.setTotalAmount(BigDecimal.valueOf(150.00));
        saleResponseDTO.setIsDeferred(false);
        saleResponseDTO.setIsPaid(false);
        saleResponseDTO.setItemCount(2);
        saleResponseDTO.setSaleTimestamp(new java.sql.Timestamp(System.currentTimeMillis()));

        // Configuration du DTO d'article de vente
        saleItemResponseDTO = new SaleItemResponseDTO();
        saleItemResponseDTO.setSaleItemId(saleItemId);
        saleItemResponseDTO.setProductId(productId);
        saleItemResponseDTO.setQuantitySold(BigDecimal.valueOf(2));
        saleItemResponseDTO.setPriceAtSale(BigDecimal.valueOf(5.00));
    }

    // ==================== TESTS CRÉATION DE VENTE ====================

    /**
     * Test de création de vente pour le client connecté
     */
    @Test
    @DisplayName("createNewSaleForCurrentClient() - Création réussie")
    void testCreateNewSaleForCurrentClientSuccess() {
        // Given
        String userId = clientId.toString();
        when(securityUtils.getCurrentUserId(request)).thenReturn(userId);
        when(saleService.createNewSale(clientId)).thenReturn(saleResponseDTO);

        // When
        ResponseEntity<?> response = saleController.createNewSaleForCurrentClient(request);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(saleResponseDTO, response.getBody());
        verify(saleService, times(1)).createNewSale(clientId);
    }

    /**
     * Test de création de vente - utilisateur non authentifié
     */
    @Test
    @DisplayName("createNewSaleForCurrentClient() - Utilisateur non authentifié")
    void testCreateNewSaleForCurrentClientUnauthorized() {
        // Given
        when(securityUtils.getCurrentUserId(request)).thenReturn(null);

        // When
        ResponseEntity<?> response = saleController.createNewSaleForCurrentClient(request);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertInstanceOf(Map.class, response.getBody());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue(responseBody.get("error").toString().contains("non authentifié"));
        verify(saleService, never()).createNewSale(any());
    }

    /**
     * Test de création de vente avec ID client spécifique
     */
    @Test
    @DisplayName("createNewSale() - Création réussie")
    void testCreateNewSaleSuccess() {
        // Given
        when(securityUtils.canAccessClientData(clientId, request)).thenReturn(true);
        when(saleService.createNewSale(clientId)).thenReturn(saleResponseDTO);

        // When
        ResponseEntity<?> response = saleController.createNewSale(clientId, request);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(saleResponseDTO, response.getBody());
        verify(saleService, times(1)).createNewSale(clientId);
    }

    /**
     * Test de création de vente - accès non autorisé
     */
    @Test
    @DisplayName("createNewSale() - Accès non autorisé")
    void testCreateNewSaleForbidden() {
        // Given
        when(securityUtils.canAccessClientData(clientId, request)).thenReturn(false);

        // When
        ResponseEntity<?> response = saleController.createNewSale(clientId, request);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertInstanceOf(Map.class, response.getBody());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue(responseBody.get("error").toString().contains("Accès non autorisé"));
        verify(saleService, never()).createNewSale(any());
    }

    // ==================== TESTS GESTION DES ARTICLES ====================

    /**
     * Test de scan de produit réussi
     */
    @Test
    @DisplayName("scanProduct() - Scan réussi")
    void testScanProductSuccess() {
        // Given
        String barcode = "1234567890123";
        BigDecimal quantity = BigDecimal.valueOf(2);

        // Mock des dépendances pour que canAccessSale() retourne true
        when(securityUtils.getCurrentUserId(request)).thenReturn(clientId.toString());
        when(securityUtils.isCurrentUserAdmin()).thenReturn(false);
        when(saleService.canAccessSale(saleId, clientId)).thenReturn(true);
        when(saleService.addProductToSale(saleId, barcode, quantity)).thenReturn(saleItemResponseDTO);

        // When
        ResponseEntity<?> response = saleController.scanProduct(saleId, barcode, quantity, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(saleItemResponseDTO, response.getBody());
        verify(saleService, times(1)).addProductToSale(saleId, barcode, quantity);
    }

    /**
     * Test de scan de produit - accès non autorisé
     */
    @Test
    @DisplayName("scanProduct() - Accès non autorisé")
    void testScanProductForbidden() {
        // Given
        String barcode = "1234567890123";
        BigDecimal quantity = BigDecimal.valueOf(1);

        // Mock pour que canAccessSale() retourne false
        when(securityUtils.getCurrentUserId(request)).thenReturn(null);

        // When
        ResponseEntity<?> response = saleController.scanProduct(saleId, barcode, quantity, request);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(saleService, never()).addProductToSale(any(), any(), any());
    }

    /**
     * Test de mise à jour de quantité d'article
     */
    @Test
    @DisplayName("updateItemQuantity() - Mise à jour réussie")
    void testUpdateItemQuantitySuccess() {
        // Given
        BigDecimal newQuantity = BigDecimal.valueOf(5);
        when(saleService.updateItemQuantity(saleItemId, newQuantity)).thenReturn(saleItemResponseDTO);

        // When
        ResponseEntity<?> response = saleController.updateItemQuantity(saleItemId, newQuantity, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(saleItemResponseDTO, response.getBody());
        verify(saleService, times(1)).updateItemQuantity(saleItemId, newQuantity);
    }

    /**
     * Test de suppression d'article
     */
    @Test
    @DisplayName("removeItem() - Suppression réussie")
    void testRemoveItemSuccess() {
        // Given
        doNothing().when(saleService).removeItemFromSale(saleItemId);

        // When
        ResponseEntity<?> response = saleController.removeItem(saleItemId, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(Map.class, response.getBody());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue(responseBody.get("message").toString().contains("supprimé avec succès"));
        verify(saleService, times(1)).removeItemFromSale(saleItemId);
    }

    // ==================== TESTS PAIEMENT ====================

    /**
     * Test de traitement de paiement réussi
     */
    @Test
    @DisplayName("processPayment() - Paiement réussi")
    void testProcessPaymentSuccess() {
        // Given
        String paymentType = "CASH";
        BigDecimal amountReceived = BigDecimal.valueOf(100.00);
        String currency = "EUR";

        PaymentResultDTO paymentResult = new PaymentResultDTO();
        paymentResult.setAmountPaid(amountReceived);

        // Mock pour que canAccessSale() retourne true
        when(securityUtils.getCurrentUserId(request)).thenReturn(clientId.toString());
        when(securityUtils.isCurrentUserAdmin()).thenReturn(false);
        when(saleService.canAccessSale(saleId, clientId)).thenReturn(true);
        when(saleService.processPayment(saleId, paymentType, amountReceived, currency))
                .thenReturn(paymentResult);

        // When
        ResponseEntity<?> response = saleController.processPayment(saleId, paymentType, amountReceived, currency, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(paymentResult, response.getBody());
        verify(saleService, times(1)).processPayment(saleId, paymentType, amountReceived, currency);
    }

    /**
     * Test de traitement de paiement - accès non autorisé
     */
    @Test
    @DisplayName("processPayment() - Accès non autorisé")
    void testProcessPaymentForbidden() {
        // Given
        String paymentType = "CASH";
        BigDecimal amountReceived = BigDecimal.valueOf(100.00);
        String currency = "EUR";

        // Mock pour que canAccessSale() retourne false
        when(securityUtils.getCurrentUserId(request)).thenReturn(null);

        // When
        ResponseEntity<?> response = saleController.processPayment(saleId, paymentType, amountReceived, currency, request);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(saleService, never()).processPayment(any(), any(), any(), any());
    }

    // ==================== TESTS CONSULTATION ====================

    /**
     * Test de récupération des détails de vente
     */
    @Test
    @DisplayName("getSaleDetails() - Récupération réussie")
    void testGetSaleDetailsSuccess() {
        // Given
        SaleDetailsDTO saleDetails = new SaleDetailsDTO();
        saleDetails.setTotalAmount(BigDecimal.valueOf(150.00));

        // Mock pour que canAccessSale() retourne true
        when(securityUtils.getCurrentUserId(request)).thenReturn(clientId.toString());
        when(securityUtils.isCurrentUserAdmin()).thenReturn(false);
        when(saleService.canAccessSale(saleId, clientId)).thenReturn(true);
        when(saleService.getSaleDetails(saleId)).thenReturn(saleDetails);

        // When
        ResponseEntity<?> response = saleController.getSaleDetails(saleId, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(saleDetails, response.getBody());
        verify(saleService, times(1)).getSaleDetails(saleId);
    }

    /**
     * Test de calcul du total de vente
     */
    @Test
    @DisplayName("calculateTotal() - Calcul réussi")
    void testCalculateTotalSuccess() {
        // Given
        SaleTotalDTO saleTotal = SaleTotalDTO.builder()
                .subtotal(BigDecimal.valueOf(130.00))
                .taxAmount(BigDecimal.valueOf(20.00))
                .discountAmount(BigDecimal.valueOf(0.00))
                .totalAmount(BigDecimal.valueOf(150.00))
                .currency("EUR")
                .build();

        // Mock pour que canAccessSale() retourne true
        when(securityUtils.getCurrentUserId(request)).thenReturn(clientId.toString());
        when(securityUtils.isCurrentUserAdmin()).thenReturn(false);
        when(saleService.canAccessSale(saleId, clientId)).thenReturn(true);
        when(saleService.calculateSaleTotal(saleId)).thenReturn(saleTotal);

        // When
        ResponseEntity<?> response = saleController.calculateTotal(saleId, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(saleTotal, response.getBody());
        verify(saleService, times(1)).calculateSaleTotal(saleId);
    }

    /**
     * Test de récupération des ventes par client
     */
    @Test
    @DisplayName("getSalesByClient() - Récupération réussie")
    void testGetSalesByClientSuccess() {
        // Given
        int page = 0;
        int size = 20;
        List<SaleResponseDTO> sales = Collections.singletonList(saleResponseDTO);

        when(securityUtils.canAccessClientData(clientId, request)).thenReturn(true);
        when(saleService.getSalesByClient(clientId, page, size)).thenReturn(sales);

        // When
        ResponseEntity<?> response = saleController.getSalesByClient(clientId, page, size, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sales, response.getBody());
        verify(saleService, times(1)).getSalesByClient(clientId, page, size);
    }

    // ==================== TESTS DES NOUVEAUX ENDPOINTS ====================

    /**
     * Test de récupération des ventes du jour
     */
    @Test
    @DisplayName("getTodaySales() - Ventes du jour")
    void testGetTodaySalesSuccess() {
        // Given
        List<SaleResponseDTO> todaySales = Collections.singletonList(saleResponseDTO);
        when(securityUtils.canAccessClientData(clientId, request)).thenReturn(true);
        when(saleService.getTodaySales(clientId)).thenReturn(todaySales);

        // When
        ResponseEntity<?> response = saleController.getTodaySales(clientId, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(todaySales, response.getBody());
        verify(saleService, times(1)).getTodaySales(clientId);
    }

    /**
     * Test de récupération du total du jour
     */
    @Test
    @DisplayName("getTodayTotal() - Total du jour")
    void testGetTodayTotalSuccess() {
        // Given
        BigDecimal todayTotal = BigDecimal.valueOf(500.00);
        when(securityUtils.canAccessClientData(clientId, request)).thenReturn(true);
        when(saleService.getTodayTotalSales(clientId)).thenReturn(todayTotal);

        // When
        ResponseEntity<?> response = saleController.getTodayTotal(clientId, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(Map.class, response.getBody());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals(todayTotal, responseBody.get("total"));
        assertEquals("EUR", responseBody.get("currency"));
        verify(saleService, times(1)).getTodayTotalSales(clientId);
    }

    /**
     * Test de génération de ticket de caisse
     */
    @Test
    @DisplayName("getReceipt() - Génération de ticket")
    void testGetReceiptSuccess() {
        // Given
        ReceiptDTO receipt = new ReceiptDTO();
        receipt.setTotalAmount(BigDecimal.valueOf(150.00));

        // Mock pour que canAccessSale() retourne true
        when(securityUtils.getCurrentUserId(request)).thenReturn(clientId.toString());
        when(securityUtils.isCurrentUserAdmin()).thenReturn(false);
        when(saleService.canAccessSale(saleId, clientId)).thenReturn(true);
        when(saleService.generateReceiptData(saleId)).thenReturn(receipt);

        // When
        ResponseEntity<?> response = saleController.getReceipt(saleId, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(receipt, response.getBody());
        verify(saleService, times(1)).generateReceiptData(saleId);
    }

    /**
     * Test de vérification de disponibilité produit
     */
    @Test
    @DisplayName("checkProductAvailability() - Vérification stock")
    void testCheckProductAvailabilitySuccess() {
        // Given
        BigDecimal quantity = BigDecimal.valueOf(5);
        when(securityUtils.canAccessClientData(clientId, request)).thenReturn(true);
        when(saleService.checkProductAvailability(productId, clientId, quantity)).thenReturn(true);
        when(optimisticStockService.getTotalStockQuantity(productId, clientId)).thenReturn(10);

        // When
        ResponseEntity<?> response = saleController.checkProductAvailability(productId, clientId, quantity, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(Map.class, response.getBody());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) responseBody.get("available"));
        assertEquals(10, responseBody.get("currentStock"));
        assertEquals(quantity, responseBody.get("requestedQuantity"));
        assertEquals(productId, responseBody.get("productId"));
        verify(saleService, times(1)).checkProductAvailability(productId, clientId, quantity);
        verify(optimisticStockService, times(1)).getTotalStockQuantity(productId, clientId);
    }

    /**
     * Test du tableau de bord multi-caisses
     */
    @Test
    @DisplayName("getMultiPosDashboard() - Tableau de bord")
    void testGetMultiPosDashboardSuccess() {
        // Given
        List<SaleResponseDTO> pendingSales = Collections.singletonList(saleResponseDTO);
        BigDecimal todayTotal = BigDecimal.valueOf(1000.00);

        // Correction : créer des Object[] au lieu de List<Object>
        Object[] topProduct = {"Produit A", 50};
        List<Object[]> topProducts = Arrays.asList(new Object[][]{topProduct});

        Object[] hourlyStat = {10, 200.00};
        List<Object[]> hourlyStats = Arrays.asList(new Object[][]{hourlyStat});

        when(securityUtils.canAccessClientData(clientId, request)).thenReturn(true);
        when(saleService.getPendingSales(clientId)).thenReturn(pendingSales);
        when(saleService.getTodayTotalSales(clientId)).thenReturn(todayTotal);
        when(saleService.getTodayTopProducts(clientId, 5)).thenReturn(topProducts);
        when(saleService.getTodayHourlySalesStats(clientId)).thenReturn(hourlyStats);

        // When
        ResponseEntity<?> response = saleController.getMultiPosDashboard(clientId, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(Map.class, response.getBody());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals(pendingSales, responseBody.get("pendingSales"));
        assertEquals(1, responseBody.get("pendingSalesCount"));
        assertEquals(todayTotal, responseBody.get("todayTotal"));
        assertEquals(topProducts, responseBody.get("topProducts"));
        assertEquals(hourlyStats, responseBody.get("hourlyStats"));

        verify(saleService, times(1)).getPendingSales(clientId);
        verify(saleService, times(1)).getTodayTotalSales(clientId);
        verify(saleService, times(1)).getTodayTopProducts(clientId, 5);
        verify(saleService, times(1)).getTodayHourlySalesStats(clientId);
    }

    // ==================== TESTS DE SÉCURITÉ ====================

    /**
     * Test de validation d'accès aux ventes
     */
    @Test
    @DisplayName("Sécurité - Validation d'accès aux ventes")
    void testSaleAccessValidation() {
        // Mock pour que canAccessSale() retourne false (pas d'utilisateur connecté)
        when(securityUtils.getCurrentUserId(request)).thenReturn(null);

        // Test sur plusieurs endpoints
        ResponseEntity<?> scanResponse = saleController.scanProduct(saleId, "123", BigDecimal.ONE, request);
        ResponseEntity<?> detailsResponse = saleController.getSaleDetails(saleId, request);
        ResponseEntity<?> totalResponse = saleController.calculateTotal(saleId, request);

        // Tous doivent retourner FORBIDDEN
        assertEquals(HttpStatus.FORBIDDEN, scanResponse.getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, detailsResponse.getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, totalResponse.getStatusCode());
    }

    /**
     * Test de validation d'accès aux données client
     */
    @Test
    @DisplayName("Sécurité - Validation d'accès aux données client")
    void testClientDataAccessValidation() {
        // Given
        when(securityUtils.canAccessClientData(clientId, request)).thenReturn(false);

        // When
        ResponseEntity<?> createResponse = saleController.createNewSale(clientId, request);
        ResponseEntity<?> salesResponse = saleController.getSalesByClient(clientId, 0, 20, request);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, createResponse.getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, salesResponse.getStatusCode());
    }

    // ==================== TESTS DE CONFIGURATION ====================

    /**
     * Test de configuration des mocks
     */
    @Test
    @DisplayName("Configuration des mocks")
    void testMockConfiguration() {
        // Vérifier que les mocks sont correctement injectés
        assertNotNull(saleController);
        assertNotNull(saleService);
        assertNotNull(securityUtils);
        assertNotNull(optimisticStockService);
    }
}