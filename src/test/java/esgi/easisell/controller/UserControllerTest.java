/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : UserControllerTest.java
 * @description : Tests unitaires pour le contrôleur des utilisateurs
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 11/07/2025
 * @package     : esgi.easisell.controller
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.controller;

import esgi.easisell.dto.*;
import esgi.easisell.entity.AdminUser;
import esgi.easisell.entity.Client;
import esgi.easisell.entity.DeletedUser;
import esgi.easisell.service.AdminUserService;
import esgi.easisell.service.ClientService;
import esgi.easisell.service.DeletedUserService;
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
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour le contrôleur des utilisateurs
 * Couverture 100% des lignes de code
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock(lenient = true)
    private ClientService clientService;

    @Mock(lenient = true)
    private AdminUserService adminUserService;

    @Mock(lenient = true)
    private DeletedUserService deletedUserService;

    @Mock(lenient = true)
    private PasswordResetService passwordResetService;

    @Mock(lenient = true)
    private Authentication authentication;

    @InjectMocks
    private UserController userController;

    private Client mockClient;
    private AdminUser mockAdmin;
    private DeletedUser mockDeletedUser;
    private UpdateClientDTO updateClientDTO;
    private UpdateAdminDTO updateAdminDTO;
    private ChangePasswordDTO changePasswordDTO;
    private AdminChangePasswordDTO adminChangePasswordDTO;
    private AccessCodeDTO accessCodeDTO;
    private UUID clientId;
    private UUID adminId;

    /**
     * Configuration initiale pour chaque test
     */
    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        adminId = UUID.randomUUID();

        mockClient = new Client();
        mockClient.setUserId(clientId);
        mockClient.setUsername("client@test.com");
        mockClient.setFirstName("John");
        mockClient.setName("Test Store"); // Propriété spécifique à Client
        mockClient.setRole("CLIENT");
        mockClient.setAccessCode("ABC123");

        mockAdmin = new AdminUser();
        mockAdmin.setUserId(adminId);
        mockAdmin.setUsername("admin@test.com");
        mockAdmin.setFirstName("Jane");
        mockAdmin.setRole("ADMIN");

        mockDeletedUser = new DeletedUser();
        mockDeletedUser.setId(UUID.randomUUID());
        mockDeletedUser.setOriginalUserId(clientId);
        mockDeletedUser.setUsername("deleted@test.com");
        mockDeletedUser.setUserType("CLIENT");
        mockDeletedUser.setDeletedBy("admin@test.com");
        mockDeletedUser.setDeletedAt(LocalDateTime.now());
        mockDeletedUser.setDeletionReason("Test deletion");

        updateClientDTO = new UpdateClientDTO();
        updateClientDTO.setName("Updated Client");
        updateClientDTO.setAddress("123 Updated Street");
        updateClientDTO.setContractStatus("ACTIVE");
        updateClientDTO.setCurrencyPreference("EUR");

        updateAdminDTO = new UpdateAdminDTO();
        updateAdminDTO.setFirstName("Updated Admin");
        updateAdminDTO.setUsername("admin-updated@test.com");

        changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setCurrentPassword("oldPassword");
        changePasswordDTO.setNewPassword("newPassword");

        adminChangePasswordDTO = new AdminChangePasswordDTO();
        adminChangePasswordDTO.setNewPassword("adminNewPassword");

        accessCodeDTO = new AccessCodeDTO();
        accessCodeDTO.setAccessCode("XYZ789");

        when(authentication.getName()).thenReturn("admin@test.com");
    }

    // ==================== TESTS GET ALL CLIENTS ====================

    /**
     * Test de récupération de tous les clients - Succès
     */
    @Test
    @DisplayName("✅ getAllClients() - Succès")
    void testGetAllClientsSuccess() {
        // Given
        List<Client> clients = Arrays.asList(mockClient);
        when(clientService.getAllUsers()).thenReturn(clients);

        // When
        ResponseEntity<List<ClientResponseDTO>> response = userController.getAllClients();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(mockClient.getUsername(), response.getBody().get(0).getUsername());
        verify(clientService, times(1)).getAllUsers();
    }

    /**
     * Test de récupération de tous les clients - Liste vide
     */
    @Test
    @DisplayName("✅ getAllClients() - Liste vide")
    void testGetAllClientsEmpty() {
        // Given
        when(clientService.getAllUsers()).thenReturn(new ArrayList<>());

        // When
        ResponseEntity<List<ClientResponseDTO>> response = userController.getAllClients();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    // ==================== TESTS GET CLIENT BY ID ====================

    /**
     * Test de récupération d'un client par ID - Succès
     */
    @Test
    @DisplayName("✅ getClientById() - Succès")
    void testGetClientByIdSuccess() {
        // Given
        when(clientService.getUserById(clientId)).thenReturn(Optional.of(mockClient));

        // When
        ResponseEntity<ClientResponseDTO> response = userController.getClientById(clientId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockClient.getUsername(), response.getBody().getUsername());
        verify(clientService, times(1)).getUserById(clientId);
    }

    /**
     * Test de récupération d'un client par ID - Client non trouvé
     */
    @Test
    @DisplayName("❌ getClientById() - Client non trouvé")
    void testGetClientByIdNotFound() {
        // Given
        when(clientService.getUserById(clientId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<ClientResponseDTO> response = userController.getClientById(clientId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(clientService, times(1)).getUserById(clientId);
    }

    // ==================== TESTS GET ALL ADMINS ====================

    /**
     * Test de récupération de tous les admins - Succès
     */
    @Test
    @DisplayName("✅ getAllAdmins() - Succès")
    void testGetAllAdminsSuccess() {
        // Given
        List<AdminUser> admins = Arrays.asList(mockAdmin);
        when(adminUserService.getAllUsers()).thenReturn(admins);

        // When
        ResponseEntity<List<AdminUser>> response = userController.getAllAdmins();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(mockAdmin.getUsername(), response.getBody().get(0).getUsername());
    }

    // ==================== TESTS GET ADMIN BY ID ====================

    /**
     * Test de récupération d'un admin par ID - Succès
     */
    @Test
    @DisplayName("✅ getAdminById() - Succès")
    void testGetAdminByIdSuccess() {
        // Given
        when(adminUserService.getUserById(adminId)).thenReturn(Optional.of(mockAdmin));

        // When
        ResponseEntity<AdminUser> response = userController.getAdminById(adminId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockAdmin, response.getBody());
    }

    /**
     * Test de récupération d'un admin par ID - Admin non trouvé
     */
    @Test
    @DisplayName("❌ getAdminById() - Admin non trouvé")
    void testGetAdminByIdNotFound() {
        // Given
        when(adminUserService.getUserById(adminId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<AdminUser> response = userController.getAdminById(adminId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ==================== TESTS DELETE CLIENT ====================

    /**
     * Test de suppression d'un client - Succès
     */
    @Test
    @DisplayName("✅ deleteClient() - Succès")
    void testDeleteClientSuccess() {
        // Given
        String reason = "Test deletion";
        when(clientService.getUserById(clientId)).thenReturn(Optional.of(mockClient));
        doNothing().when(clientService).deleteUserWithArchive(eq(clientId), anyString(), eq(reason));

        // When
        ResponseEntity<Void> response = userController.deleteClient(clientId, reason, authentication);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(clientService, times(1)).getUserById(clientId);
        verify(clientService, times(1)).deleteUserWithArchive(clientId, "admin@test.com", reason);
    }

    /**
     * Test de suppression d'un client - Client non trouvé
     */
    @Test
    @DisplayName("❌ deleteClient() - Client non trouvé")
    void testDeleteClientNotFound() {
        // Given
        when(clientService.getUserById(clientId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<Void> response = userController.deleteClient(clientId, "reason", authentication);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(clientService, never()).deleteUserWithArchive(any(), any(), any());
    }

    /**
     * Test de suppression d'un client - Sans raison
     */
    @Test
    @DisplayName("✅ deleteClient() - Sans raison")
    void testDeleteClientWithoutReason() {
        // Given
        when(clientService.getUserById(clientId)).thenReturn(Optional.of(mockClient));
        doNothing().when(clientService).deleteUserWithArchive(eq(clientId), anyString(), eq("Suppression manuelle"));

        // When
        ResponseEntity<Void> response = userController.deleteClient(clientId, null, authentication);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(clientService, times(1)).deleteUserWithArchive(clientId, "admin@test.com", "Suppression manuelle");
    }

    // ==================== TESTS DELETE ADMIN ====================

    /**
     * Test de suppression d'un admin - Succès
     */
    @Test
    @DisplayName("✅ deleteAdmin() - Succès")
    void testDeleteAdminSuccess() {
        // Given
        String reason = "Admin deletion";
        when(adminUserService.getUserById(adminId)).thenReturn(Optional.of(mockAdmin));
        doNothing().when(adminUserService).deleteUserWithArchive(eq(adminId), anyString(), eq(reason));

        // When
        ResponseEntity<Void> response = userController.deleteAdmin(adminId, reason, authentication);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(adminUserService, times(1)).deleteUserWithArchive(adminId, "admin@test.com", reason);
    }

    /**
     * Test de suppression d'un admin - Admin non trouvé
     */
    @Test
    @DisplayName("❌ deleteAdmin() - Admin non trouvé")
    void testDeleteAdminNotFound() {
        // Given
        when(adminUserService.getUserById(adminId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<Void> response = userController.deleteAdmin(adminId, "reason", authentication);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(adminUserService, never()).deleteUserWithArchive(any(), any(), any());
    }

    // ==================== TESTS UPDATE CLIENT ====================

    /**
     * Test de mise à jour d'un client - Succès
     */
    @Test
    @DisplayName("✅ updateClient() - Succès")
    void testUpdateClientSuccess() {
        // Given
        when(clientService.updateUser(clientId, updateClientDTO)).thenReturn(Optional.of(mockClient));

        // When
        ResponseEntity<ClientResponseDTO> response = userController.updateClient(clientId, updateClientDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(clientService, times(1)).updateUser(clientId, updateClientDTO);
    }

    /**
     * Test de mise à jour d'un client - Client non trouvé
     */
    @Test
    @DisplayName("❌ updateClient() - Client non trouvé")
    void testUpdateClientNotFound() {
        // Given
        when(clientService.updateUser(clientId, updateClientDTO)).thenReturn(Optional.empty());

        // When
        ResponseEntity<ClientResponseDTO> response = userController.updateClient(clientId, updateClientDTO);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ==================== TESTS UPDATE ADMIN ====================

    /**
     * Test de mise à jour d'un admin - Succès
     */
    @Test
    @DisplayName("✅ updateAdmin() - Succès")
    void testUpdateAdminSuccess() {
        // Given
        when(adminUserService.updateUser(adminId, updateAdminDTO)).thenReturn(Optional.of(mockAdmin));

        // When
        ResponseEntity<AdminUser> response = userController.updateAdmin(adminId, updateAdminDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockAdmin, response.getBody());
    }

    /**
     * Test de mise à jour d'un admin - Admin non trouvé
     */
    @Test
    @DisplayName("❌ updateAdmin() - Admin non trouvé")
    void testUpdateAdminNotFound() {
        // Given
        when(adminUserService.updateUser(adminId, updateAdminDTO)).thenReturn(Optional.empty());

        // When
        ResponseEntity<AdminUser> response = userController.updateAdmin(adminId, updateAdminDTO);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ==================== TESTS CHANGE PASSWORD CLIENT ====================

    /**
     * Test de changement de mot de passe client - Succès
     */
    @Test
    @DisplayName("✅ changeClientPassword() - Succès")
    void testChangeClientPasswordSuccess() {
        // Given
        when(clientService.changePassword(clientId, changePasswordDTO)).thenReturn(Optional.of(mockClient));

        // When
        ResponseEntity<String> response = userController.changeClientPassword(clientId, changePasswordDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Mot de passe modifié avec succès", response.getBody());
    }

    /**
     * Test de changement de mot de passe client - Client non trouvé
     */
    @Test
    @DisplayName("❌ changeClientPassword() - Client non trouvé")
    void testChangeClientPasswordNotFound() {
        // Given
        when(clientService.changePassword(clientId, changePasswordDTO)).thenReturn(Optional.empty());

        // When
        ResponseEntity<String> response = userController.changeClientPassword(clientId, changePasswordDTO);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Test de changement de mot de passe client - Argument invalide
     */
    @Test
    @DisplayName("❌ changeClientPassword() - Argument invalide")
    void testChangeClientPasswordInvalidArgument() {
        // Given
        when(clientService.changePassword(clientId, changePasswordDTO))
                .thenThrow(new IllegalArgumentException("Ancien mot de passe incorrect"));

        // When
        ResponseEntity<String> response = userController.changeClientPassword(clientId, changePasswordDTO);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Ancien mot de passe incorrect", response.getBody());
    }

    // ==================== TESTS CHANGE PASSWORD ADMIN ====================

    /**
     * Test de changement de mot de passe admin - Succès
     */
    @Test
    @DisplayName("✅ changeAdminPassword() - Succès")
    void testChangeAdminPasswordSuccess() {
        // Given
        when(adminUserService.changePassword(adminId, changePasswordDTO)).thenReturn(Optional.of(mockAdmin));

        // When
        ResponseEntity<String> response = userController.changeAdminPassword(adminId, changePasswordDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Mot de passe modifié avec succès", response.getBody());
    }

    /**
     * Test de changement de mot de passe admin - Admin non trouvé
     */
    @Test
    @DisplayName("❌ changeAdminPassword() - Admin non trouvé")
    void testChangeAdminPasswordNotFound() {
        // Given
        when(adminUserService.changePassword(adminId, changePasswordDTO)).thenReturn(Optional.empty());

        // When
        ResponseEntity<String> response = userController.changeAdminPassword(adminId, changePasswordDTO);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ==================== TESTS ADMIN CHANGE CLIENT PASSWORD ====================

    /**
     * Test de changement de mot de passe client par admin - Succès
     */
    @Test
    @DisplayName("✅ adminChangeClientPassword() - Succès")
    void testAdminChangeClientPasswordSuccess() {
        // Given
        when(clientService.adminChangePassword(clientId, adminChangePasswordDTO)).thenReturn(Optional.of(mockClient));
        when(passwordResetService.notifyPasswordChanged(mockClient.getUsername())).thenReturn(true);

        // When
        ResponseEntity<String> response = userController.adminChangeClientPassword(clientId, adminChangePasswordDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Mot de passe du client modifié avec succès"));
        verify(passwordResetService, times(1)).notifyPasswordChanged(mockClient.getUsername());
    }

    /**
     * Test de changement de mot de passe client par admin - Client non trouvé
     */
    @Test
    @DisplayName("❌ adminChangeClientPassword() - Client non trouvé")
    void testAdminChangeClientPasswordNotFound() {
        // Given
        when(clientService.adminChangePassword(clientId, adminChangePasswordDTO)).thenReturn(Optional.empty());

        // When
        ResponseEntity<String> response = userController.adminChangeClientPassword(clientId, adminChangePasswordDTO);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(passwordResetService, never()).notifyPasswordChanged(any());
    }

    /**
     * Test de changement de mot de passe client par admin - Erreur email
     */
    @Test
    @DisplayName("✅ adminChangeClientPassword() - Erreur email mais succès")
    void testAdminChangeClientPasswordEmailError() {
        // Given
        when(clientService.adminChangePassword(clientId, adminChangePasswordDTO)).thenReturn(Optional.of(mockClient));
        when(passwordResetService.notifyPasswordChanged(mockClient.getUsername())).thenThrow(new RuntimeException("Erreur email"));

        // When
        ResponseEntity<String> response = userController.adminChangeClientPassword(clientId, adminChangePasswordDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Mot de passe du client modifié avec succès"));
    }

    /**
     * Test de changement de mot de passe client par admin - Exception générale
     */
    @Test
    @DisplayName("❌ adminChangeClientPassword() - Exception générale")
    void testAdminChangeClientPasswordException() {
        // Given
        when(clientService.adminChangePassword(clientId, adminChangePasswordDTO))
                .thenThrow(new RuntimeException("Erreur interne"));

        // When
        ResponseEntity<String> response = userController.adminChangeClientPassword(clientId, adminChangePasswordDTO);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Erreur lors de la modification", response.getBody());
    }

    // ==================== TESTS ADMIN CHANGE ADMIN PASSWORD ====================

    /**
     * Test de changement de mot de passe admin par admin - Succès
     */
    @Test
    @DisplayName("✅ adminChangeAdminPassword() - Succès")
    void testAdminChangeAdminPasswordSuccess() {
        // Given
        when(adminUserService.adminChangePassword(adminId, adminChangePasswordDTO)).thenReturn(Optional.of(mockAdmin));
        when(passwordResetService.notifyPasswordChanged(mockAdmin.getUsername())).thenReturn(true);

        // When
        ResponseEntity<String> response = userController.adminChangeAdminPassword(adminId, adminChangePasswordDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Mot de passe de l'admin modifié avec succès"));
        verify(passwordResetService, times(1)).notifyPasswordChanged(mockAdmin.getUsername());
    }

    /**
     * Test de changement de mot de passe admin par admin - Admin non trouvé
     */
    @Test
    @DisplayName("❌ adminChangeAdminPassword() - Admin non trouvé")
    void testAdminChangeAdminPasswordNotFound() {
        // Given
        when(adminUserService.adminChangePassword(adminId, adminChangePasswordDTO)).thenReturn(Optional.empty());

        // When
        ResponseEntity<String> response = userController.adminChangeAdminPassword(adminId, adminChangePasswordDTO);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ==================== TESTS DELETED USERS ====================

    /**
     * Test de récupération de tous les utilisateurs supprimés - Succès
     */
    @Test
    @DisplayName("✅ getAllDeletedUsers() - Succès")
    void testGetAllDeletedUsersSuccess() {
        // Given
        List<DeletedUser> deletedUsers = Arrays.asList(mockDeletedUser);
        when(deletedUserService.getAllDeletedUsers()).thenReturn(deletedUsers);

        // When
        ResponseEntity<List<DeletedUser>> response = userController.getAllDeletedUsers();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(mockDeletedUser.getUsername(), response.getBody().get(0).getUsername());
    }

    /**
     * Test de récupération des clients supprimés - Succès
     */
    @Test
    @DisplayName("✅ getDeletedClients() - Succès")
    void testGetDeletedClientsSuccess() {
        // Given
        List<DeletedUser> deletedClients = Arrays.asList(mockDeletedUser);
        when(deletedUserService.getDeletedUsersByType("CLIENT")).thenReturn(deletedClients);

        // When
        ResponseEntity<List<DeletedUser>> response = userController.getDeletedClients();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    /**
     * Test de récupération des admins supprimés - Succès
     */
    @Test
    @DisplayName("✅ getDeletedAdmins() - Succès")
    void testGetDeletedAdminsSuccess() {
        // Given
        List<DeletedUser> deletedAdmins = Arrays.asList(mockDeletedUser);
        when(deletedUserService.getDeletedUsersByType("ADMIN")).thenReturn(deletedAdmins);

        // When
        ResponseEntity<List<DeletedUser>> response = userController.getDeletedAdmins();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    /**
     * Test de récupération des utilisateurs supprimés par supprimeur - Succès
     */
    @Test
    @DisplayName("✅ getDeletedUsersByDeleter() - Succès")
    void testGetDeletedUsersByDeleterSuccess() {
        // Given
        String deleter = "admin@test.com";
        List<DeletedUser> deletedUsers = Arrays.asList(mockDeletedUser);
        when(deletedUserService.getDeletedUsersByDeleter(deleter)).thenReturn(deletedUsers);

        // When
        ResponseEntity<List<DeletedUser>> response = userController.getDeletedUsersByDeleter(deleter);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    // ==================== TESTS ACCESS CODE ====================

    /**
     * Test de récupération du code d'accès client - Succès
     */
    @Test
    @DisplayName("✅ getClientAccessCode() - Succès")
    void testGetClientAccessCodeSuccess() {
        // Given
        when(clientService.getAccessCode(clientId)).thenReturn(Optional.of("ABC123"));

        // When
        ResponseEntity<AccessCodeDTO> response = userController.getClientAccessCode(clientId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ABC123", response.getBody().getAccessCode());
    }

    /**
     * Test de récupération du code d'accès client - Client non trouvé
     */
    @Test
    @DisplayName("❌ getClientAccessCode() - Client non trouvé")
    void testGetClientAccessCodeNotFound() {
        // Given
        when(clientService.getAccessCode(clientId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<AccessCodeDTO> response = userController.getClientAccessCode(clientId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Test de régénération du code d'accès client - Succès
     */
    @Test
    @DisplayName("✅ regenerateClientAccessCode() - Succès")
    void testRegenerateClientAccessCodeSuccess() {
        // Given
        when(clientService.regenerateAccessCode(clientId)).thenReturn(Optional.of(mockClient));

        // When
        ResponseEntity<AccessCodeDTO> response = userController.regenerateClientAccessCode(clientId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockClient.getAccessCode(), response.getBody().getAccessCode());
    }

    /**
     * Test de régénération du code d'accès client - Client non trouvé
     */
    @Test
    @DisplayName("❌ regenerateClientAccessCode() - Client non trouvé")
    void testRegenerateClientAccessCodeNotFound() {
        // Given
        when(clientService.regenerateAccessCode(clientId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<AccessCodeDTO> response = userController.regenerateClientAccessCode(clientId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Test de vérification du code d'accès - Code valide
     */
    @Test
    @DisplayName("✅ verifyAccessCode() - Code valide")
    void testVerifyAccessCodeValid() {
        // Given
        when(clientService.verifyAccessCode(clientId, accessCodeDTO.getAccessCode())).thenReturn(true);

        // When
        ResponseEntity<Map<String, Boolean>> response = userController.verifyAccessCode(clientId, accessCodeDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("valid"));
    }

    /**
     * Test de vérification du code d'accès - Code invalide
     */
    @Test
    @DisplayName("❌ verifyAccessCode() - Code invalide")
    void testVerifyAccessCodeInvalid() {
        // Given
        when(clientService.verifyAccessCode(clientId, accessCodeDTO.getAccessCode())).thenReturn(false);

        // When
        ResponseEntity<Map<String, Boolean>> response = userController.verifyAccessCode(clientId, accessCodeDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().get("valid"));
    }

    /**
     * Test de définition d'un code d'accès personnalisé - Succès
     */
    @Test
    @DisplayName("✅ setCustomAccessCode() - Succès")
    void testSetCustomAccessCodeSuccess() {
        // Given
        when(clientService.setCustomAccessCode(clientId, accessCodeDTO.getAccessCode())).thenReturn(Optional.of(mockClient));

        // When
        ResponseEntity<AccessCodeDTO> response = userController.setCustomAccessCode(clientId, accessCodeDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockClient.getAccessCode(), response.getBody().getAccessCode());
        verify(clientService, times(1)).setCustomAccessCode(clientId, accessCodeDTO.getAccessCode());
    }

    /**
     * Test de définition d'un code d'accès personnalisé - Client non trouvé
     */
    @Test
    @DisplayName("❌ setCustomAccessCode() - Client non trouvé")
    void testSetCustomAccessCodeNotFound() {
        // Given
        when(clientService.setCustomAccessCode(clientId, accessCodeDTO.getAccessCode())).thenReturn(Optional.empty());

        // When
        ResponseEntity<AccessCodeDTO> response = userController.setCustomAccessCode(clientId, accessCodeDTO);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(clientService, times(1)).setCustomAccessCode(clientId, accessCodeDTO.getAccessCode());
    }

    // ==================== TESTS DE VALIDATION SUPPLÉMENTAIRES ====================

    /**
     * Test de validation - Services correctement injectés
     */
    @Test
    @DisplayName("✅ Configuration des mocks")
    void testMockConfiguration() {
        // Vérifier que les mocks sont injectés
        assertNotNull(userController);
        assertNotNull(clientService);
        assertNotNull(adminUserService);
        assertNotNull(deletedUserService);
        assertNotNull(passwordResetService);
    }

    /**
     * Test de comportement - Gestion des collections vides
     */
    @Test
    @DisplayName("✅ Gestion des collections vides")
    void testEmptyCollectionsHandling() {
        // Given
        when(clientService.getAllUsers()).thenReturn(new ArrayList<>());
        when(adminUserService.getAllUsers()).thenReturn(new ArrayList<>());
        when(deletedUserService.getAllDeletedUsers()).thenReturn(new ArrayList<>());

        // When & Then
        ResponseEntity<List<ClientResponseDTO>> clientsResponse = userController.getAllClients();
        assertEquals(HttpStatus.OK, clientsResponse.getStatusCode());
        assertTrue(clientsResponse.getBody().isEmpty());

        ResponseEntity<List<AdminUser>> adminsResponse = userController.getAllAdmins();
        assertEquals(HttpStatus.OK, adminsResponse.getStatusCode());
        assertTrue(adminsResponse.getBody().isEmpty());

        ResponseEntity<List<DeletedUser>> deletedResponse = userController.getAllDeletedUsers();
        assertEquals(HttpStatus.OK, deletedResponse.getStatusCode());
        assertTrue(deletedResponse.getBody().isEmpty());
    }

    /**
     * Test de cas limite - UUIDs null ou invalides
     */
    @Test
    @DisplayName("❌ Gestion des UUIDs null")
    void testNullUUIDHandling() {
        // Given
        UUID nullId = null;

        // When & Then - Les services doivent gérer les UUIDs null
        when(clientService.getUserById(nullId)).thenReturn(Optional.empty());
        when(adminUserService.getUserById(nullId)).thenReturn(Optional.empty());

        ResponseEntity<ClientResponseDTO> clientResponse = userController.getClientById(nullId);
        assertEquals(HttpStatus.NOT_FOUND, clientResponse.getStatusCode());

        ResponseEntity<AdminUser> adminResponse = userController.getAdminById(nullId);
        assertEquals(HttpStatus.NOT_FOUND, adminResponse.getStatusCode());
    }

    /**
     * Test de vérification des interactions avec les services
     */
    @Test
    @DisplayName("✅ Vérification des interactions services")
    void testServiceInteractions() {
        // Given
        when(clientService.getAllUsers()).thenReturn(Arrays.asList(mockClient));
        when(adminUserService.getAllUsers()).thenReturn(Arrays.asList(mockAdmin));

        // When
        userController.getAllClients();
        userController.getAllAdmins();

        // Then
        verify(clientService, times(1)).getAllUsers();
        verify(adminUserService, times(1)).getAllUsers();
    }

    /**
     * Test de validation des DTOs retournés
     */
    @Test
    @DisplayName("✅ Validation des DTOs")
    void testDTOValidation() {
        // Given
        when(clientService.getUserById(clientId)).thenReturn(Optional.of(mockClient));

        // When
        ResponseEntity<ClientResponseDTO> response = userController.getClientById(clientId);

        // Then
        assertNotNull(response.getBody());
        ClientResponseDTO dto = response.getBody();
        assertEquals(mockClient.getUserId(), dto.getUserId());
        assertEquals(mockClient.getUsername(), dto.getUsername());
        assertEquals(mockClient.getName(), dto.getName()); // name vient de Client
        assertEquals(mockClient.getFirstName(), dto.getFirstName()); // firstName vient de User
        assertEquals(mockClient.getRole(), dto.getRole());
    }

    /**
     * Test de gestion des exceptions dans les changements de mot de passe
     */
    @Test
    @DisplayName("❌ Exceptions changement mot de passe")
    void testPasswordChangeExceptions() {
        // Test exception pour client
        when(clientService.changePassword(clientId, changePasswordDTO))
                .thenThrow(new IllegalArgumentException("Mot de passe trop faible"));

        ResponseEntity<String> clientResponse = userController.changeClientPassword(clientId, changePasswordDTO);
        assertEquals(HttpStatus.BAD_REQUEST, clientResponse.getStatusCode());
        assertEquals("Mot de passe trop faible", clientResponse.getBody());

        // Test exception pour admin
        when(adminUserService.changePassword(adminId, changePasswordDTO))
                .thenThrow(new IllegalArgumentException("Mot de passe requis"));

        ResponseEntity<String> adminResponse = userController.changeAdminPassword(adminId, changePasswordDTO);
        assertEquals(HttpStatus.BAD_REQUEST, adminResponse.getStatusCode());
        assertEquals("Mot de passe requis", adminResponse.getBody());
    }

    /**
     * Test de vérification des paramètres optionnels
     */
    @Test
    @DisplayName("✅ Paramètres optionnels")
    void testOptionalParameters() {
        // Given
        when(clientService.getUserById(clientId)).thenReturn(Optional.of(mockClient));
        when(adminUserService.getUserById(adminId)).thenReturn(Optional.of(mockAdmin));

        // When - Test sans raison de suppression
        ResponseEntity<Void> clientDeleteResponse = userController.deleteClient(clientId, null, authentication);
        ResponseEntity<Void> adminDeleteResponse = userController.deleteAdmin(adminId, null, authentication);

        // Then - Doit utiliser la raison par défaut
        assertEquals(HttpStatus.NO_CONTENT, clientDeleteResponse.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, adminDeleteResponse.getStatusCode());

        verify(clientService, times(1)).deleteUserWithArchive(clientId, "admin@test.com", "Suppression manuelle");
        verify(adminUserService, times(1)).deleteUserWithArchive(adminId, "admin@test.com", "Suppression manuelle");
    }

    /**
     * Test de consistance des réponses HTTP
     */
    @Test
    @DisplayName("✅ Consistance des codes HTTP")
    void testHttpStatusConsistency() {
        // Test des codes 200 OK
        when(clientService.getAllUsers()).thenReturn(Arrays.asList(mockClient));
        assertEquals(HttpStatus.OK, userController.getAllClients().getStatusCode());

        // Test des codes 404 NOT_FOUND
        when(clientService.getUserById(clientId)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND, userController.getClientById(clientId).getStatusCode());

        // Test des codes 204 NO_CONTENT
        when(clientService.getUserById(clientId)).thenReturn(Optional.of(mockClient));
        assertEquals(HttpStatus.NO_CONTENT, userController.deleteClient(clientId, "test", authentication).getStatusCode());
    }
}