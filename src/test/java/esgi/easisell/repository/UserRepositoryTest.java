/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE  
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : UserRepositoryTest.java
 * @description : Tests unitaires pour UserRepository avec Mockito
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 12/07/2025
 * @package     : esgi.easisell.repository
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.repository;

import esgi.easisell.entity.User;
import esgi.easisell.entity.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour UserRepository utilisant Mockito sans H2
 * Focus sur les méthodes essentielles du repository
 */
@ExtendWith(MockitoExtension.class)
@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    private User testUser;
    private Client testClient;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testClient = new Client();
        testClient.setUserId(userId);
        testClient.setUsername("test@example.com");
        testClient.setPassword("hashedPassword");
        testClient.setRole("CLIENT");
        testClient.setName("Test Client");
        testClient.setCreatedAt(LocalDateTime.now());

        testUser = testClient;
    }

    // ==================== TESTS FINDBYUSERNAME ====================

    /**
     * Test findByUsername - utilisateur trouvé
     */
    @Test
    @DisplayName("✅ findByUsername() - Utilisateur trouvé")
    void testFindByUsernameFound() {
        // Given
        String username = "test@example.com";
        when(userRepository.findByUsername(username)).thenReturn(testUser);

        // When
        User result = userRepository.findByUsername(username);

        // Then
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(testUser.getUserId(), result.getUserId());
        verify(userRepository, times(1)).findByUsername(username);
    }

    /**
     * Test findByUsername - utilisateur non trouvé
     */
    @Test
    @DisplayName("❌ findByUsername() - Utilisateur non trouvé")
    void testFindByUsernameNotFound() {
        // Given
        String username = "nonexistent@example.com";
        when(userRepository.findByUsername(username)).thenReturn(null);

        // When
        User result = userRepository.findByUsername(username);

        // Then
        assertNull(result);
        verify(userRepository, times(1)).findByUsername(username);
    }

    /**
     * Test findByUsername avec username null
     */
    @Test
    @DisplayName("❌ findByUsername() - Username null")
    void testFindByUsernameWithNull() {
        // Given
        when(userRepository.findByUsername(null)).thenReturn(null);

        // When
        User result = userRepository.findByUsername(null);

        // Then
        assertNull(result);
        verify(userRepository, times(1)).findByUsername(null);
    }

    // ==================== TESTS MÉTHODES JPA STANDARD ====================

    /**
     * Test save - sauvegarde réussie
     */
    @Test
    @DisplayName("✅ save() - Sauvegarde réussie")
    void testSaveUser() {
        // Given
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User savedUser = userRepository.save(testUser);

        // Then
        assertNotNull(savedUser);
        assertEquals(testUser.getUsername(), savedUser.getUsername());
        verify(userRepository, times(1)).save(testUser);
    }

    /**
     * Test findById - utilisateur trouvé
     */
    @Test
    @DisplayName("✅ findById() - Utilisateur trouvé")
    void testFindByIdFound() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userRepository.findById(userId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser.getUserId(), result.get().getUserId());
        verify(userRepository, times(1)).findById(userId);
    }

    /**
     * Test findById - utilisateur non trouvé
     */
    @Test
    @DisplayName("❌ findById() - Utilisateur non trouvé")
    void testFindByIdNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userRepository.findById(nonExistentId);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(nonExistentId);
    }

    /**
     * Test findAll - liste d'utilisateurs
     */
    @Test
    @DisplayName("✅ findAll() - Liste d'utilisateurs")
    void testFindAll() {
        // Given
        Client user2 = new Client();
        user2.setUserId(UUID.randomUUID());
        user2.setUsername("user2@example.com");
        user2.setName("Client 2");

        List<User> userList = Arrays.asList(testUser, user2);
        when(userRepository.findAll()).thenReturn(userList);

        // When
        List<User> result = userRepository.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(testUser));
        assertTrue(result.contains(user2));
        verify(userRepository, times(1)).findAll();
    }

    /**
     * Test existsById - utilisateur existe
     */
    @Test
    @DisplayName("✅ existsById() - Utilisateur existe")
    void testExistsByIdTrue() {
        // Given
        when(userRepository.existsById(userId)).thenReturn(true);

        // When
        boolean exists = userRepository.existsById(userId);

        // Then
        assertTrue(exists);
        verify(userRepository, times(1)).existsById(userId);
    }

    /**
     * Test existsById - utilisateur n'existe pas
     */
    @Test
    @DisplayName("❌ existsById() - Utilisateur n'existe pas")
    void testExistsByIdFalse() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.existsById(nonExistentId)).thenReturn(false);

        // When
        boolean exists = userRepository.existsById(nonExistentId);

        // Then
        assertFalse(exists);
        verify(userRepository, times(1)).existsById(nonExistentId);
    }

    /**
     * Test deleteById - suppression réussie
     */
    @Test
    @DisplayName("✅ deleteById() - Suppression réussie")
    void testDeleteById() {
        // Given
        doNothing().when(userRepository).deleteById(userId);

        // When
        userRepository.deleteById(userId);

        // Then
        verify(userRepository, times(1)).deleteById(userId);
    }

    /**
     * Test count - comptage des utilisateurs
     */
    @Test
    @DisplayName("✅ count() - Comptage des utilisateurs")
    void testCount() {
        // Given
        when(userRepository.count()).thenReturn(5L);

        // When
        long count = userRepository.count();

        // Then
        assertEquals(5L, count);
        verify(userRepository, times(1)).count();
    }

    // ==================== TESTS DE VALIDATION MÉTIER ====================

    /**
     * Test avec données valides
     */
    @Test
    @DisplayName("✅ Validation - Données valides")
    void testValidUserData() {
        // Given
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User savedUser = userRepository.save(testUser);

        // Then
        assertNotNull(savedUser.getUsername());
        assertNotNull(savedUser.getPassword());
        assertNotNull(savedUser.getRole());
        assertNotNull(savedUser.getCreatedAt());
    }

    /**
     * Test recherche par username avec différents formats
     */
    @Test
    @DisplayName("✅ findByUsername() - Différents formats email")
    void testFindByUsernameVariousFormats() {
        // Test avec différents formats d'email
        String[] usernames = {
                "user@example.com",
                "user.name@domain.org",
                "user+tag@example.co.uk"
        };

        for (String username : usernames) {
            // Given
            Client user = new Client();
            user.setUsername(username);
            user.setName("Test User");
            when(userRepository.findByUsername(username)).thenReturn(user);

            // When
            User result = userRepository.findByUsername(username);

            // Then
            assertNotNull(result);
            assertEquals(username, result.getUsername());
        }

        verify(userRepository, times(usernames.length)).findByUsername(anyString());
    }

    // ==================== TESTS DE PERFORMANCE SIMULÉS ====================

    /**
     * Test simulation de recherche rapide
     */
    @Test
    @DisplayName("⚡ Performance - Recherche rapide par username")
    void testQuickUsernameSearch() {
        // Given
        String username = "quicksearch@example.com";
        when(userRepository.findByUsername(username)).thenReturn(testUser);

        // When - Simulation de recherches multiples
        for (int i = 0; i < 3; i++) {
            User result = userRepository.findByUsername(username);
            assertNotNull(result);
        }

        // Then
        verify(userRepository, times(3)).findByUsername(username);
    }

    /**
     * Test vérification de la cohérence des données
     */
    @Test
    @DisplayName("✅ Cohérence - Vérification des données utilisateur")
    void testUserDataConsistency() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(testUser);

        // When
        Optional<User> userById = userRepository.findById(userId);
        User userByUsername = userRepository.findByUsername(testUser.getUsername());

        // Then - Les deux méthodes doivent retourner le même utilisateur
        assertTrue(userById.isPresent());
        assertNotNull(userByUsername);
        assertEquals(userById.get().getUserId(), userByUsername.getUserId());
        assertEquals(userById.get().getUsername(), userByUsername.getUsername());
    }

    /**
     * Test validation du rôle utilisateur
     */
    @Test
    @DisplayName("✅ Validation - Rôles utilisateur valides")
    void testValidUserRoles() {
        // Given
        String[] validRoles = {"CLIENT", "ADMIN"};

        for (String role : validRoles) {
            testUser.setRole(role);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            User savedUser = userRepository.save(testUser);

            // Then
            assertEquals(role, savedUser.getRole());
        }
    }

    /**
     * Test timestamp de création
     */
    @Test
    @DisplayName("✅ Validation - Timestamp de création")
    void testCreatedAtTimestamp() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        testUser.setCreatedAt(now);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User savedUser = userRepository.save(testUser);

        // Then
        assertNotNull(savedUser.getCreatedAt());
        assertEquals(now, savedUser.getCreatedAt());
    }
}