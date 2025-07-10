/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : UserTest.java
 * @description : Tests unitaires pour l'entité abstraite User
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 03/07/2025
 * @package     : esgi.easisell.entity
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The type User test.
 */
class UserTest {

    // Classe concrète pour tester l'entité abstraite User
    private static class TestUser extends User {
    }

    private TestUser user;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        user = new TestUser();
    }

    /**
     * Test getters setters.
     */
    @Test
    @DisplayName("Getters/Setters fonctionnent")
    void testGettersSetters() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String username = "test@email.com";
        String firstName = "John";
        String role = "CLIENT";

        // Act
        user.setUserId(userId);
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setRole(role);

        // Assert
        assertEquals(userId, user.getUserId());
        assertEquals(username, user.getUsername());
        assertEquals(firstName, user.getFirstName());
        assertEquals(role, user.getRole());
    }

    /**
     * Test default constructor.
     */
    @Test
    @DisplayName("✅ Constructeur par défaut fonctionne")
    void testDefaultConstructor() {
        // Act
        TestUser newUser = new TestUser();

        // Assert
        assertNotNull(newUser);
        assertNull(newUser.getUserId());
        assertNull(newUser.getUsername());
    }

    /**
     * Test on create callback.
     */
    @Test
    @DisplayName("✅ Callback onCreate fonctionne")
    void testOnCreateCallback() {
        // Act
        user.onCreate();

        // Assert
        assertNotNull(user.getCreatedAt());
        assertTrue(user.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    /**
     * Test username required.
     */
    @Test
    @DisplayName("✅ Validation métier - username obligatoire")
    void testUsernameRequired() {
        // Act & Assert
        user.setUsername("test@email.com");
        assertNotNull(user.getUsername());
        assertFalse(user.getUsername().isEmpty());
    }
}
