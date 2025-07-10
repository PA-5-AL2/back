/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : EmployeeAccessControllerTest.java
 * @description : Tests unitaires pour le contrôleur d'accès employé
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 10/07/2025
 * @package     : esgi.easisell.controller
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.controller;

import esgi.easisell.controller.EmployeeAccessController.EmployeeAccessRequestDTO;
import esgi.easisell.entity.Client;
import esgi.easisell.exception.EmailException;
import esgi.easisell.repository.ClientRepository;
import esgi.easisell.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour le contrôleur d'accès employé
 *
 * Tests des méthodes :
 * - requestAccess()
 */
@ExtendWith(MockitoExtension.class)
class EmployeeAccessControllerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private EmployeeAccessController employeeAccessController;

    private EmployeeAccessRequestDTO accessRequestDTO;
    private Client mockClient;

    /**
     * Configuration initiale pour chaque test
     */
    @BeforeEach
    void setUp() {
        // Configuration du client mock
        mockClient = new Client();
        mockClient.setUserId(UUID.randomUUID());
        mockClient.setUsername("client@store.com");
        mockClient.setName("Test Store");

        // Configuration du DTO de demande d'accès
        accessRequestDTO = new EmployeeAccessRequestDTO();
        accessRequestDTO.setClientId(mockClient.getUserId().toString());
        accessRequestDTO.setEmployeeName("Jean Dupont");
        accessRequestDTO.setEmployeeEmail("jean.dupont@employee.com");
    }

    // ==================== TESTS DE DEMANDE D'ACCÈS ====================

    /**
     * Test de demande d'accès employé réussie
     */
    @Test
    @DisplayName("requestAccess() - Demande d'accès envoyée avec succès")
    void testRequestAccessSuccess() throws EmailException {
        // Given
        when(clientRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockClient));
        doNothing().when(emailService).sendEmployeeAccessRequest(any(), anyString(), anyString());

        // When
        ResponseEntity<?> response = employeeAccessController.requestAccess(accessRequestDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) responseBody.get("success"));
        assertTrue(responseBody.get("message").toString().contains("Test Store"));
        verify(emailService, times(1)).sendEmployeeAccessRequest(mockClient, "Jean Dupont", "jean.dupont@employee.com");
    }

    /**
     * Test de demande d'accès avec client non trouvé
     */
    @Test
    @DisplayName("requestAccess() - Client non trouvé")
    void testRequestAccessClientNotFound() throws EmailException {
        // Given
        when(clientRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = employeeAccessController.requestAccess(accessRequestDTO);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertTrue(responseBody.get("message").toString().contains("Client non trouvé"));
        verify(emailService, never()).sendEmployeeAccessRequest(any(), anyString(), anyString());
    }

    /**
     * Test de demande d'accès avec erreur d'email
     */
    @Test
    @DisplayName("requestAccess() - Erreur d'email")
    void testRequestAccessEmailError() throws EmailException {
        // Given
        when(clientRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockClient));
        doThrow(new EmailException("Erreur d'envoi")).when(emailService).sendEmployeeAccessRequest(any(), anyString(), anyString());

        // When
        ResponseEntity<?> response = employeeAccessController.requestAccess(accessRequestDTO);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertTrue(responseBody.get("message").toString().contains("Erreur lors de l'envoi"));
    }

    /**
     * Test de demande d'accès avec erreur inattendue
     */
    @Test
    @DisplayName("requestAccess() - Erreur inattendue")
    void testRequestAccessUnexpectedError() throws EmailException {
        // Given
        when(clientRepository.findById(any(UUID.class))).thenThrow(new RuntimeException("Erreur inattendue"));

        // When
        ResponseEntity<?> response = employeeAccessController.requestAccess(accessRequestDTO);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertTrue(responseBody.get("message").toString().contains("Erreur inattendue"));
        verify(emailService, never()).sendEmployeeAccessRequest(any(), anyString(), anyString());
    }

    /**
     * Test de validation des paramètres de la demande d'accès
     */
    @Test
    @DisplayName("requestAccess() - Validation des paramètres")
    void testRequestAccessParameterValidation() throws EmailException {
        // Given
        when(clientRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockClient));
        doNothing().when(emailService).sendEmployeeAccessRequest(any(), anyString(), anyString());

        // When
        ResponseEntity<?> response = employeeAccessController.requestAccess(accessRequestDTO);

        // Then
        verify(emailService, times(1)).sendEmployeeAccessRequest(
                eq(mockClient),
                eq("Jean Dupont"),
                eq("jean.dupont@employee.com")
        );
    }

    // ==================== TESTS DU DTO ====================

    /**
     * Test des getters/setters du DTO EmployeeAccessRequestDTO
     */
    @Test
    @DisplayName("EmployeeAccessRequestDTO - Getters/Setters")
    void testEmployeeAccessRequestDTOGettersSetters() {
        // Given
        EmployeeAccessRequestDTO dto = new EmployeeAccessRequestDTO();
        String clientId = UUID.randomUUID().toString();
        String employeeName = "Marie Martin";
        String employeeEmail = "marie.martin@company.com";

        // When
        dto.setClientId(clientId);
        dto.setEmployeeName(employeeName);
        dto.setEmployeeEmail(employeeEmail);

        // Then
        assertEquals(clientId, dto.getClientId());
        assertEquals(employeeName, dto.getEmployeeName());
        assertEquals(employeeEmail, dto.getEmployeeEmail());
    }

    /**
     * Test du constructeur avec paramètres du DTO
     */
    @Test
    @DisplayName("EmployeeAccessRequestDTO - Constructeur avec paramètres")
    void testEmployeeAccessRequestDTOParameterizedConstructor() {
        // Given
        String clientId = UUID.randomUUID().toString();
        String employeeName = "Pierre Durand";
        String employeeEmail = "pierre.durand@company.com";

        // When
        EmployeeAccessRequestDTO dto = new EmployeeAccessRequestDTO(clientId, employeeName, employeeEmail);

        // Then
        assertEquals(clientId, dto.getClientId());
        assertEquals(employeeName, dto.getEmployeeName());
        assertEquals(employeeEmail, dto.getEmployeeEmail());
    }

    /**
     * Test du constructeur par défaut du DTO
     */
    @Test
    @DisplayName("EmployeeAccessRequestDTO - Constructeur par défaut")
    void testEmployeeAccessRequestDTODefaultConstructor() {
        // When
        EmployeeAccessRequestDTO dto = new EmployeeAccessRequestDTO();

        // Then
        assertNull(dto.getClientId());
        assertNull(dto.getEmployeeName());
        assertNull(dto.getEmployeeEmail());
    }

    // ==================== TESTS AVEC DIFFÉRENTS TYPES D'EMPLOYÉS ====================

    /**
     * Test avec différents noms d'employés
     */
    @Test
    @DisplayName("requestAccess() - Différents noms d'employés")
    void testRequestAccessDifferentEmployeeNames() throws EmailException {
        // Given
        when(clientRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockClient));
        doNothing().when(emailService).sendEmployeeAccessRequest(any(), anyString(), anyString());

        // Test avec nom composé
        accessRequestDTO.setEmployeeName("Jean-Pierre Dupont-Martin");
        accessRequestDTO.setEmployeeEmail("jp.dupont@company.com");

        // When
        ResponseEntity<?> response = employeeAccessController.requestAccess(accessRequestDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(emailService, times(1)).sendEmployeeAccessRequest(
                any(),
                eq("Jean-Pierre Dupont-Martin"),
                eq("jp.dupont@company.com")
        );
    }

    /**
     * Test avec différents formats d'email
     */
    @Test
    @DisplayName("requestAccess() - Différents formats d'email")
    void testRequestAccessDifferentEmailFormats() throws EmailException {
        // Given
        when(clientRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockClient));
        doNothing().when(emailService).sendEmployeeAccessRequest(any(), anyString(), anyString());

        // Test avec email de domaine différent
        accessRequestDTO.setEmployeeEmail("employee@external-company.org");

        // When
        ResponseEntity<?> response = employeeAccessController.requestAccess(accessRequestDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(emailService, times(1)).sendEmployeeAccessRequest(
                any(),
                anyString(),
                eq("employee@external-company.org")
        );
    }
}