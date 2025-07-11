package esgi.easisell.service;

import esgi.easisell.entity.AdminUser;
import esgi.easisell.entity.Client;
import esgi.easisell.entity.DeletedUser;
import esgi.easisell.entity.User;
import esgi.easisell.repository.DeletedUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour DeletedUserService")
class DeletedUserServiceTest {

    @Mock
    private DeletedUserRepository deletedUserRepository;

    @InjectMocks
    private DeletedUserService deletedUserService;

    private Client testClient;
    private AdminUser testAdminUser;
    private DeletedUser testDeletedClient;
    private DeletedUser testDeletedAdmin;

    @BeforeEach
    void setUp() {
        // Setup Client
        testClient = new Client();
        testClient.setUserId(UUID.randomUUID());
        testClient.setUsername("client@test.com");
        testClient.setFirstName("John");
        testClient.setName("Supérette John");
        testClient.setAddress("123 Rue de la Paix");
        testClient.setContractStatus("ACTIVE");
        testClient.setCurrencyPreference("EUR");

        // Setup AdminUser
        testAdminUser = new AdminUser();
        testAdminUser.setUserId(UUID.randomUUID());
        testAdminUser.setUsername("admin@test.com");
        testAdminUser.setFirstName("Admin");

        // Setup DeletedUser pour Client
        testDeletedClient = DeletedUser.builder()
                .originalUserId(testClient.getUserId())
                .username(testClient.getUsername())
                .firstName(testClient.getFirstName())
                .userType("CLIENT")
                .clientName(testClient.getName())
                .address(testClient.getAddress())
                .contractStatus(testClient.getContractStatus())
                .currencyPreference(testClient.getCurrencyPreference())
                .deletedBy("admin@test.com")
                .deletionReason("Test deletion")
                .deletedAt(LocalDateTime.now())
                .build();

        // Setup DeletedUser pour Admin
        testDeletedAdmin = DeletedUser.builder()
                .originalUserId(testAdminUser.getUserId())
                .username(testAdminUser.getUsername())
                .firstName(testAdminUser.getFirstName())
                .userType("ADMIN")
                .deletedBy("super.admin@test.com")
                .deletionReason("Admin removal")
                .deletedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Devrait archiver un client avec succès")
    void archiveUser_ShouldArchiveClient_WhenClientProvided() {
        // Given
        String deletedBy = "admin@test.com";
        String reason = "Test deletion";
        when(deletedUserRepository.save(any(DeletedUser.class))).thenReturn(testDeletedClient);

        // When
        deletedUserService.archiveUser(testClient, deletedBy, reason);

        // Then
        verify(deletedUserRepository, times(1)).save(argThat(deletedUser -> {
            assertEquals(testClient.getUserId(), deletedUser.getOriginalUserId());
            assertEquals(testClient.getUsername(), deletedUser.getUsername());
            assertEquals(testClient.getFirstName(), deletedUser.getFirstName());
            assertEquals("CLIENT", deletedUser.getUserType());
            assertEquals(testClient.getName(), deletedUser.getClientName());
            assertEquals(testClient.getAddress(), deletedUser.getAddress());
            assertEquals(testClient.getContractStatus(), deletedUser.getContractStatus());
            assertEquals(testClient.getCurrencyPreference(), deletedUser.getCurrencyPreference());
            assertEquals(deletedBy, deletedUser.getDeletedBy());
            assertEquals(reason, deletedUser.getDeletionReason());
            return true;
        }));
    }

    @Test
    @DisplayName("Devrait archiver un admin avec succès")
    void archiveUser_ShouldArchiveAdmin_WhenAdminProvided() {
        // Given
        String deletedBy = "super.admin@test.com";
        String reason = "Admin removal";
        when(deletedUserRepository.save(any(DeletedUser.class))).thenReturn(testDeletedAdmin);

        // When
        deletedUserService.archiveUser(testAdminUser, deletedBy, reason);

        // Then
        verify(deletedUserRepository, times(1)).save(argThat(deletedUser -> {
            assertEquals(testAdminUser.getUserId(), deletedUser.getOriginalUserId());
            assertEquals(testAdminUser.getUsername(), deletedUser.getUsername());
            assertEquals(testAdminUser.getFirstName(), deletedUser.getFirstName());
            assertEquals("ADMIN", deletedUser.getUserType());
            assertEquals(deletedBy, deletedUser.getDeletedBy());
            assertEquals(reason, deletedUser.getDeletionReason());
            // Les champs spécifiques au client doivent être null
            assertNull(deletedUser.getClientName());
            assertNull(deletedUser.getAddress());
            assertNull(deletedUser.getContractStatus());
            assertNull(deletedUser.getCurrencyPreference());
            return true;
        }));
    }

    @Test
    @DisplayName("Devrait retourner tous les utilisateurs supprimés")
    void getAllDeletedUsers_ShouldReturnAllDeletedUsers() {
        // Given
        List<DeletedUser> deletedUsers = Arrays.asList(testDeletedClient, testDeletedAdmin);
        when(deletedUserRepository.findAll()).thenReturn(deletedUsers);

        // When
        List<DeletedUser> result = deletedUserService.getAllDeletedUsers();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testDeletedClient, result.get(0));
        assertEquals(testDeletedAdmin, result.get(1));
        verify(deletedUserRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Devrait retourner les utilisateurs supprimés par type")
    void getDeletedUsersByType_ShouldReturnUsersByType() {
        // Given
        String userType = "CLIENT";
        List<DeletedUser> deletedClients = Arrays.asList(testDeletedClient);
        when(deletedUserRepository.findByUserTypeOrderByDeletedAtDesc(userType))
                .thenReturn(deletedClients);

        // When
        List<DeletedUser> result = deletedUserService.getDeletedUsersByType(userType);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testDeletedClient, result.get(0));
        assertEquals("CLIENT", result.get(0).getUserType());
        verify(deletedUserRepository, times(1)).findByUserTypeOrderByDeletedAtDesc(userType);
    }

    @Test
    @DisplayName("Devrait retourner les utilisateurs supprimés par qui les a supprimés")
    void getDeletedUsersByDeleter_ShouldReturnUsersByDeleter() {
        // Given
        String deletedBy = "admin@test.com";
        List<DeletedUser> deletedByAdmin = Arrays.asList(testDeletedClient);
        when(deletedUserRepository.findByDeletedByOrderByDeletedAtDesc(deletedBy))
                .thenReturn(deletedByAdmin);

        // When
        List<DeletedUser> result = deletedUserService.getDeletedUsersByDeleter(deletedBy);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testDeletedClient, result.get(0));
        assertEquals(deletedBy, result.get(0).getDeletedBy());
        verify(deletedUserRepository, times(1)).findByDeletedByOrderByDeletedAtDesc(deletedBy);
    }

