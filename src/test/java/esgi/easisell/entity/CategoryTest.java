/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : CategoryTest.java
 * @description : Tests unitaires pour l'entité Category
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

import java.util.UUID;

/**
 * The type Category test.
 */
class CategoryTest {

    private Category category;
    private Client client;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        category = new Category();

        client = new Client();
        client.setUserId(UUID.randomUUID());
        client.setName("Test Store");
    }

    /**
     * Test category getters setters.
     */
    @Test
    @DisplayName("✅ Getters/Setters catégorie")
    void testCategoryGettersSetters() {
        String name = "Boissons";
        UUID categoryId = UUID.randomUUID();

        category.setCategoryId(categoryId);
        category.setName(name);
        category.setClient(client);

        assertEquals(categoryId, category.getCategoryId());
        assertEquals(name, category.getName());
        assertEquals(client, category.getClient());
    }

    /**
     * Test client relation.
     */
    @Test
    @DisplayName("✅ Relation avec Client")
    void testClientRelation() {
        category.setClient(client);

        assertEquals(client, category.getClient());
        assertNotNull(category.getClient());
    }

    /**
     * Test products management.
     */
    @Test
    @DisplayName("✅ Gestion des produits")
    void testProductsManagement() {
        // Test initialisation par défaut
        assertNotNull(category.getProducts());
        assertTrue(category.getProducts().isEmpty());

        // Ajouter un produit
        Product product = new Product();
        product.setProductId(UUID.randomUUID());
        product.setName("Test Product");
        category.getProducts().add(product);

        assertEquals(1, category.getProducts().size());
        assertTrue(category.getProducts().contains(product));
    }

    /**
     * Test name required.
     */
    @Test
    @DisplayName("✅ Validation métier - nom obligatoire")
    void testNameRequired() {
        category.setName("Alimentation");

        assertNotNull(category.getName());
        assertFalse(category.getName().isEmpty());
    }
}