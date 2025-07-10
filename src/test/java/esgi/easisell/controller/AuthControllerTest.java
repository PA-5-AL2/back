/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : AuthControllerTest.java
 * @description : Tests unitaires pour le contrôleur d'authentification
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 10/07/2025
 * @package     : esgi.easisell.controller
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.controller;

import esgi.easisell.dto.AuthDTO;
import esgi.easisell.entity.Client;
import esgi.easisell.exception.EmailException;
import esgi.easisell.service.AuthService;
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
import org.springframework.security.core.AuthenticationException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour le contrôleur d'authentification
 *
 * Tests des méthodes :
 * - register()
 * - login()
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthController authController;

    private AuthDTO authDTO;
    private Client mockClient;

    /**
     * Configuration initiale pour chaque test
     */
    @BeforeEach
    void setUp() {
        authDTO = new AuthDTO();
        authDTO.setUsername("test@store.com");
        authDTO.setPassword("password123");
        authDTO.setRole("client");

        mockClient = new Client();
        mockClient.setUserId(UUID.randomUUID());
        mockClient.setUsername("test@store.com");
        mockClient.setName("Test Store");
        mockClient.setRole("CLIENT");
    }

    // ==================== TESTS D'INSCRIPTION ====================

    /**
     * Test d'inscription réussie avec envoi d'email pour un client
     */
    @Test
    @DisplayName("register() - Inscription client réussie avec email")
    void testRegisterClientSuccess() throws EmailException {
        // Given
        when(authService.isUsernameAvailable(authDTO.getUsername())).thenReturn(true);
        when(authService.registerUser(any(AuthDTO.class))).thenReturn(mockClient);
        doNothing().when(emailService).sendPreRegistrationEmail(any(), anyString());

        // When
        ResponseEntity<?> response = authController.register(authDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockClient, response.getBody());
        verify(emailService, times(1)).sendPreRegistrationEmail(mockClient, authDTO.getPassword());
    }

    /**
     * Test d'inscription réussie sans email pour un admin
     */
    @Test
    @DisplayName("register() - Inscription admin réussie sans email")
    void testRegisterAdminSuccess() throws EmailException {
        // Given
        authDTO.setRole("admin");
        mockClient.setRole("ADMIN");

        when(authService.isUsernameAvailable(authDTO.getUsername())).thenReturn(true);
        when(authService.registerUser(any(AuthDTO.class))).thenReturn(mockClient);

        // When
        ResponseEntity<?> response = authController.register(authDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockClient, response.getBody());
        verify(emailService, never()).sendPreRegistrationEmail(any(), anyString());
    }

    /**
     * Test d'inscription échouée - username déjà utilisé
     */
    @Test
    @DisplayName("register() - Username déjà utilisé")
    void testRegisterUsernameAlreadyExists() throws EmailException {
        // Given
        when(authService.isUsernameAvailable(authDTO.getUsername())).thenReturn(false);

        // When
        ResponseEntity<?> response = authController.register(authDTO);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Username is already in use", response.getBody());
        verify(authService, never()).registerUser(any(AuthDTO.class));
        verify(emailService, never()).sendPreRegistrationEmail(any(), anyString());
    }

    /**
     * Test d'inscription échouée - IllegalArgumentException
     */
    @Test
    @DisplayName("register() - Données invalides")
    void testRegisterInvalidData() {
        // Given
        when(authService.isUsernameAvailable(authDTO.getUsername())).thenReturn(true);
        when(authService.registerUser(any(AuthDTO.class)))
                .thenThrow(new IllegalArgumentException("Données invalides"));

        // When
        ResponseEntity<?> response = authController.register(authDTO);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Données invalides", response.getBody());
    }

    /**
     * Test d'inscription échouée - Exception générale
     */
    @Test
    @DisplayName("register() - Erreur interne")
    void testRegisterInternalError() {
        // Given
        when(authService.isUsernameAvailable(authDTO.getUsername())).thenReturn(true);
        when(authService.registerUser(any(AuthDTO.class)))
                .thenThrow(new RuntimeException("Erreur base de données"));

        // When
        ResponseEntity<?> response = authController.register(authDTO);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Error creating user: Erreur base de données"));
    }

    // ==================== TESTS D'AUTHENTIFICATION ====================

    /**
     * Test de connexion réussie
     */
    @Test
    @DisplayName("login() - Connexion réussie")
    void testLoginSuccess() {
        // Given
        Map<String, Object> loginResponse = new HashMap<>();
        loginResponse.put("token", "eyJhbGciOiJIUzI1NiJ9...");
        loginResponse.put("user", mockClient);
        loginResponse.put("expiresIn", 3600);

        when(authService.authenticateUser(any(AuthDTO.class))).thenReturn(loginResponse);

        // When
        ResponseEntity<?> response = authController.login(authDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(loginResponse, response.getBody());
    }

    /**
     * Test de connexion échouée - credentials invalides
     */
    @Test
    @DisplayName("login() - Credentials invalides")
    void testLoginInvalidCredentials() {
        // Given
        when(authService.authenticateUser(any(AuthDTO.class)))
                .thenThrow(new AuthenticationException("Invalid credentials") {});

        // When
        ResponseEntity<?> response = authController.login(authDTO);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid username or password", response.getBody());
    }

    /**
     * Test de connexion avec données manquantes
     */
    @Test
    @DisplayName("login() - Données manquantes")
    void testLoginMissingData() {
        // Given
        AuthDTO incompleteAuthDTO = new AuthDTO();
        incompleteAuthDTO.setUsername("test@store.com");
        // password manquant

        when(authService.authenticateUser(any(AuthDTO.class)))
                .thenThrow(new AuthenticationException("Missing password") {});

        // When
        ResponseEntity<?> response = authController.login(incompleteAuthDTO);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid username or password", response.getBody());
    }

    // ==================== TESTS DE VALIDATION MÉTIER ====================

    /**
     * Test de validation - username null
     */
    @Test
    @DisplayName("register() - Username null")
    void testRegisterNullUsername() {
        // Given
        authDTO.setUsername(null);

        // When
        ResponseEntity<?> response = authController.register(authDTO);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(authService, never()).registerUser(any(AuthDTO.class));
    }

    /**
     * Test de validation - password null
     */
    @Test
    @DisplayName("login() - Password null")
    void testLoginNullPassword() {
        // Given
        authDTO.setPassword(null);
        when(authService.authenticateUser(any(AuthDTO.class)))
                .thenThrow(new AuthenticationException("Password required") {});

        // When
        ResponseEntity<?> response = authController.login(authDTO);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Test que les mocks sont correctement injectés
     */
    @Test
    @DisplayName("Configuration des mocks")
    void testMockConfiguration() {
        // Vérifier que les mocks sont injectés
        assertNotNull(authController);
        assertNotNull(authService);
        assertNotNull(emailService);
    }

    /**
     * Test de la logique métier - email envoyé seulement pour clients
     */
    @Test
    @DisplayName("Logique métier - Email seulement pour clients")
    void testEmailOnlyForClients() throws EmailException {
        // Given - Client
        authDTO.setRole("client");
        when(authService.isUsernameAvailable(authDTO.getUsername())).thenReturn(true);
        when(authService.registerUser(any(AuthDTO.class))).thenReturn(mockClient);

        // When
        authController.register(authDTO);

        // Then - Email envoyé
        verify(emailService, times(1)).sendPreRegistrationEmail(any(), anyString());

        // Given - Admin
        reset(emailService);
        authDTO.setRole("admin");

        // When
        authController.register(authDTO);

        // Then - Aucun email envoyé
        verify(emailService, never()).sendPreRegistrationEmail(any(), anyString());
    }
}