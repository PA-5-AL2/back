/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : SupplierControllerTest.java
 * @description : Tests unitaires pour le contrôleur des fournisseurs
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 11/07/2025
 * @package     : esgi.easisell.controller
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.controller;

import esgi.easisell.dto.SupplierDTO;
import esgi.easisell.dto.SupplierResponseDTO;
import esgi.easisell.entity.Client;
import esgi.easisell.entity.Supplier;
import esgi.easisell.service.SupplierService;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour le contrôleur des fournisseurs
 * Couverture 100% des lignes de code
 */
@ExtendWith(MockitoExtension.class)
class SupplierControllerTest {

    @Mock
    private SupplierService supplierService;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private SupplierController supplierController;

    private SupplierDTO supplierDTO;
    private Supplier mockSupplier;
    private Client mockClient;
    private SupplierResponseDTO supplierResponseDTO;
    private UUID supplierId;
    private UUID clientId;

    /**
     * Configuration initiale pour chaque test
     */
    @BeforeEach
    void setUp() {
        supplierId = UUID.randomUUID();
        clientId = UUID.randomUUID();

        mockClient = new Client();
        mockClient.setUserId(clientId);
        mockClient.setName("Test Store");

        mockSupplier = new Supplier();
        mockSupplier.setSupplierId(supplierId);
        mockSupplier.setName("Fournisseur Test");
        mockSupplier.setFirstName("Jean");
        mockSupplier.setDescription("Fournisseur de boissons");
        mockSupplier.setContactInfo("contact@fournisseur.com");
        mockSupplier.setPhoneNumber("0123456789");
        mockSupplier.setClient(mockClient);

        supplierDTO = new SupplierDTO();
        supplierDTO.setName("Fournisseur Test");
        supplierDTO.setFirstName("Jean");
        supplierDTO.setDescription("Fournisseur de boissons");
        supplierDTO.setContactInfo("contact@fournisseur.com");
        supplierDTO.setPhoneNumber("0123456789");

        supplierResponseDTO = new SupplierResponseDTO(mockSupplier);
    }

    // ==================== TESTS GET SUPPLIERS BY CLIENT ID ====================

