/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : ClientRequestControllerTest.java
 * @description : Tests unitaires pour le contrôleur des demandes clients
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 10/07/2025
 * @package     : esgi.easisell.controller
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.controller;

import esgi.easisell.dto.*;
import esgi.easisell.entity.Client;
import esgi.easisell.entity.ClientRequest;
import esgi.easisell.service.ClientRequestService;
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

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour le contrôleur des demandes clients
 * Couverture 100% des lignes de code
 */
@ExtendWith(MockitoExtension.class)
class ClientRequestControllerTest {

    @Mock
    private ClientRequestService clientRequestService;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private ClientRequestController clientRequestController;

    private ClientRequestDTO clientRequestDTO;
    private ClientRequest mockClientRequest;
    private Client mockClient;
    private ApproveRequestDTO approveRequestDTO;
    private RejectRequestDTO rejectRequestDTO;
    private UUID requestId;

    /**
     * Configuration initiale pour chaque test
     */
    @BeforeEach
    void setUp() {
        requestId = UUID.randomUUID();

        clientRequestDTO = new ClientRequestDTO();
        clientRequestDTO.setCompanyName("Test Company");
        clientRequestDTO.setContactName("John Doe");
        clientRequestDTO.setEmail("test@company.com");
        clientRequestDTO.setPhoneNumber("0123456789");
        clientRequestDTO.setAddress("123 Test Street");
        clientRequestDTO.setMessage("Nous souhaitons utiliser votre plateforme");

        mockClientRequest = new ClientRequest();
        mockClientRequest.setRequestId(requestId);
        mockClientRequest.setCompanyName("Test Company");
        mockClientRequest.setEmail("test@company.com");

        mockClient = new Client();
        mockClient.setUserId(UUID.randomUUID());
        mockClient.setUsername("test@company.com");
        mockClient.setName("Test Company");

        approveRequestDTO = new ApproveRequestDTO();
        approveRequestDTO.setCurrencyPreference("EUR");
        approveRequestDTO.setContractStatus("ACTIVE");
        approveRequestDTO.setAdminNotes("Demande approuvée - client fiable");

        rejectRequestDTO = new RejectRequestDTO();
        rejectRequestDTO.setReason("Informations incomplètes");
        rejectRequestDTO.setAdminNotes("Manque les justificatifs d'entreprise");
    }

