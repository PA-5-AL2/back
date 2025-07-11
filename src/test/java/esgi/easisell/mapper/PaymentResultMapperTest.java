/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : PaymentResultMapperTest.java
 * @description : Tests unitaires pour le mapper PaymentResultMapper
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 11/07/2025
 * @package     : esgi.easisell.mapper
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.mapper;

import esgi.easisell.dto.PaymentResultDTO;
import esgi.easisell.entity.Sale;
import esgi.easisell.entity.Client;
import esgi.easisell.model.PaymentResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour PaymentResultMapper
 * Teste la méthode :
 * - toDTO() - Mapping du résultat de paiement avec calculs
 */
class PaymentResultMapperTest {

    private PaymentResult paymentResult;
    private Sale sale;

    /**
     * Forcer la locale US pour avoir des points comme séparateurs décimaux
     */
    @BeforeAll
    static void setUpLocale() {
        Locale.setDefault(Locale.US);
    }

    @BeforeEach
    void setUp() {
        // Créer le client
        Client client = new Client();
        client.setUserId(UUID.randomUUID());
        client.setUsername("teststore@easisell.com");
        client.setName("Test Store");

        // Créer la vente
        sale = new Sale();
        sale.setSaleId(UUID.randomUUID());
        sale.setClient(client);
        sale.setSaleTimestamp(Timestamp.valueOf(LocalDateTime.now()));
        sale.setTotalAmount(new BigDecimal("25.50"));

        // Créer le résultat de paiement réussi avec Builder
        paymentResult = PaymentResult.builder()
                .successful(true)
                .paymentId(UUID.randomUUID())
                .amountPaid(new BigDecimal("30.00"))
                .changeAmount(new BigDecimal("4.50"))
                .currency("EUR")
                .message("Paiement effectué avec succès")
                .errorMessage(null)
                .build();
    }

    // ==================== TESTS toDTO() - CAS RÉUSSIS ====================

    /**
     * Test toDTO() avec paiement réussi
     */
    @Test
    @DisplayName("✅ toDTO() - Paiement réussi avec monnaie rendue")
    void testToDTOSuccessfulPayment() {
        // When
        PaymentResultDTO result = PaymentResultMapper.toDTO(paymentResult, sale);

        // Then
        assertNotNull(result);
        assertEquals(true, result.getSuccessful());
        assertEquals(paymentResult.getPaymentId(), result.getPaymentId());
        assertEquals(sale.getSaleId(), result.getSaleId());
        assertEquals(paymentResult.getAmountPaid(), result.getAmountPaid());
        assertEquals(paymentResult.getChangeAmount(), result.getChangeAmount());
        assertEquals(paymentResult.getCurrency(), result.getCurrency());
        assertEquals(paymentResult.getMessage(), result.getMessage());
        assertNull(result.getErrorMessage());
    }

    /**
     * Test toDTO() avec paiement exact (pas de monnaie)
     */
    @Test
    @DisplayName("✅ toDTO() - Paiement exact sans monnaie")
    void testToDTOExactPayment() {
        // Given - Reconstruire avec Builder
        paymentResult = PaymentResult.builder()
                .successful(true)
                .paymentId(paymentResult.getPaymentId())
                .amountPaid(new BigDecimal("25.50"))
                .changeAmount(BigDecimal.ZERO)
                .currency("EUR")
                .message("Paiement exact effectué")
                .build();

        // When
        PaymentResultDTO result = PaymentResultMapper.toDTO(paymentResult, sale);

        // Then
        assertEquals(true, result.getSuccessful());
        assertEquals(new BigDecimal("25.50"), result.getAmountPaid());
        assertEquals(BigDecimal.ZERO, result.getChangeAmount());
        assertEquals("Paiement exact effectué", result.getMessage());
    }

    /**
     * Test toDTO() avec paiement par carte
     */
    @Test
    @DisplayName("✅ toDTO() - Paiement par carte bancaire")
    void testToDTOCardPayment() {
        // Given - Reconstruire avec Builder
        paymentResult = PaymentResult.builder()
                .successful(true)
                .paymentId(paymentResult.getPaymentId())
                .amountPaid(new BigDecimal("25.50"))
                .changeAmount(BigDecimal.ZERO)
                .currency("EUR")
                .message("Paiement par carte approuvé")
                .build();

        // When
        PaymentResultDTO result = PaymentResultMapper.toDTO(paymentResult, sale);

        // Then
        assertEquals(true, result.getSuccessful());
        assertEquals(new BigDecimal("25.50"), result.getAmountPaid());
        assertEquals(BigDecimal.ZERO, result.getChangeAmount());
        assertEquals("EUR", result.getCurrency());
        assertEquals("Paiement par carte approuvé", result.getMessage());
    }

