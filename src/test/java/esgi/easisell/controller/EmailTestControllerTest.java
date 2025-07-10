/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : EmailTestControllerTest.java
 * @description : Tests unitaires pour le contrôleur de test d'emails
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 10/07/2025
 * @package     : esgi.easisell.controller
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.controller;

import esgi.easisell.exception.EmailException;
import esgi.easisell.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour le contrôleur de test d'emails
 *
 * Tests des méthodes :
 * - testPreRegistrationEmail()
 * - testPaymentReminder()
 * - testLatePaymentReminder()
 * - testCancellation()
 */
@ExtendWith(MockitoExtension.class)
class EmailTestControllerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailTestController emailTestController;

    // ==================== TESTS EMAIL DE PRÉ-INSCRIPTION ====================

    /**
     * Test d'envoi d'email de pré-inscription avec succès
     */
    @Test
    @DisplayName("testPreRegistrationEmail() - Email envoyé avec succès")
    void testPreRegistrationEmailSuccess() throws EmailException {
        // Given
        String email = "test@example.com";
        doNothing().when(emailService).sendPreRegistrationEmail(any(), anyString());

        // When
        ResponseEntity<String> response = emailTestController.testPreRegistrationEmail(email);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Email de pré-inscription envoyé avec succès"));
        assertTrue(response.getBody().contains(email));
        verify(emailService, times(1)).sendPreRegistrationEmail(any(), eq("testPassword123"));
    }

    /**
     * Test d'envoi d'email de pré-inscription avec email par défaut
     */
    @Test
    @DisplayName("testPreRegistrationEmail() - Comportement avec valeur par défaut")
    void testPreRegistrationEmailDefaultEmail() throws EmailException {
        // Given
        doNothing().when(emailService).sendPreRegistrationEmail(any(), anyString());

        // When - Simuler un appel avec la valeur par défaut
        ResponseEntity<String> response = emailTestController.testPreRegistrationEmail("test@example.com");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("test@example.com"));
        verify(emailService, times(1)).sendPreRegistrationEmail(any(), anyString());
    }

    /**
     * Test d'envoi d'email de pré-inscription avec erreur
     */
    @Test
    @DisplayName("testPreRegistrationEmail() - Erreur d'envoi")
    void testPreRegistrationEmailError() throws EmailException {
        // Given
        String email = "test@example.com";
        doThrow(new EmailException("Erreur d'envoi")).when(emailService).sendPreRegistrationEmail(any(), anyString());

        // When
        ResponseEntity<String> response = emailTestController.testPreRegistrationEmail(email);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Erreur"));
    }

    // ==================== TESTS RAPPEL DE PAIEMENT ====================

    /**
     * Test d'envoi de rappel de paiement avec succès
     */
    @Test
    @DisplayName("testPaymentReminder() - Rappel envoyé avec succès")
    void testPaymentReminderSuccess() throws EmailException {
        // Given
        String email = "test@example.com";
        doNothing().when(emailService).sendPaymentReminder(any(), anyString(), any(), anyString(), any(), anyBoolean());

        // When
        ResponseEntity<String> response = emailTestController.testPaymentReminder(email);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Email de rappel de paiement envoyé avec succès"));
        assertTrue(response.getBody().contains(email));
        verify(emailService, times(1)).sendPaymentReminder(any(), eq("Abonnement Premium"), any(), eq("EUR"), any(), eq(false));
    }

    /**
     * Test d'envoi de rappel de paiement avec erreur
     */
    @Test
    @DisplayName("testPaymentReminder() - Erreur d'envoi")
    void testPaymentReminderError() throws EmailException {
        // Given
        String email = "test@example.com";
        doThrow(new EmailException("Erreur d'envoi")).when(emailService).sendPaymentReminder(any(), anyString(), any(), anyString(), any(), anyBoolean());

        // When
        ResponseEntity<String> response = emailTestController.testPaymentReminder(email);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Erreur"));
    }

    // ==================== TESTS RAPPEL DE PAIEMENT EN RETARD ====================

    /**
     * Test d'envoi de rappel de paiement en retard avec succès
     */
    @Test
    @DisplayName("testLatePaymentReminder() - Rappel en retard envoyé avec succès")
    void testLatePaymentReminderSuccess() throws EmailException {
        // Given
        String email = "test@example.com";
        doNothing().when(emailService).sendPaymentReminder(any(), anyString(), any(), anyString(), any(), anyBoolean());

        // When
        ResponseEntity<String> response = emailTestController.testLatePaymentReminder(email);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Email de rappel de paiement en retard envoyé avec succès"));
        assertTrue(response.getBody().contains(email));
        verify(emailService, times(1)).sendPaymentReminder(any(), eq("Abonnement Standard"), any(), eq("EUR"), any(), eq(true));
    }

    /**
     * Test d'envoi de rappel de paiement en retard avec email par défaut
     */
    @Test
    @DisplayName("testLatePaymentReminder() - Email par défaut")
    void testLatePaymentReminderDefaultEmail() throws EmailException {
        // Given
        doNothing().when(emailService).sendPaymentReminder(any(), anyString(), any(), anyString(), any(), anyBoolean());

        // When - Simuler un appel sans paramètre (valeur par défaut)
        ResponseEntity<String> response = emailTestController.testLatePaymentReminder("test@example.com");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("test@example.com"));
        verify(emailService, times(1)).sendPaymentReminder(any(), anyString(), any(), anyString(), any(), eq(true));
    }

    /**
     * Test d'envoi de rappel de paiement en retard avec erreur
     */
    @Test
    @DisplayName("testLatePaymentReminder() - Erreur d'envoi")
    void testLatePaymentReminderError() throws EmailException {
        // Given
        String email = "test@example.com";
        doThrow(new EmailException("Erreur d'envoi")).when(emailService).sendPaymentReminder(any(), anyString(), any(), anyString(), any(), anyBoolean());

        // When
        ResponseEntity<String> response = emailTestController.testLatePaymentReminder(email);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Erreur"));
    }

    // ==================== TESTS CONFIRMATION DE RÉSILIATION ====================

    /**
     * Test d'envoi de confirmation de résiliation avec succès
     */
    @Test
    @DisplayName("testCancellation() - Confirmation envoyée avec succès")
    void testCancellationSuccess() throws EmailException {
        // Given
        String email = "test@example.com";
        doNothing().when(emailService).sendCancellationConfirmation(any(), anyString(), any(), any(), anyString());

        // When
        ResponseEntity<String> response = emailTestController.testCancellation(email);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Email de confirmation de résiliation envoyé avec succès"));
        assertTrue(response.getBody().contains(email));
        verify(emailService, times(1)).sendCancellationConfirmation(any(), eq("Abonnement Standard"), any(), any(), anyString());
    }

    /**
     * Test d'envoi de confirmation de résiliation avec email par défaut
     */
    @Test
    @DisplayName("testCancellation() - Email par défaut")
    void testCancellationDefaultEmail() throws EmailException {
        // Given
        doNothing().when(emailService).sendCancellationConfirmation(any(), anyString(), any(), any(), anyString());

        // When - Simuler un appel sans paramètre (valeur par défaut)
        ResponseEntity<String> response = emailTestController.testCancellation("test@example.com");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("test@example.com"));
        verify(emailService, times(1)).sendCancellationConfirmation(any(), anyString(), any(), any(), anyString());
    }

    /**
     * Test d'envoi de confirmation de résiliation avec erreur
     */
    @Test
    @DisplayName("testCancellation() - Erreur d'envoi")
    void testCancellationError() throws EmailException {
        // Given
        String email = "test@example.com";
        doThrow(new EmailException("Erreur d'envoi")).when(emailService).sendCancellationConfirmation(any(), anyString(), any(), any(), anyString());

        // When
        ResponseEntity<String> response = emailTestController.testCancellation(email);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Erreur"));
    }

    // ==================== TESTS AVEC DIFFÉRENTS PARAMÈTRES ====================

    /**
     * Test de validation des paramètres d'email personnalisés
     */
    @Test
    @DisplayName("testPreRegistrationEmail() - Email personnalisé")
    void testPreRegistrationEmailCustomEmail() throws EmailException {
        // Given
        String customEmail = "custom@domain.com";
        doNothing().when(emailService).sendPreRegistrationEmail(any(), anyString());

        // When
        ResponseEntity<String> response = emailTestController.testPreRegistrationEmail(customEmail);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains(customEmail));
        verify(emailService, times(1)).sendPreRegistrationEmail(any(), anyString());
    }

    /**
     * Test de validation des montants dans les rappels de paiement
     */
    @Test
    @DisplayName("testPaymentReminder() - Vérification des montants")
    void testPaymentReminderAmounts() throws EmailException {
        // Given
        doNothing().when(emailService).sendPaymentReminder(any(), anyString(), any(), anyString(), any(), anyBoolean());

        // When - Test rappel normal
        emailTestController.testPaymentReminder("test@example.com");

        // When - Test rappel en retard
        emailTestController.testLatePaymentReminder("test@example.com");

        // Then
        verify(emailService, times(1)).sendPaymentReminder(any(), eq("Abonnement Premium"), any(), anyString(), any(), eq(false));
        verify(emailService, times(1)).sendPaymentReminder(any(), eq("Abonnement Standard"), any(), anyString(), any(), eq(true));
    }
}