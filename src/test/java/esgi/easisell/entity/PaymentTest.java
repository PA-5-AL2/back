/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : PaymentTest.java
 * @description : Tests unitaires pour l'entité Payment
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
 * The type Payment test.
 */
class PaymentTest {

    private Payment payment;
    private Sale sale;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        payment = new Payment();

        Client client = new Client();
        client.setUserId(UUID.randomUUID());
        client.setName("Test Store");

        sale = new Sale();
        sale.setSaleId(UUID.randomUUID());
        sale.setClient(client);
        sale.setTotalAmount(new BigDecimal("25.00"));
    }

    /**
     * Test builder constructor.
     */
    @Test
    @DisplayName("✅ Constructeur Builder")
    void testBuilderConstructor() {
        UUID paymentId = UUID.randomUUID();
        String type = "CASH";
        BigDecimal amount = new BigDecimal("25.00");
        String currency = "EUR";
        Timestamp paymentDate = Timestamp.valueOf(LocalDateTime.now());

        Payment newPayment = Payment.builder()
                .paymentId(paymentId)
                .type(type)
                .amount(amount)
                .currency(currency)
                .paymentDate(paymentDate)
                .sale(sale)
                .build();

        assertEquals(paymentId, newPayment.getPaymentId());
        assertEquals(type, newPayment.getType());
        assertEquals(amount, newPayment.getAmount());
        assertEquals(currency, newPayment.getCurrency());
        assertEquals(paymentDate, newPayment.getPaymentDate());
        assertEquals(sale, newPayment.getSale());
    }

    /**
     * Test payment getters setters.
     */
    @Test
    @DisplayName("✅ Getters/Setters payment")
    void testPaymentGettersSetters() {
        UUID paymentId = UUID.randomUUID();
        String type = "CARD";
        BigDecimal amount = new BigDecimal("50.00");
        String currency = "USD";
        Timestamp paymentDate = Timestamp.valueOf(LocalDateTime.now());

        payment.setPaymentId(paymentId);
        payment.setType(type);
        payment.setAmount(amount);
        payment.setCurrency(currency);
        payment.setPaymentDate(paymentDate);
        payment.setSale(sale);

        assertEquals(paymentId, payment.getPaymentId());
        assertEquals(type, payment.getType());
        assertEquals(amount, payment.getAmount());
        assertEquals(currency, payment.getCurrency());
        assertEquals(paymentDate, payment.getPaymentDate());
        assertEquals(sale, payment.getSale());
    }

    /**
     * Test sale relation.
     */
    @Test
    @DisplayName("✅ Relation avec Sale")
    void testSaleRelation() {
        payment.setSale(sale);

        assertEquals(sale, payment.getSale());
        assertNotNull(payment.getSale());
    }

    /**
     * Test valid payment types.
     */
    @Test
    @DisplayName("✅ Types de paiement valides")
    void testValidPaymentTypes() {
        String[] validTypes = {"CASH", "CARD", "CHECK"};

        for (String type : validTypes) {
            payment.setType(type);
            assertEquals(type, payment.getType());
        }
    }

    /**
     * Test valid currencies.
     */
    @Test
    @DisplayName("✅ Devises valides")
    void testValidCurrencies() {
        String[] validCurrencies = {"EUR", "USD", "CAD"};

        for (String currency : validCurrencies) {
            payment.setCurrency(currency);
            assertEquals(currency, payment.getCurrency());
        }
    }

    /**
     * Test amount validation.
     */
    @Test
    @DisplayName("✅ Validation métier - montant positif")
    void testAmountValidation() {
        BigDecimal positiveAmount = new BigDecimal("100.00");
        payment.setAmount(positiveAmount);

        assertTrue(payment.getAmount().compareTo(BigDecimal.ZERO) > 0);
    }

    /**
     * Test required fields.
     */
    @Test
    @DisplayName("✅ Champs obligatoires")
    void testRequiredFields() {
        payment.setType("CASH");
        payment.setAmount(new BigDecimal("25.00"));
        payment.setCurrency("EUR");
        payment.setPaymentDate(Timestamp.valueOf(LocalDateTime.now()));
        payment.setSale(sale);

        assertNotNull(payment.getType());
        assertNotNull(payment.getAmount());
        assertNotNull(payment.getCurrency());
        assertNotNull(payment.getPaymentDate());
        assertNotNull(payment.getSale());
    }
}