    // ==================== TESTS toDTO() - CAS D'ERREUR ====================

    /**
     * Test toDTO() avec paiement échoué
     */
    @Test
    @DisplayName("❌ toDTO() - Paiement échoué")
    void testToDTOFailedPayment() {
        // Given - Créer un paiement échoué avec Builder
        paymentResult = PaymentResult.builder()
                .successful(false)
                .paymentId(null)
                .amountPaid(BigDecimal.ZERO)
                .changeAmount(BigDecimal.ZERO)
                .currency("EUR")
                .message(null)
                .errorMessage("Carte refusée - Fonds insuffisants")
                .build();

        // When
        PaymentResultDTO result = PaymentResultMapper.toDTO(paymentResult, sale);

        // Then
        assertEquals(false, result.getSuccessful());
        assertNull(result.getPaymentId());
        assertEquals(sale.getSaleId(), result.getSaleId());
        assertEquals(BigDecimal.ZERO, result.getAmountPaid());
        assertEquals(BigDecimal.ZERO, result.getChangeAmount());
        assertEquals("EUR", result.getCurrency());
        assertNull(result.getMessage());
        assertEquals("Carte refusée - Fonds insuffisants", result.getErrorMessage());
    }

    /**
     * Test toDTO() avec montant insuffisant
     */
    @Test
    @DisplayName("❌ toDTO() - Montant insuffisant")
    void testToDTOInsufficientAmount() {
        // Given - Créer avec Builder
        paymentResult = PaymentResult.builder()
                .successful(false)
                .paymentId(paymentResult.getPaymentId())
                .amountPaid(new BigDecimal("20.00"))
                .changeAmount(BigDecimal.ZERO)
                .currency("EUR")
                .errorMessage("Montant insuffisant - Il manque 5.50 €")
                .build();

        // When
        PaymentResultDTO result = PaymentResultMapper.toDTO(paymentResult, sale);

        // Then
        assertEquals(false, result.getSuccessful());
        assertEquals(new BigDecimal("20.00"), result.getAmountPaid());
        assertEquals(BigDecimal.ZERO, result.getChangeAmount());
        assertEquals("Montant insuffisant - Il manque 5.50 €", result.getErrorMessage());
    }

    /**
     * Test toDTO() avec erreur de connexion
     */
    @Test
    @DisplayName("❌ toDTO() - Erreur de connexion au terminal")
    void testToDTOConnectionError() {
        // Given - Créer avec Builder
        paymentResult = PaymentResult.builder()
                .successful(false)
                .paymentId(null)
                .amountPaid(BigDecimal.ZERO)
                .changeAmount(BigDecimal.ZERO)
                .currency("EUR")
                .errorMessage("Erreur de connexion au terminal de paiement")
                .build();

        // When
        PaymentResultDTO result = PaymentResultMapper.toDTO(paymentResult, sale);

        // Then
        assertEquals(false, result.getSuccessful());
        assertNull(result.getPaymentId());
        assertEquals("Erreur de connexion au terminal de paiement", result.getErrorMessage());
    }

    // ==================== TESTS EDGE CASES ====================

    /**
     * Test toDTO() avec monnaie importante
     */
    @Test
    @DisplayName("✅ toDTO() - Paiement avec beaucoup de monnaie")
    void testToDTOLargeChange() {
        // Given - Créer avec Builder
        paymentResult = PaymentResult.builder()
                .successful(true)
                .paymentId(paymentResult.getPaymentId())
                .amountPaid(new BigDecimal("100.00"))
                .changeAmount(new BigDecimal("74.50"))
                .currency("EUR")
                .message("Paiement de 100€ pour 25.50€")
                .build();

        // When
        PaymentResultDTO result = PaymentResultMapper.toDTO(paymentResult, sale);

        // Then
        assertEquals(new BigDecimal("100.00"), result.getAmountPaid());
        assertEquals(new BigDecimal("74.50"), result.getChangeAmount());
        assertEquals("Paiement de 100€ pour 25.50€", result.getMessage());
    }

