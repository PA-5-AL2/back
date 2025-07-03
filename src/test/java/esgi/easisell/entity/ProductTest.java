/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : ProductTest.java
 * @description : Tests unitaires pour l'entité Product
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

/**
 * The type Product test.
 */
class ProductTest {

    private Product product;
    private Client client;
    private Category category;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        product = new Product();

        // Créer objets liés pour les tests de relations
        client = new Client();
        client.setUserId(UUID.randomUUID());

        category = new Category();
        category.setCategoryId(UUID.randomUUID());
        category.setName("Boissons");
    }

    /**
     * Test product getters setters.
     */
    @Test
    @DisplayName("✅ Getters/Setters produit")
    void testProductGettersSetters() {
        // Arrange
        String name = "Coca-Cola 1.5L";
        String description = "Boisson gazeuse";
        String barcode = "1234567890123";
        String brand = "Coca-Cola";
        BigDecimal unitPrice = new BigDecimal("2.50");

        // Act
        product.setName(name);
        product.setDescription(description);
        product.setBarcode(barcode);
        product.setBrand(brand);
        product.setUnitPrice(unitPrice);

        // Assert
        assertEquals(name, product.getName());
        assertEquals(description, product.getDescription());
        assertEquals(barcode, product.getBarcode());
        assertEquals(brand, product.getBrand());
        assertEquals(unitPrice, product.getUnitPrice());
    }

    /**
     * Test product relations.
     */
    @Test
    @DisplayName("✅ Relations avec Client et Category")
    void testProductRelations() {
        // Act
        product.setClient(client);
        product.setCategory(category);

        // Assert
        assertEquals(client, product.getClient());
        assertEquals(category, product.getCategory());
    }

    /**
     * Test price validation.
     */
    @Test
    @DisplayName("✅ Validation métier - prix positif")
    void testPriceValidation() {
        // Arrange
        BigDecimal positivePrice = new BigDecimal("15.99");

        // Act
        product.setUnitPrice(positivePrice);

        // Assert
        assertTrue(product.getUnitPrice().compareTo(BigDecimal.ZERO) > 0);
    }

    /**
     * Test collections management.
     */
    @Test
    @DisplayName("✅ Gestion des collections")
    void testCollectionsManagement() {
        // Act
        product.setStockItems(new ArrayList<>());
        product.setPromotions(new ArrayList<>());

        // Assert
        assertNotNull(product.getStockItems());
        assertNotNull(product.getPromotions());
        assertTrue(product.getStockItems().isEmpty());
    }
}
