package esgi.easisell.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

/**
 * The type Client test.
 */
class ClientTest {

    private Client client;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        client = new Client();
    }

    /**
     * Test client getters setters.
     */
    @Test
    @DisplayName("✅ Getters/Setters spécifiques à Client")
    void testClientGettersSetters() {
        // Arrange
        String name = "Ma Supérette";
        String address = "123 Rue du Commerce";
        String contractStatus = "ACTIVE";
        String currencyPreference = "EUR";

        // Act
        client.setName(name);
        client.setAddress(address);
        client.setContractStatus(contractStatus);
        client.setCurrencyPreference(currencyPreference);

        // Assert
        assertEquals(name, client.getName());
        assertEquals(address, client.getAddress());
        assertEquals(contractStatus, client.getContractStatus());
        assertEquals(currencyPreference, client.getCurrencyPreference());
    }

    /**
     * Test relations management.
     */
    @Test
    @DisplayName("✅ Relations - gestion des listes")
    void testRelationsManagement() {
        // Act
        client.setSuppliers(new ArrayList<>());
        client.setProducts(new ArrayList<>());
        client.setSales(new ArrayList<>());

        // Assert
        assertNotNull(client.getSuppliers());
        assertNotNull(client.getProducts());
        assertNotNull(client.getSales());
        assertTrue(client.getSuppliers().isEmpty());
    }

    /**
     * Test contract status validation.
     */
    @Test
    @DisplayName("✅ Validation métier - statut de contrat")
    void testContractStatusValidation() {
        // Arrange
        String[] validStatuses = {"ACTIVE", "SUSPENDED", "CANCELLED"};

        // Act & Assert
        for (String status : validStatuses) {
            client.setContractStatus(status);
            assertEquals(status, client.getContractStatus());
        }
    }

    /**
     * Test user inheritance.
     */
    @Test
    @DisplayName("✅ Héritage de User fonctionne")
    void testUserInheritance() {
        // Act
        client.setUsername("client@test.com");
        client.setRole("CLIENT");

        // Assert
        assertInstanceOf(User.class, client);
        assertEquals("client@test.com", client.getUsername());
        assertEquals("CLIENT", client.getRole());
    }
}