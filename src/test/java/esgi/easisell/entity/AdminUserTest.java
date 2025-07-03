package esgi.easisell.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.UUID;

/**
 * The type Admin user test.
 */
class AdminUserTest {

    private AdminUser adminUser;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        adminUser = new AdminUser();
    }

    /**
     * Test default constructor.
     */
    @Test
    @DisplayName("✅ Constructeur par défaut")
    void testDefaultConstructor() {
        AdminUser newAdmin = new AdminUser();
        assertNotNull(newAdmin);
        assertNull(newAdmin.getManagedClients());
    }

    /**
     * Test user inheritance.
     */
    @Test
    @DisplayName("✅ Héritage de User fonctionne")
    void testUserInheritance() {
        adminUser.setUsername("admin@easisell.com");
        adminUser.setRole("ADMIN");

        assertInstanceOf(User.class, adminUser);
        assertEquals("admin@easisell.com", adminUser.getUsername());
        assertEquals("ADMIN", adminUser.getRole());
    }

    /**
     * Test managed clients.
     */
    @Test
    @DisplayName("✅ Gestion des clients managés")
    void testManagedClients() {
        adminUser.setManagedClients(new ArrayList<>());

        assertNotNull(adminUser.getManagedClients());
        assertTrue(adminUser.getManagedClients().isEmpty());

        // Ajouter un client
        Client client = new Client();
        client.setUserId(UUID.randomUUID());
        adminUser.getManagedClients().add(client);

        assertEquals(1, adminUser.getManagedClients().size());
        assertTrue(adminUser.getManagedClients().contains(client));
    }
}
