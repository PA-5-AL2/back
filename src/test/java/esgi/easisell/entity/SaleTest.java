/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : SaleTest.java
 * @description : Tests unitaires pour l'entité Sale
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
import java.util.ArrayList;
import java.util.UUID;

/**
 * The type Sale test.
 */
class SaleTest {

    private Sale sale;
    private Client client;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        sale = new Sale();

        client = new Client();
        client.setUserId(UUID.randomUUID());
        client.setCurrencyPreference("EUR");
    }

    /**
     * Test custom constructor.
     */
    @Test
    @DisplayName("✅ Constructeur personnalisé")
    void testCustomConstructor() {
        // Arrange
        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
        BigDecimal totalAmount = new BigDecimal("25.50");
        Boolean isDeferred = false;

        // Act
        Sale newSale = new Sale(client, timestamp, totalAmount, isDeferred);

        // Assert
        assertEquals(client, newSale.getClient());
        assertEquals(timestamp, newSale.getSaleTimestamp());
        assertEquals(totalAmount, newSale.getTotalAmount());
        assertEquals(isDeferred, newSale.getIsDeferred());
        assertNotNull(newSale.getSaleItems());
        assertNotNull(newSale.getPayments());
    }

    /**
     * Test sale getters setters.
     */
    @Test
    @DisplayName("✅ Getters/Setters vente")
    void testSaleGettersSetters() {
        // Arrange
        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
        BigDecimal totalAmount = new BigDecimal("50.00");
        Boolean isDeferred = true;

        // Act
        sale.setSaleTimestamp(timestamp);
        sale.setTotalAmount(totalAmount);
        sale.setIsDeferred(isDeferred);
        sale.setClient(client);

        // Assert
        assertEquals(timestamp, sale.getSaleTimestamp());
        assertEquals(totalAmount, sale.getTotalAmount());
        assertEquals(isDeferred, sale.getIsDeferred());
        assertEquals(client, sale.getClient());
    }

    /**
     * Test sale relations.
     */
    @Test
    @DisplayName("✅ Relations avec items et paiements")
    void testSaleRelations() {
        // Act
        sale.setSaleItems(new ArrayList<>());
        sale.setPayments(new ArrayList<>());

        // Assert
        assertNotNull(sale.getSaleItems());
        assertNotNull(sale.getPayments());
        assertTrue(sale.getSaleItems().isEmpty());
        assertTrue(sale.getPayments().isEmpty());
    }

    /**
     * Test amount validation.
     */
    @Test
    @DisplayName("✅ Validation métier - montant positif")
    void testAmountValidation() {
        // Arrange
        BigDecimal positiveAmount = new BigDecimal("100.00");

        // Act
        sale.setTotalAmount(positiveAmount);

        // Assert
        assertTrue(sale.getTotalAmount().compareTo(BigDecimal.ZERO) > 0);
    }
}