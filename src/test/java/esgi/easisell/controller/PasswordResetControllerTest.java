/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : PasswordResetControllerTest.java
 * @description : Tests unitaires pour le contrôleur de réinitialisation de mot de passe
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 10/07/2025
 * @package     : esgi.easisell.controller
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.controller;

import esgi.easisell.dto.PasswordResetRequestDTO;
import esgi.easisell.service.PasswordResetService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour le contrôleur de réinitialisation de mot de passe
 *
 * Tests des méthodes :
 * - requestPasswordReset()
 * - notifyPasswordChanged()
 * - getPasswordResetStatus()
 */
@ExtendWith(MockitoExtension.class)
class PasswordResetControllerTest {

    @Mock
    private PasswordResetService passwordResetService;

    @InjectMocks
    private PasswordResetController passwordResetController;

    private PasswordResetRequestDTO requestDTO;

    /**
     * Configuration initiale pour chaque test
     */
    @BeforeEach
    void setUp() {
        requestDTO = new PasswordResetRequestDTO();
        requestDTO.setEmail("test@example.com");
    }

    // ==================== TESTS DEMANDE DE RÉINITIALISATION ====================

    /**
     * Test de demande de réinitialisation réussie
     */
    @Test
    @DisplayName("requestPasswordReset() - Demande réussie")
    void testRequestPasswordResetSuccess() {
        // Given
        when(passwordResetService.requestPasswordReset(anyString())).thenReturn(true);

        // When
        ResponseEntity<?> response = passwordResetController.requestPasswordReset(requestDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) responseBody.get("success"));
        assertTrue(responseBody.get("message").toString().contains("Si votre email existe"));
        verify(passwordResetService, times(1)).requestPasswordReset("test@example.com");
    }

    /**
     * Test de demande de réinitialisation avec email inexistant
     * Note: Pour la sécurité, le même message est retourné
     */
    @Test
    @DisplayName("requestPasswordReset() - Email inexistant (même réponse)")
    void testRequestPasswordResetEmailNotFound() {
        // Given
        when(passwordResetService.requestPasswordReset(anyString())).thenReturn(false);

        // When
        ResponseEntity<?> response = passwordResetController.requestPasswordReset(requestDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) responseBody.get("success"));
        assertTrue(responseBody.get("message").toString().contains("Si votre email existe"));
        verify(passwordResetService, times(1)).requestPasswordReset("test@example.com");
    }

    /**
     * Test de demande de réinitialisation avec erreur technique
     */
    @Test
    @DisplayName("requestPasswordReset() - Erreur technique")
    void testRequestPasswordResetTechnicalError() {
        // Given
        when(passwordResetService.requestPasswordReset(anyString())).thenThrow(new RuntimeException("Erreur technique"));

        // When
        ResponseEntity<?> response = passwordResetController.requestPasswordReset(requestDTO);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertTrue(responseBody.get("message").toString().contains("erreur technique"));
    }

    /**
     * Test de validation du DTO pour la demande de réinitialisation
     */
    @Test
    @DisplayName("requestPasswordReset() - Validation du DTO")
    void testRequestPasswordResetDTOValidation() {
        // Given
        requestDTO.setEmail("user@domain.com");
        when(passwordResetService.requestPasswordReset(anyString())).thenReturn(true);

        // When
        passwordResetController.requestPasswordReset(requestDTO);

        // Then
        verify(passwordResetService, times(1)).requestPasswordReset("user@domain.com");
    }

    // ==================== TESTS NOTIFICATION DE CHANGEMENT ====================

    /**
     * Test de notification de changement de mot de passe réussie
     */
    @Test
    @DisplayName("notifyPasswordChanged() - Notification réussie")
    void testNotifyPasswordChangedSuccess() {
        // Given
        when(passwordResetService.notifyPasswordChanged(anyString())).thenReturn(true);

        // When
        ResponseEntity<?> response = passwordResetController.notifyPasswordChanged(requestDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) responseBody.get("success"));
        assertEquals("Email de confirmation envoyé", responseBody.get("message"));
        verify(passwordResetService, times(1)).notifyPasswordChanged("test@example.com");
    }

    /**
     * Test de notification avec utilisateur non trouvé
     */
    @Test
    @DisplayName("notifyPasswordChanged() - Utilisateur non trouvé")
    void testNotifyPasswordChangedUserNotFound() {
        // Given
        when(passwordResetService.notifyPasswordChanged(anyString())).thenReturn(false);

        // When
        ResponseEntity<?> response = passwordResetController.notifyPasswordChanged(requestDTO);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertEquals("Utilisateur non trouvé", responseBody.get("message"));
        verify(passwordResetService, times(1)).notifyPasswordChanged("test@example.com");
    }

    /**
     * Test de notification avec erreur technique
     */
    @Test
    @DisplayName("notifyPasswordChanged() - Erreur technique")
    void testNotifyPasswordChangedTechnicalError() {
        // Given
        when(passwordResetService.notifyPasswordChanged(anyString())).thenThrow(new RuntimeException("Erreur technique"));

        // When
        ResponseEntity<?> response = passwordResetController.notifyPasswordChanged(requestDTO);

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
     * Test de validation du DTO pour la notification
     */
    @Test
    @DisplayName("notifyPasswordChanged() - Validation du DTO")
    void testNotifyPasswordChangedDTOValidation() {
        // Given
        requestDTO.setEmail("changed@example.com");
        when(passwordResetService.notifyPasswordChanged(anyString())).thenReturn(true);

        // When
        passwordResetController.notifyPasswordChanged(requestDTO);

        // Then
        verify(passwordResetService, times(1)).notifyPasswordChanged("changed@example.com");
    }

    // ==================== TESTS STATUT DU SERVICE ====================

    /**
     * Test du statut du service de réinitialisation
     */
    @Test
    @DisplayName("getPasswordResetStatus() - Statut du service")
    void testGetPasswordResetStatus() {
        // When
        ResponseEntity<?> response = passwordResetController.getPasswordResetStatus();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("active", responseBody.get("status"));
        assertTrue(responseBody.get("message").toString().contains("opérationnel"));
    }

    // ==================== TESTS DE SÉCURITÉ ====================

    /**
     * Test de la logique de sécurité - même réponse pour email existant/inexistant
     */
    @Test
    @DisplayName("requestPasswordReset() - Logique de sécurité")
    void testRequestPasswordResetSecurityLogic() {
        // Test avec email existant
        when(passwordResetService.requestPasswordReset("existing@example.com")).thenReturn(true);
        requestDTO.setEmail("existing@example.com");
        ResponseEntity<?> responseExisting = passwordResetController.requestPasswordReset(requestDTO);

        // Test avec email inexistant
        when(passwordResetService.requestPasswordReset("nonexistent@example.com")).thenReturn(false);
        requestDTO.setEmail("nonexistent@example.com");
        ResponseEntity<?> responseNonExisting = passwordResetController.requestPasswordReset(requestDTO);

        // Then - Les deux réponses doivent être identiques pour la sécurité
        assertEquals(responseExisting.getStatusCode(), responseNonExisting.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> bodyExisting = (Map<String, Object>) responseExisting.getBody();
        @SuppressWarnings("unchecked")
        Map<String, Object> bodyNonExisting = (Map<String, Object>) responseNonExisting.getBody();
        assertEquals(bodyExisting.get("success"), bodyNonExisting.get("success"));
        assertEquals(bodyExisting.get("message"), bodyNonExisting.get("message"));
    }

    // ==================== TESTS AVEC DIFFÉRENTS FORMATS D'EMAIL ====================

    /**
     * Test avec différents formats d'email valides
     */
    @Test
    @DisplayName("requestPasswordReset() - Différents formats d'email")
    void testRequestPasswordResetDifferentEmailFormats() {
        // Given
        when(passwordResetService.requestPasswordReset(anyString())).thenReturn(true);

        String[] emails = {
                "simple@example.com",
                "user.name@example.com",
                "user+tag@example.org",
                "user123@sub.domain.co.uk"
        };

        for (String email : emails) {
            // When
            requestDTO.setEmail(email);
            ResponseEntity<?> response = passwordResetController.requestPasswordReset(requestDTO);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(passwordResetService, times(1)).requestPasswordReset(email);
        }
    }

    /**
     * Test de gestion des erreurs avec emails spéciaux
     */
    @Test
    @DisplayName("notifyPasswordChanged() - Gestion d'erreurs avec emails spéciaux")
    void testNotifyPasswordChangedSpecialEmails() {
        // Given
        when(passwordResetService.notifyPasswordChanged(anyString())).thenReturn(true);

        String[] specialEmails = {
                "test@localhost",
                "admin@internal.company",
                "support@test-domain.com"
        };

        for (String email : specialEmails) {
            // When
            requestDTO.setEmail(email);
            ResponseEntity<?> response = passwordResetController.notifyPasswordChanged(requestDTO);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(passwordResetService, times(1)).notifyPasswordChanged(email);
        }
    }

    /**
     * Test de configuration des mocks
     */
    @Test
    @DisplayName("Configuration des mocks")
    void testMockConfiguration() {
        // Vérifier que les mocks sont correctement injectés
        assertNotNull(passwordResetController);
        assertNotNull(passwordResetService);
    }
}