    /**
     * Test de récupération des fournisseurs par client ID - Succès
     */
    @Test
    @DisplayName("✅ getSuppliersByClientId() - Succès")
    void testGetSuppliersByClientIdSuccess() {
        // Given
        List<SupplierResponseDTO> supplierList = Arrays.asList(supplierResponseDTO);
        when(securityUtils.canAccessClientData(eq(clientId), any(HttpServletRequest.class))).thenReturn(true);
        when(supplierService.getSuppliersByClientId(clientId)).thenReturn(supplierList);

        // When
        ResponseEntity<?> response = supplierController.getSuppliersByClientId(clientId, httpServletRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(supplierList, response.getBody());
        verify(securityUtils, times(1)).canAccessClientData(eq(clientId), any(HttpServletRequest.class));
        verify(supplierService, times(1)).getSuppliersByClientId(clientId);
    }

    /**
     * Test de récupération des fournisseurs par client ID - Accès refusé
     */
    @Test
    @DisplayName("❌ getSuppliersByClientId() - Accès refusé")
    void testGetSuppliersByClientIdAccessDenied() {
        // Given
        when(securityUtils.canAccessClientData(eq(clientId), any(HttpServletRequest.class))).thenReturn(false);

        // When
        ResponseEntity<?> response = supplierController.getSuppliersByClientId(clientId, httpServletRequest);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Accès non autorisé à ce client", body.get("error"));
        verify(supplierService, never()).getSuppliersByClientId(any());
    }

    // ==================== TESTS GET SUPPLIER BY ID ====================

    /**
     * Test de récupération d'un fournisseur par ID - Succès
     */
    @Test
    @DisplayName("✅ getSupplierById() - Succès")
    void testGetSupplierByIdSuccess() {
        // Given
        when(supplierService.getSupplierById(supplierId)).thenReturn(Optional.of(mockSupplier));
        when(securityUtils.canAccessClientData(eq(clientId), any(HttpServletRequest.class))).thenReturn(true);

        // When
        ResponseEntity<?> response = supplierController.getSupplierById(supplierId, httpServletRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof SupplierResponseDTO);
        verify(supplierService, times(1)).getSupplierById(supplierId);
        verify(securityUtils, times(1)).canAccessClientData(eq(clientId), any(HttpServletRequest.class));
    }

    /**
     * Test de récupération d'un fournisseur par ID - Fournisseur non trouvé
     */
    @Test
    @DisplayName("❌ getSupplierById() - Fournisseur non trouvé")
    void testGetSupplierByIdNotFound() {
        // Given
        when(supplierService.getSupplierById(supplierId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = supplierController.getSupplierById(supplierId, httpServletRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(supplierService, times(1)).getSupplierById(supplierId);
        verify(securityUtils, never()).canAccessClientData(any(), any());
    }

    /**
     * Test de récupération d'un fournisseur par ID - Accès refusé
     */
    @Test
    @DisplayName("❌ getSupplierById() - Accès refusé")
    void testGetSupplierByIdAccessDenied() {
        // Given
        when(supplierService.getSupplierById(supplierId)).thenReturn(Optional.of(mockSupplier));
        when(securityUtils.canAccessClientData(eq(clientId), any(HttpServletRequest.class))).thenReturn(false);

        // When
        ResponseEntity<?> response = supplierController.getSupplierById(supplierId, httpServletRequest);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Accès non autorisé à ce fournisseur", body.get("error"));
    }

    // ==================== TESTS CREATE SUPPLIER ====================

    /**
     * Test de création d'un fournisseur - Accès refusé
     */
    @Test
    @DisplayName("❌ createSupplier() - Accès refusé")
    void testCreateSupplierAccessDenied() {
        // Given
        when(securityUtils.canAccessClientData(eq(clientId), any(HttpServletRequest.class))).thenReturn(false);

        // When
        ResponseEntity<?> response = supplierController.createSupplier(clientId, supplierDTO, httpServletRequest);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Accès non autorisé à ce client", body.get("error"));
        verify(supplierService, never()).createSupplier(any(), any());
    }

    /**
     * Test de création d'un fournisseur - Client non trouvé
     */
    @Test
    @DisplayName("❌ createSupplier() - Client non trouvé")
    void testCreateSupplierClientNotFound() {
        // Given
        when(securityUtils.canAccessClientData(eq(clientId), any(HttpServletRequest.class))).thenReturn(true);
        when(supplierService.createSupplier(eq(clientId), any(SupplierDTO.class))).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = supplierController.createSupplier(clientId, supplierDTO, httpServletRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Client non trouvé", body.get("error"));
    }

    /**
     * Test de création d'un fournisseur - Exception
     */
    @Test
    @DisplayName("❌ createSupplier() - Exception")
    void testCreateSupplierException() {
        // Given
        when(securityUtils.canAccessClientData(eq(clientId), any(HttpServletRequest.class))).thenReturn(true);
        when(supplierService.createSupplier(eq(clientId), any(SupplierDTO.class)))
                .thenThrow(new RuntimeException("Erreur lors de la création"));

        // When
        ResponseEntity<?> response = supplierController.createSupplier(clientId, supplierDTO, httpServletRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Erreur lors de la création", body.get("error"));
    }

    // ==================== TESTS UPDATE SUPPLIER ====================

    /**
     * Test de mise à jour d'un fournisseur - Succès
     */
    @Test
    @DisplayName("✅ updateSupplier() - Succès")
    void testUpdateSupplierSuccess() {
        // Given
        when(supplierService.getSupplierById(supplierId)).thenReturn(Optional.of(mockSupplier));
        when(securityUtils.canAccessClientData(eq(clientId), any(HttpServletRequest.class))).thenReturn(true);
        when(supplierService.updateSupplier(eq(supplierId), any(SupplierDTO.class))).thenReturn(Optional.of(supplierResponseDTO));

        // When
        ResponseEntity<?> response = supplierController.updateSupplier(supplierId, supplierDTO, httpServletRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(supplierResponseDTO, response.getBody());
        verify(supplierService, times(1)).getSupplierById(supplierId);
        verify(securityUtils, times(1)).canAccessClientData(eq(clientId), any(HttpServletRequest.class));
        verify(supplierService, times(1)).updateSupplier(eq(supplierId), any(SupplierDTO.class));
    }

    /**
     * Test de mise à jour d'un fournisseur - Fournisseur non trouvé
     */
    @Test
    @DisplayName("❌ updateSupplier() - Fournisseur non trouvé")
    void testUpdateSupplierNotFound() {
        // Given
        when(supplierService.getSupplierById(supplierId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = supplierController.updateSupplier(supplierId, supplierDTO, httpServletRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(supplierService, times(1)).getSupplierById(supplierId);
        verify(securityUtils, never()).canAccessClientData(any(), any());
        verify(supplierService, never()).updateSupplier(any(), any());
    }

    /**
     * Test de mise à jour d'un fournisseur - Accès refusé
     */
    @Test
    @DisplayName("❌ updateSupplier() - Accès refusé")
    void testUpdateSupplierAccessDenied() {
        // Given
        when(supplierService.getSupplierById(supplierId)).thenReturn(Optional.of(mockSupplier));
        when(securityUtils.canAccessClientData(eq(clientId), any(HttpServletRequest.class))).thenReturn(false);

        // When
        ResponseEntity<?> response = supplierController.updateSupplier(supplierId, supplierDTO, httpServletRequest);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Accès non autorisé à ce fournisseur", body.get("error"));
        verify(supplierService, never()).updateSupplier(any(), any());
    }

    /**
     * Test de mise à jour d'un fournisseur - Mise à jour échouée
     */
    @Test
    @DisplayName("❌ updateSupplier() - Mise à jour échouée")
    void testUpdateSupplierUpdateFailed() {
        // Given
        when(supplierService.getSupplierById(supplierId)).thenReturn(Optional.of(mockSupplier));
        when(securityUtils.canAccessClientData(eq(clientId), any(HttpServletRequest.class))).thenReturn(true);
        when(supplierService.updateSupplier(eq(supplierId), any(SupplierDTO.class))).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = supplierController.updateSupplier(supplierId, supplierDTO, httpServletRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(supplierService, times(1)).updateSupplier(eq(supplierId), any(SupplierDTO.class));
    }

    // ==================== TESTS DELETE SUPPLIER ====================

    /**
     * Test de suppression d'un fournisseur - Succès
     */
    @Test
    @DisplayName("✅ deleteSupplier() - Succès")
    void testDeleteSupplierSuccess() {
        // Given
        when(supplierService.getSupplierById(supplierId)).thenReturn(Optional.of(mockSupplier));
        when(securityUtils.canAccessClientData(eq(clientId), any(HttpServletRequest.class))).thenReturn(true);
        doNothing().when(supplierService).deleteSupplier(supplierId);

        // When
        ResponseEntity<?> response = supplierController.deleteSupplier(supplierId, httpServletRequest);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(supplierService, times(1)).getSupplierById(supplierId);
        verify(securityUtils, times(1)).canAccessClientData(eq(clientId), any(HttpServletRequest.class));
        verify(supplierService, times(1)).deleteSupplier(supplierId);
    }

    /**
     * Test de suppression d'un fournisseur - Fournisseur non trouvé
     */
    @Test
    @DisplayName("❌ deleteSupplier() - Fournisseur non trouvé")
    void testDeleteSupplierNotFound() {
        // Given
        when(supplierService.getSupplierById(supplierId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = supplierController.deleteSupplier(supplierId, httpServletRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(supplierService, times(1)).getSupplierById(supplierId);
        verify(securityUtils, never()).canAccessClientData(any(), any());
        verify(supplierService, never()).deleteSupplier(any());
    }

    /**
     * Test de suppression d'un fournisseur - Accès refusé
     */
    @Test
    @DisplayName("❌ deleteSupplier() - Accès refusé")
    void testDeleteSupplierAccessDenied() {
        // Given
        when(supplierService.getSupplierById(supplierId)).thenReturn(Optional.of(mockSupplier));
        when(securityUtils.canAccessClientData(eq(clientId), any(HttpServletRequest.class))).thenReturn(false);

        // When
        ResponseEntity<?> response = supplierController.deleteSupplier(supplierId, httpServletRequest);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Accès non autorisé à ce fournisseur", body.get("error"));
        verify(supplierService, never()).deleteSupplier(any());
    }
}