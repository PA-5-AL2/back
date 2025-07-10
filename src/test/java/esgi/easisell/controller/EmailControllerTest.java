/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : EmailControllerTest.java
 * @description : Tests unitaires pour le contrôleur d'emails
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 10/07/2025
 * @package     : esgi.easisell.controller
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.controller;

import esgi.easisell.dto.EmailReminderDTO;
import esgi.easisell.dto.EmailCancellationDTO;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour le contrôleur d'emails
 *
 * Tests des méthodes :
 * - sendPaymentReminder()
 * - sendCancellationConfirmation()
 * - getEmailServiceStatus()
 */
@ExtendWith(MockitoExtension.class)
class EmailControllerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private EmailController emailController;

    private EmailReminderDTO reminderDTO;
    private EmailCancellationDTO cancellationDTO;
    private Client mockClient;

    /**
     * Configuration initiale pour chaque test
     */
    @BeforeEach
    void setUp() {
        // Configuration du client mock
        mockClient = new Client();
        mockClient.setUserId(UUID.randomUUID());
        mockClient.setUsername("test@store.com");
        mockClient.setName("Test Store");
        mockClient.setContractStatus("ACTIVE");

        // Configuration du DTO de rappel
        reminderDTO = new EmailReminderDTO();
        reminderDTO.setClientId(mockClient.getUserId().toString()); // Convertir en String
        reminderDTO.setServiceName("Service Test");
        reminderDTO.setAmount(BigDecimal.valueOf(100.0));
        reminderDTO.setCurrency("EUR");
        reminderDTO.setDueDate(LocalDate.now().plusDays(7));
        reminderDTO.setLate(false);

        // Configuration du DTO de résiliation
        cancellationDTO = new EmailCancellationDTO();
        cancellationDTO.setClientId(mockClient.getUserId().toString()); // Convertir en String
        cancellationDTO.setServiceName("Service Test");
        cancellationDTO.setEffectiveDate(LocalDate.now());
        cancellationDTO.setEndDate(LocalDate.now().plusDays(30));
        cancellationDTO.setReference("REF123456");
    }

    // ==================== TESTS RAPPEL DE PAIEMENT ====================

    /**
     * Test d'envoi de rappel de paiement réussi
     */
    @Test
    @DisplayName("sendPaymentReminder() - Rappel envoyé avec succès")
    void testSendPaymentReminderSuccess() throws EmailException {
        // Given
        when(clientRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockClient));
        doNothing().when(emailService).sendPaymentReminder(any(), anyString(), any(), anyString(), any(), anyBoolean());

        // When
        ResponseEntity<?> response = emailController.sendPaymentReminder(reminderDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();

        assertTrue((Boolean) responseBody.get("success"), "La réponse devrait indiquer un succès");
        assertTrue(responseBody.get("message").toString().contains("test@store.com"));
        verify(emailService, times(1)).sendPaymentReminder(any(), anyString(), any(), anyString(), any(), anyBoolean());
    }

    /**
     * Test d'envoi de rappel avec client non trouvé
     */
    @Test
    @DisplayName("sendPaymentReminder() - Client non trouvé")
    void testSendPaymentReminderClientNotFound() throws EmailException {
        // Given
        when(clientRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = emailController.sendPaymentReminder(reminderDTO);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertTrue(responseBody.get("message").toString().contains("Client non trouvé"));
        verify(emailService, never()).sendPaymentReminder(any(), anyString(), any(), anyString(), any(), anyBoolean());
    }

    /**
     * Test d'envoi de rappel avec erreur d'email
     */
    @Test
    @DisplayName("sendPaymentReminder() - Erreur d'email")
    void testSendPaymentReminderEmailError() throws EmailException {
        // Given
        when(clientRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockClient));
        doThrow(new EmailException("Erreur d'envoi")).when(emailService).sendPaymentReminder(any(), anyString(), any(), anyString(), any(), anyBoolean());

        // When
        ResponseEntity<?> response = emailController.sendPaymentReminder(reminderDTO);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertTrue(responseBody.get("message").toString().contains("Erreur lors de l'envoi"));
    }

    // ==================== TESTS CONFIRMATION DE RÉSILIATION ====================

    /**
     * Test d'envoi de confirmation de résiliation réussi
     */
    @Test
    @DisplayName("sendCancellationConfirmation() - Confirmation envoyée avec succès")
    void testSendCancellationConfirmationSuccess() throws EmailException {
        // Given
        when(clientRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockClient));
        when(clientRepository.save(any(Client.class))).thenReturn(mockClient);
        doNothing().when(emailService).sendCancellationConfirmation(any(), anyString(), any(), any(), anyString());

        // When
        ResponseEntity<?> response = emailController.sendCancellationConfirmation(cancellationDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();

        assertTrue((Boolean) responseBody.get("success"), "La réponse devrait indiquer un succès");
        assertTrue(responseBody.get("message").toString().contains("test@store.com"));
        verify(clientRepository, times(1)).save(any(Client.class));
        verify(emailService, times(1)).sendCancellationConfirmation(any(), anyString(), any(), any(), anyString());
    }

    /**
     * Test de mise à jour du statut du contrat lors de la résiliation
     */
    @Test
    @DisplayName("sendCancellationConfirmation() - Mise à jour du statut")
    void testSendCancellationConfirmationUpdateStatus() throws EmailException {
        // Given
        when(clientRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockClient));
        when(clientRepository.save(any(Client.class))).thenReturn(mockClient);
        doNothing().when(emailService).sendCancellationConfirmation(any(), anyString(), any(), any(), anyString());

        // When
        emailController.sendCancellationConfirmation(cancellationDTO);

        // Then
        verify(clientRepository, times(1)).save(argThat(client ->
                "CANCELLED".equals(client.getContractStatus())
        ));
    }

    /**
     * Test d'envoi de confirmation avec client non trouvé
     */
    @Test
    @DisplayName("sendCancellationConfirmation() - Client non trouvé")
    void testSendCancellationConfirmationClientNotFound() throws EmailException {
        // Given
        when(clientRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = emailController.sendCancellationConfirmation(cancellationDTO);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertTrue(responseBody.get("message").toString().contains("Client non trouvé"));
        verify(emailService, never()).sendCancellationConfirmation(any(), anyString(), any(), any(), anyString());
    }

    /**
     * Test d'envoi de confirmation avec erreur d'email
     */
    @Test
    @DisplayName("sendCancellationConfirmation() - Erreur d'email")
    void testSendCancellationConfirmationEmailError() throws EmailException {
        // Given
        when(clientRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockClient));
        when(clientRepository.save(any(Client.class))).thenReturn(mockClient);
        doThrow(new EmailException("Erreur d'envoi")).when(emailService).sendCancellationConfirmation(any(), anyString(), any(), any(), anyString());

        // When
        ResponseEntity<?> response = emailController.sendCancellationConfirmation(cancellationDTO);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertTrue(responseBody.get("message").toString().contains("Erreur lors de l'envoi"));
    }

    // ==================== TESTS STATUT DU SERVICE ====================

    /**
     * Test du statut du service d'email
     */
    @Test
    @DisplayName("getEmailServiceStatus() - Statut du service")
    void testGetEmailServiceStatus() {
        // When
        ResponseEntity<?> response = emailController.getEmailServiceStatus();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("active", responseBody.get("status"));
        assertTrue(responseBody.get("message").toString().contains("opérationnel"));
    }
}