    @Test
    @DisplayName("Devrait retourner une liste vide quand aucun utilisateur supprimé")
    void getAllDeletedUsers_ShouldReturnEmptyList_WhenNoDeletedUsers() {
        // Given
        when(deletedUserRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<DeletedUser> result = deletedUserService.getAllDeletedUsers();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(deletedUserRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Devrait retourner une liste vide pour un type d'utilisateur inexistant")
    void getDeletedUsersByType_ShouldReturnEmptyList_WhenNoUsersOfType() {
        // Given
        String userType = "UNKNOWN_TYPE";
        when(deletedUserRepository.findByUserTypeOrderByDeletedAtDesc(userType))
                .thenReturn(Arrays.asList());

        // When
        List<DeletedUser> result = deletedUserService.getDeletedUsersByType(userType);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(deletedUserRepository, times(1)).findByUserTypeOrderByDeletedAtDesc(userType);
    }

    @Test
    @DisplayName("Devrait retourner une liste vide quand personne n'a supprimé d'utilisateurs")
    void getDeletedUsersByDeleter_ShouldReturnEmptyList_WhenDeleterHasNoDeletedUsers() {
        // Given
        String deletedBy = "new.admin@test.com";
        when(deletedUserRepository.findByDeletedByOrderByDeletedAtDesc(deletedBy))
                .thenReturn(Arrays.asList());

        // When
        List<DeletedUser> result = deletedUserService.getDeletedUsersByDeleter(deletedBy);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(deletedUserRepository, times(1)).findByDeletedByOrderByDeletedAtDesc(deletedBy);
    }

    @Test
    @DisplayName("Devrait gérer l'archivage d'un utilisateur avec des champs null")
    void archiveUser_ShouldHandleNullFields_WhenUserHasNullProperties() {
        // Given
        Client clientWithNulls = new Client();
        clientWithNulls.setUserId(UUID.randomUUID());
        clientWithNulls.setUsername("client.nulls@test.com");
        clientWithNulls.setFirstName(null); // Champ null
        clientWithNulls.setName(null);
        clientWithNulls.setAddress(null);

        String deletedBy = "admin@test.com";
        String reason = "Test with nulls";

        when(deletedUserRepository.save(any(DeletedUser.class))).thenReturn(testDeletedClient);

        // When
        deletedUserService.archiveUser(clientWithNulls, deletedBy, reason);

        // Then
        verify(deletedUserRepository, times(1)).save(argThat(deletedUser -> {
            assertEquals(clientWithNulls.getUserId(), deletedUser.getOriginalUserId());
            assertEquals(clientWithNulls.getUsername(), deletedUser.getUsername());
            assertNull(deletedUser.getFirstName());
            assertEquals("CLIENT", deletedUser.getUserType());
            assertNull(deletedUser.getClientName());
            assertNull(deletedUser.getAddress());
            assertEquals(deletedBy, deletedUser.getDeletedBy());
            assertEquals(reason, deletedUser.getDeletionReason());
            return true;
        }));
    }

    @Test
    @DisplayName("Devrait différencier les types d'utilisateurs lors de l'archivage")
    void archiveUser_ShouldDifferentiateUserTypes() {
        // Given
        when(deletedUserRepository.save(any(DeletedUser.class)))
                .thenReturn(testDeletedClient)
                .thenReturn(testDeletedAdmin);

        // When - Archiver un client
        deletedUserService.archiveUser(testClient, "admin1", "reason1");

        // When - Archiver un admin
        deletedUserService.archiveUser(testAdminUser, "admin2", "reason2");

        // Then
        verify(deletedUserRepository, times(2)).save(any(DeletedUser.class));

        // Vérifier que le premier appel était pour un CLIENT
        verify(deletedUserRepository).save(argThat(deletedUser ->
                "CLIENT".equals(deletedUser.getUserType())
        ));

        // Vérifier que le deuxième appel était pour un ADMIN
        verify(deletedUserRepository).save(argThat(deletedUser ->
                "ADMIN".equals(deletedUser.getUserType())
        ));
    }

    @Test
    @DisplayName("Devrait préserver tous les détails du client lors de l'archivage")
    void archiveUser_ShouldPreserveAllClientDetails() {
        // Given
        String deletedBy = "admin@test.com";
        String reason = "Complete test";

        // Client avec tous les champs remplis
        testClient.setContractStatus("ACTIVE");
        testClient.setCurrencyPreference("EUR");

        when(deletedUserRepository.save(any(DeletedUser.class))).thenReturn(testDeletedClient);

        // When
        deletedUserService.archiveUser(testClient, deletedBy, reason);

        // Then
        verify(deletedUserRepository, times(1)).save(argThat(deletedUser -> {
            // Vérifier tous les champs du client
            assertEquals(testClient.getUserId(), deletedUser.getOriginalUserId());
            assertEquals(testClient.getUsername(), deletedUser.getUsername());
            assertEquals(testClient.getFirstName(), deletedUser.getFirstName());
            assertEquals("CLIENT", deletedUser.getUserType());
            assertEquals(testClient.getName(), deletedUser.getClientName());
            assertEquals(testClient.getAddress(), deletedUser.getAddress());
            assertEquals(testClient.getContractStatus(), deletedUser.getContractStatus());
            assertEquals(testClient.getCurrencyPreference(), deletedUser.getCurrencyPreference());
            assertEquals(deletedBy, deletedUser.getDeletedBy());
            assertEquals(reason, deletedUser.getDeletionReason());
            assertNotNull(deletedUser.getDeletedAt());
            return true;
        }));
    }
}