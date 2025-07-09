/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : SaleItemTest.java
 * @description : Tests unitaires pour l'entité SaleItem
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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The type Sale item test.
 */
class SaleItemTest {

    private SaleItem saleItem;
    private Sale sale;
    private Product product;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        saleItem = new SaleItem();

        Client client = new Client();
        client.setUserId(UUID.randomUUID());
        client.setName("Test Store");

        product = new Product();
        product.setProductId(UUID.randomUUID());
        product.setName("Test Product");
        product.setUnitPrice(new BigDecimal("2.50"));

        sale = new Sale();
        sale.setSaleId(UUID.randomUUID());
        sale.setClient(client);
        sale.setSaleTimestamp(Timestamp.valueOf(LocalDateTime.now()));
    }

    /**
     * Test builder constructor.
     */
    @Test
    @DisplayName("✅ Constructeur Builder")
    void testBuilderConstructor() {
        UUID saleItemId = UUID.randomUUID();
        BigDecimal quantitySold = BigDecimal.valueOf(3);
        BigDecimal priceAtSale = new BigDecimal("7.50");

        SaleItem newSaleItem = SaleItem.builder()
                .saleItemId(saleItemId)
                .sale(sale)
                .product(product)
                .quantitySold(quantitySold)
                .priceAtSale(priceAtSale)
                .build();

        assertEquals(saleItemId, newSaleItem.getSaleItemId());
        assertEquals(sale, newSaleItem.getSale());
        assertEquals(product, newSaleItem.getProduct());
        assertEquals(quantitySold, newSaleItem.getQuantitySold());
        assertEquals(priceAtSale, newSaleItem.getPriceAtSale());
    }

    /**
     * Test sale item getters setters.
     */
    @Test
    @DisplayName("✅ Getters/Setters sale item")
    void testSaleItemGettersSetters() {
        UUID saleItemId = UUID.randomUUID();
        int quantitySold = 5;
        BigDecimal priceAtSale = new BigDecimal("12.50");

        saleItem.setSaleItemId(saleItemId);
        saleItem.setSale(sale);
        saleItem.setProduct(product);
        saleItem.setQuantitySold(BigDecimal.valueOf(quantitySold));
        saleItem.setPriceAtSale(priceAtSale);

        assertEquals(saleItemId, saleItem.getSaleItemId());
        assertEquals(sale, saleItem.getSale());
        assertEquals(product, saleItem.getProduct());
        assertEquals(quantitySold, saleItem.getQuantitySold());
        assertEquals(priceAtSale, saleItem.getPriceAtSale());
    }

    /**
     * Test required relations.
     */
    @Test
    @DisplayName("✅ Relations obligatoires")
    void testRequiredRelations() {
        saleItem.setSale(sale);
        saleItem.setProduct(product);

        assertNotNull(saleItem.getSale());
        assertNotNull(saleItem.getProduct());
    }

    /**
     * Test quantity validation.
     */
    @Test
    @DisplayName("✅ Validation métier - quantité positive")
    void testQuantityValidation() {
        saleItem.setQuantitySold(BigDecimal.valueOf(10));

        assertTrue(saleItem.getQuantitySold().intValue() > 0);
    }

    /**
     * Test price validation.
     */
    @Test
    @DisplayName("✅ Validation métier - prix positif")
    void testPriceValidation() {
        BigDecimal positivePrice = new BigDecimal("15.99");
        saleItem.setPriceAtSale(positivePrice);

        assertTrue(saleItem.getPriceAtSale().compareTo(BigDecimal.ZERO) >= 0);
    }
}