    /**
     * Test toDTO() avec différentes devises
     */
    @Test
    @DisplayName("✅ toDTO() - Paiement en devise différente")
    void testToDTODifferentCurrency() {
        // Given - Créer avec Builder
        paymentResult = PaymentResult.builder()
                .successful(true)
                .paymentId(paymentResult.getPaymentId())
                .amountPaid(new BigDecimal("30.00"))
                .changeAmount(new BigDecimal("4.50"))
                .currency("USD")
                .message(paymentResult.getMessage())
                .build();

        // When
        PaymentResultDTO result = PaymentResultMapper.toDTO(paymentResult, sale);

        // Then
        assertEquals("USD", result.getCurrency());
        assertEquals(new BigDecimal("30.00"), result.getAmountPaid());
        assertEquals(new BigDecimal("4.50"), result.getChangeAmount());
    }

    /**
     * Test toDTO() avec montants décimaux complexes
     */
    @Test
    @DisplayName("✅ toDTO() - Montants avec décimales complexes")
    void testToDTOComplexDecimals() {
        // Given
        sale.setTotalAmount(new BigDecimal("12.567"));
        paymentResult.setAmountPaid(new BigDecimal("15.123"));
        paymentResult.setChangeAmount(new BigDecimal("2.556"));

        // When
        PaymentResultDTO result = PaymentResultMapper.toDTO(paymentResult, sale);

        // Then
        assertEquals(new BigDecimal("15.123"), result.getAmountPaid());
        assertEquals(new BigDecimal("2.556"), result.getChangeAmount());
        assertEquals(sale.getSaleId(), result.getSaleId());
    }

    /**
     * Test toDTO() avec messages longs
     */
    @Test
    @DisplayName("✅ toDTO() - Messages longs et caractères spéciaux")
    void testToDTOLongMessages() {
        // Given
        String longMessage = "Paiement effectué avec succès - Transaction n°12345 - Merci pour votre achat !";
        String longError = "Erreur technique : impossible de contacter le serveur d'autorisation bancaire";

        paymentResult.setMessage(longMessage);
        paymentResult.setErrorMessage(longError);

        // When
        PaymentResultDTO result = PaymentResultMapper.toDTO(paymentResult, sale);

        // Then
        assertEquals(longMessage, result.getMessage());
        assertEquals(longError, result.getErrorMessage());
    }

    /**
     * Test toDTO() avec valeurs nulles dans PaymentResult
     */
    @Test
    @DisplayName("✅ toDTO() - Gestion des valeurs nulles")
    void testToDTOWithNullValues() {
        // Given
        paymentResult.setPaymentId(null);
        paymentResult.setMessage(null);
        paymentResult.setErrorMessage(null);
        paymentResult.setCurrency(null);

        // When
        PaymentResultDTO result = PaymentResultMapper.toDTO(paymentResult, sale);

        // Then
        assertNull(result.getPaymentId());
        assertNull(result.getMessage());
        assertNull(result.getErrorMessage());
        assertNull(result.getCurrency());
        // Les autres champs doivent être présents
        assertEquals(sale.getSaleId(), result.getSaleId());
        assertEquals(paymentResult.isSuccessful(), result.getSuccessful());
    }

    /**
     * Test toDTO() avec paiement partiel
     */
    @Test
    @DisplayName("✅ toDTO() - Paiement partiel accepté")
    void testToDTOPartialPayment() {
        // Given
        paymentResult.setSuccessful(true);
        paymentResult.setAmountPaid(new BigDecimal("15.00"));
        paymentResult.setChangeAmount(BigDecimal.ZERO);
        paymentResult.setMessage("Paiement partiel accepté - Reste à payer: 10.50€");

        // When
        PaymentResultDTO result = PaymentResultMapper.toDTO(paymentResult, sale);

        // Then
        assertEquals(true, result.getSuccessful());
        assertEquals(new BigDecimal("15.00"), result.getAmountPaid());
        assertEquals(BigDecimal.ZERO, result.getChangeAmount());
        assertEquals("Paiement partiel accepté - Reste à payer: 10.50€", result.getMessage());
    }
}