    /**
     * Test de soumission de demande réussie
     */
    @Test
    @DisplayName("✅ submitClientRequest() - Succès")
    void testSubmitClientRequestSuccess() {
        // Given
        when(clientRequestService.submitRequest(any(ClientRequestDTO.class))).thenReturn(mockClientRequest);

        // When
        ResponseEntity<?> response = clientRequestController.submitClientRequest(clientRequestDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    /**
     * Test de soumission de demande avec argument invalide
     */
    @Test
    @DisplayName("❌ submitClientRequest() - Argument invalide")
    void testSubmitClientRequestInvalidArgument() {
        // Given
        when(clientRequestService.submitRequest(any(ClientRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Email déjà utilisé"));

        // When
        ResponseEntity<?> response = clientRequestController.submitClientRequest(clientRequestDTO);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * Test de soumission de demande avec exception générale
     */
    @Test
    @DisplayName("❌ submitClientRequest() - Exception générale")
    void testSubmitClientRequestGeneralException() {
        // Given
        when(clientRequestService.submitRequest(any(ClientRequestDTO.class)))
                .thenThrow(new RuntimeException("Erreur base de données"));

        // When
        ResponseEntity<?> response = clientRequestController.submitClientRequest(clientRequestDTO);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    /**
     * Test de récupération des demandes en attente - Admin
     */
    @Test
    @DisplayName("✅ getPendingRequests() - Admin succès")
    void testGetPendingRequestsAdminSuccess() {
        // Given
        when(securityUtils.isCurrentUserAdmin()).thenReturn(true);
        when(clientRequestService.getPendingRequests()).thenReturn(List.of(new ClientRequestResponseDTO()));

        // When
        ResponseEntity<?> response = clientRequestController.getPendingRequests(httpServletRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Test de récupération des demandes en attente - Non admin
     */
    @Test
    @DisplayName("❌ getPendingRequests() - Non admin")
    void testGetPendingRequestsNotAdmin() {
        // Given
        when(securityUtils.isCurrentUserAdmin()).thenReturn(false);

        // When
        ResponseEntity<?> response = clientRequestController.getPendingRequests(httpServletRequest);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    /**
     * Test de récupération des demandes en attente avec exception
     */
    @Test
    @DisplayName("❌ getPendingRequests() - Exception")
    void testGetPendingRequestsException() {
        // Given
        when(securityUtils.isCurrentUserAdmin()).thenReturn(true);
        when(clientRequestService.getPendingRequests()).thenThrow(new RuntimeException("Erreur DB"));

        // When
        ResponseEntity<?> response = clientRequestController.getPendingRequests(httpServletRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    /**
     * Test de récupération de toutes les demandes - Admin
     */
    @Test
    @DisplayName("✅ getAllRequests() - Admin succès")
    void testGetAllRequestsAdminSuccess() {
        // Given
        when(securityUtils.isCurrentUserAdmin()).thenReturn(true);
        when(clientRequestService.getAllRequests()).thenReturn(List.of(new ClientRequestResponseDTO()));

        // When
        ResponseEntity<?> response = clientRequestController.getAllRequests(httpServletRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Test de récupération de toutes les demandes - Non admin
     */
    @Test
    @DisplayName("❌ getAllRequests() - Non admin")
    void testGetAllRequestsNotAdmin() {
        // Given
        when(securityUtils.isCurrentUserAdmin()).thenReturn(false);

        // When
        ResponseEntity<?> response = clientRequestController.getAllRequests(httpServletRequest);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    /**
     * Test de récupération de toutes les demandes avec exception
     */
    @Test
    @DisplayName("❌ getAllRequests() - Exception")
    void testGetAllRequestsException() {
        // Given
        when(securityUtils.isCurrentUserAdmin()).thenReturn(true);
        when(clientRequestService.getAllRequests()).thenThrow(new RuntimeException("Erreur DB"));

        // When
        ResponseEntity<?> response = clientRequestController.getAllRequests(httpServletRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    /**
     * Test d'approbation de demande - Admin succès
     */
    @Test
    @DisplayName("✅ approveRequest() - Admin succès")
    void testApproveRequestAdminSuccess() {
        // Given
        when(securityUtils.isCurrentUserAdmin()).thenReturn(true);
        when(clientRequestService.approveRequest(requestId, approveRequestDTO)).thenReturn(mockClient);

        // When
        ResponseEntity<?> response = clientRequestController.approveRequest(requestId, approveRequestDTO, httpServletRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Test d'approbation de demande - Non admin
     */
    @Test
    @DisplayName("❌ approveRequest() - Non admin")
    void testApproveRequestNotAdmin() {
        // Given
        when(securityUtils.isCurrentUserAdmin()).thenReturn(false);

        // When
        ResponseEntity<?> response = clientRequestController.approveRequest(requestId, approveRequestDTO, httpServletRequest);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    /**
     * Test d'approbation de demande avec RuntimeException
     */
    @Test
    @DisplayName("❌ approveRequest() - RuntimeException")
    void testApproveRequestRuntimeException() {
        // Given
        when(securityUtils.isCurrentUserAdmin()).thenReturn(true);
        when(clientRequestService.approveRequest(requestId, approveRequestDTO))
                .thenThrow(new RuntimeException("Demande déjà traitée"));

        // When
        ResponseEntity<?> response = clientRequestController.approveRequest(requestId, approveRequestDTO, httpServletRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * Test d'approbation de demande avec exception générale
     */
    @Test
    @DisplayName("❌ approveRequest() - Exception générale")
    void testApproveRequestGeneralException() {
        // Given
        when(securityUtils.isCurrentUserAdmin()).thenReturn(true);
        when(clientRequestService.approveRequest(requestId, approveRequestDTO))
                .thenThrow(new RuntimeException("Erreur interne"));

        // When
        ResponseEntity<?> response = clientRequestController.approveRequest(requestId, approveRequestDTO, httpServletRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * Test de rejet de demande - Admin succès
     */
    @Test
    @DisplayName("✅ rejectRequest() - Admin succès")
    void testRejectRequestAdminSuccess() {
        // Given
        when(securityUtils.isCurrentUserAdmin()).thenReturn(true);
        doNothing().when(clientRequestService).rejectRequest(requestId, rejectRequestDTO);

        // When
        ResponseEntity<?> response = clientRequestController.rejectRequest(requestId, rejectRequestDTO, httpServletRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Test de rejet de demande - Non admin
     */
    @Test
    @DisplayName("❌ rejectRequest() - Non admin")
    void testRejectRequestNotAdmin() {
        // Given
        when(securityUtils.isCurrentUserAdmin()).thenReturn(false);

        // When
        ResponseEntity<?> response = clientRequestController.rejectRequest(requestId, rejectRequestDTO, httpServletRequest);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    /**
     * Test de rejet de demande avec RuntimeException
     */
    @Test
    @DisplayName("❌ rejectRequest() - RuntimeException")
    void testRejectRequestRuntimeException() {
        // Given
        when(securityUtils.isCurrentUserAdmin()).thenReturn(true);
        doThrow(new RuntimeException("Demande déjà traitée"))
                .when(clientRequestService).rejectRequest(requestId, rejectRequestDTO);

        // When
        ResponseEntity<?> response = clientRequestController.rejectRequest(requestId, rejectRequestDTO, httpServletRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * Test de rejet de demande avec exception générale
     */
    @Test
    @DisplayName("❌ rejectRequest() - Exception générale")
    void testRejectRequestGeneralException() {
        // Given
        when(securityUtils.isCurrentUserAdmin()).thenReturn(true);
        doThrow(new RuntimeException("Erreur interne"))
                .when(clientRequestService).rejectRequest(requestId, rejectRequestDTO);

        // When
        ResponseEntity<?> response = clientRequestController.rejectRequest(requestId, rejectRequestDTO, httpServletRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}