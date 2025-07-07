/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : SupplierTest.java
 * @description : Tests unitaires pour l'entité Supplier
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

import java.util.ArrayList;
import java.util.UUID;

/**
 * The type Supplier test.
 */
class SupplierTest {

    private Supplier supplier;
    private Client client;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        supplier = new Supplier();

        client = new Client();
        client.setUserId(UUID.randomUUID());
        client.setName("Test Store");
    }

    /**
     * Test supplier getters setters.
     */
    @Test
    @DisplayName("✅ Getters/Setters fournisseur")
    void testSupplierGettersSetters() {
        UUID supplierId = UUID.randomUUID();
        String name = "Fournisseur Test";
        String firstName = "Jean";
        String description = "Fournisseur de boissons";
        String contactInfo = "contact@fournisseur.com";
        String phoneNumber = "0123456789";

        supplier.setSupplierId(supplierId);
        supplier.setName(name);
        supplier.setFirstName(firstName);
        supplier.setDescription(description);
        supplier.setContactInfo(contactInfo);
        supplier.setPhoneNumber(phoneNumber);
        supplier.setClient(client);

        assertEquals(supplierId, supplier.getSupplierId());
        assertEquals(name, supplier.getName());
        assertEquals(firstName, supplier.getFirstName());
        assertEquals(description, supplier.getDescription());
        assertEquals(contactInfo, supplier.getContactInfo());
        assertEquals(phoneNumber, supplier.getPhoneNumber());
        assertEquals(client, supplier.getClient());
    }

    /**
     * Test client relation.
     */
    @Test
    @DisplayName("✅ Relation avec Client")
    void testClientRelation() {
        supplier.setClient(client);

        assertEquals(client, supplier.getClient());
        assertNotNull(supplier.getClient());
    }

    /**
     * Test stock items management.
     */
    @Test
    @DisplayName("✅ Gestion des stock items")
    void testStockItemsManagement() {
        supplier.setStockItems(new ArrayList<>());

        assertNotNull(supplier.getStockItems());
        assertTrue(supplier.getStockItems().isEmpty());

        // Ajouter un stock item
        StockItem stockItem = new StockItem();
        stockItem.setStockItemId(UUID.randomUUID());
        stockItem.setQuantity(100);
        supplier.getStockItems().add(stockItem);

        assertEquals(1, supplier.getStockItems().size());
        assertTrue(supplier.getStockItems().contains(stockItem));
    }

    /**
     * Test name required.
     */
    @Test
    @DisplayName("✅ Validation métier - nom obligatoire")
    void testNameRequired() {
        supplier.setName("Fournisseur Obligatoire");

        assertNotNull(supplier.getName());
        assertFalse(supplier.getName().isEmpty());
    }
}
