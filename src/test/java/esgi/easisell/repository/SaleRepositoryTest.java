/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : SaleRepositoryTest.java
 * @description : Tests d'intégration pour SaleRepository
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 11/07/2025
 * @package     : esgi.easisell.repository
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.repository;

import esgi.easisell.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour SaleRepository
 * Teste les requêtes personnalisées importantes
 */
@DataJpaTest
class SaleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SaleRepository saleRepository;

    private Client testClient;

    /**
     * Configuration initiale pour chaque test
     */
    @BeforeEach
    void setUp() {
        testClient = new Client();
        testClient.setUserId(UUID.randomUUID());
        testClient.setUsername("test@example.com");
        testClient.setPassword("password123");
        testClient.setName("Test Store");
        testClient.setFirstName("Test");
        testClient.setAddress("123 Test Street");
        testClient.setContractStatus("ACTIVE");
        testClient.setCurrencyPreference("EUR");
        testClient.setAccessCode("TEST123");
        testClient = entityManager.persistAndFlush(testClient);
    }

    /**
     * Test findTodaySalesByClient() - Ventes du jour
     */
    @Test
    @DisplayName("✅ Ventes du jour - findTodaySalesByClient")
    void testFindTodaySalesByClient() {
        // Given
        Sale todaySale = createSale(BigDecimal.valueOf(100.0), false);
        Sale yesterdaySale = createSale(BigDecimal.valueOf(50.0), false);
        yesterdaySale.setSaleTimestamp(Timestamp.valueOf(LocalDateTime.now().minusDays(1)));

        entityManager.persistAndFlush(todaySale);
        entityManager.persistAndFlush(yesterdaySale);

        // When
        List<Sale> todaySales = saleRepository.findTodaySalesByClient(testClient.getUserId());

        // Then
        assertEquals(1, todaySales.size());
        assertEquals(todaySale.getSaleId(), todaySales.get(0).getSaleId());
    }

    /**
     * Test getTodayTotalSales() - Total du jour
     */
    @Test
    @DisplayName("✅ Total des ventes du jour - getTodayTotalSales")
    void testGetTodayTotalSales() {
        // Given
        Sale sale1 = createSale(BigDecimal.valueOf(100.0), false);
        Sale sale2 = createSale(BigDecimal.valueOf(75.50), false);
        Sale sale3 = createSale(BigDecimal.valueOf(25.0), false);

        entityManager.persistAndFlush(sale1);
        entityManager.persistAndFlush(sale2);
        entityManager.persistAndFlush(sale3);

        // When
        BigDecimal totalSales = saleRepository.getTodayTotalSales(testClient.getUserId());

        // Then
        assertEquals(BigDecimal.valueOf(200.50), totalSales);
    }

    /**
     * Test findPendingPaymentSales() - Ventes en attente
     */
    @Test
    @DisplayName("✅ Ventes en attente - findPendingPaymentSales")
    void testFindPendingPaymentSales() {
        // Given
        Sale pendingSale = createSale(BigDecimal.valueOf(100.0), true);
        Sale normalSale = createSale(BigDecimal.valueOf(50.0), false);

        entityManager.persistAndFlush(pendingSale);
        entityManager.persistAndFlush(normalSale);

        // When
        List<Sale> pendingSales = saleRepository.findPendingPaymentSales(testClient.getUserId());

        // Then
        assertEquals(1, pendingSales.size());
        assertEquals(pendingSale.getSaleId(), pendingSales.get(0).getSaleId());
    }

    /**
     * Test getTotalSalesAmount() - Total sur période
     */
    @Test
    @DisplayName("✅ Total des ventes sur période - getTotalSalesAmount")
    void testGetTotalSalesAmount() {
        // Given
        Sale sale1 = createSale(BigDecimal.valueOf(100.0), false);
        sale1.setSaleTimestamp(Timestamp.valueOf(LocalDateTime.now().minusDays(2)));

        Sale sale2 = createSale(BigDecimal.valueOf(50.0), false);
        sale2.setSaleTimestamp(Timestamp.valueOf(LocalDateTime.now().minusDays(1)));

        Sale sale3 = createSale(BigDecimal.valueOf(75.0), false);

        entityManager.persistAndFlush(sale1);
        entityManager.persistAndFlush(sale2);
        entityManager.persistAndFlush(sale3);

        // When
        Timestamp startDate = Timestamp.valueOf(LocalDateTime.now().minusDays(7));
        Timestamp endDate = Timestamp.valueOf(LocalDateTime.now().plusDays(1));

        BigDecimal totalAmount = saleRepository.getTotalSalesAmount(
                testClient.getUserId(), startDate, endDate);

        // Then
        assertEquals(BigDecimal.valueOf(225.0), totalAmount);
    }

    /**
     * Test countSalesByPeriod() - Comptage sur période
     */
    @Test
    @DisplayName("✅ Nombre de ventes sur période - countSalesByPeriod")
    void testCountSalesByPeriod() {
        // Given
        Sale sale1 = createSale(BigDecimal.valueOf(100.0), false);
        Sale sale2 = createSale(BigDecimal.valueOf(50.0), false);
        Sale sale3 = createSale(BigDecimal.valueOf(75.0), false);

        entityManager.persistAndFlush(sale1);
        entityManager.persistAndFlush(sale2);
        entityManager.persistAndFlush(sale3);

        // When
        Timestamp startDate = Timestamp.valueOf(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0));
        Timestamp endDate = Timestamp.valueOf(LocalDateTime.now().withHour(23).withMinute(59).withSecond(59));

        long count = saleRepository.countSalesByPeriod(
                testClient.getUserId(), startDate, endDate);

        // Then
        assertEquals(3L, count);
    }

    /**
     * Test existsByIdAndClientId() - Vérification propriété
     */
    @Test
    @DisplayName("✅ Vérification propriété vente - existsByIdAndClientId")
    void testExistsByIdAndClientId() {
        // Given
        Sale sale = createSale(BigDecimal.valueOf(100.0), false);
        sale = entityManager.persistAndFlush(sale);

        // When & Then
        assertTrue(saleRepository.existsByIdAndClientId(
                sale.getSaleId(), testClient.getUserId()));

        assertFalse(saleRepository.existsByIdAndClientId(
                sale.getSaleId(), UUID.randomUUID()));
    }

    /**
     * Test findPendingSalesByClient() - Ventes non finalisées
     */
    @Test
    @DisplayName("✅ Ventes non finalisées - findPendingSalesByClient")
    void testFindPendingSalesByClient() {
        // Given
        Sale pendingSale = createSale(BigDecimal.valueOf(100.0), false);
        // Pas de paiements = vente en attente

        Sale paidSale = createSale(BigDecimal.valueOf(50.0), false);
        // On ajoute un paiement
        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID());
        payment.setSale(paidSale);
        payment.setType("ESPECES");
        payment.setAmount(BigDecimal.valueOf(50.0));
        payment.setCurrency("EUR");
        payment.setPaymentDate(Timestamp.valueOf(LocalDateTime.now()));

        entityManager.persistAndFlush(pendingSale);
        entityManager.persistAndFlush(paidSale);
        entityManager.persistAndFlush(payment);

        // When
        List<Sale> pendingSales = saleRepository.findPendingSalesByClient(testClient.getUserId());

        // Then
        assertEquals(1, pendingSales.size());
        assertEquals(pendingSale.getSaleId(), pendingSales.get(0).getSaleId());
    }

    /**
     * Méthode utilitaire pour créer une Sale
     */
    private Sale createSale(BigDecimal totalAmount, boolean isDeferred) {
        Sale sale = new Sale();
        sale.setSaleId(UUID.randomUUID());
        sale.setTotalAmount(totalAmount);
        sale.setIsDeferred(isDeferred);
        sale.setSaleTimestamp(Timestamp.valueOf(LocalDateTime.now()));
        sale.setClient(testClient);
        return sale;
    }
}