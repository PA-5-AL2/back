package esgi.easisell.repository;

import esgi.easisell.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour SaleRepository
 * Utilise H2 en mémoire pour tester les requêtes custom
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests d'intégration pour SaleRepository")
class SaleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private Client testClient;
    private Product testProduct;
    private Category testCategory;
    private Sale testSale;
    private Sale paidSale;
    private Sale pendingSale;

    @BeforeEach
    void setUp() {
        // Créer un client de test
        testClient = new Client();
        testClient.setUsername("client@test.com");
        testClient.setPassword("password");
        testClient.setFirstName("John");
        testClient.setRole("CLIENT");
        testClient.setName("Test Store");
        testClient.setAddress("123 Test Street");
        testClient.setContractStatus("ACTIVE");
        testClient.setCurrencyPreference("EUR");
        testClient.setAccessCode("TEST1234");
        testClient = entityManager.persistAndFlush(testClient);

        // Créer une catégorie
        testCategory = new Category();
        testCategory.setName("Électronique");
        testCategory.setClient(testClient);
        testCategory = entityManager.persistAndFlush(testCategory);

        // Créer un produit
        testProduct = new Product();
        testProduct.setName("iPhone 15");
        testProduct.setDescription("Smartphone Apple");
        testProduct.setBarcode("123456789");
        testProduct.setBrand("Apple");
        testProduct.setUnitPrice(new BigDecimal("999.99"));
        testProduct.setCategory(testCategory);
        testProduct.setClient(testClient);
        testProduct = entityManager.persistAndFlush(testProduct);

        // Créer une vente avec paiement (finalisée)
        paidSale = new Sale();
        paidSale.setClient(testClient);
        paidSale.setSaleTimestamp(Timestamp.valueOf(LocalDateTime.now()));
        paidSale.setTotalAmount(new BigDecimal("999.99"));
        paidSale.setIsDeferred(false);
        paidSale = entityManager.persistAndFlush(paidSale);

        // Ajouter un item à la vente
        SaleItem saleItem = new SaleItem();
        saleItem.setSale(paidSale);
        saleItem.setProduct(testProduct);
        saleItem.setQuantitySold(BigDecimal.ONE);
        saleItem.setPriceAtSale(new BigDecimal("999.99"));
        entityManager.persistAndFlush(saleItem);

        // Ajouter un paiement pour finaliser la vente
        Payment payment = new Payment();
        payment.setSale(paidSale);
        payment.setType("CARTE");
        payment.setAmount(new BigDecimal("999.99"));
        payment.setCurrency("EUR");
        entityManager.persistAndFlush(payment);

        // Créer une vente en attente (sans paiement)
        pendingSale = new Sale();
        pendingSale.setClient(testClient);
        pendingSale.setSaleTimestamp(Timestamp.valueOf(LocalDateTime.now().minusHours(1)));
        pendingSale.setTotalAmount(new BigDecimal("500.00"));
        pendingSale.setIsDeferred(false);
        pendingSale = entityManager.persistAndFlush(pendingSale);

        // Ajouter un item à la vente en attente
        SaleItem pendingItem = new SaleItem();
        pendingItem.setSale(pendingSale);
        pendingItem.setProduct(testProduct);
        pendingItem.setQuantitySold(BigDecimal.valueOf(0.5));
        pendingItem.setPriceAtSale(new BigDecimal("999.99"));
        entityManager.persistAndFlush(pendingItem);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("findByClientUserIdOrderBySaleTimestampDesc devrait retourner les ventes du client")
    void findByClientUserId_ShouldReturnClientSales() {
        // When
        List<Sale> sales = saleRepository.findByClientUserIdOrderBySaleTimestampDesc(testClient.getUserId());

        // Then
        assertNotNull(sales);
        assertEquals(2, sales.size());
        // Vérifier l'ordre décroissant par timestamp
        assertTrue(sales.get(0).getSaleTimestamp().after(sales.get(1).getSaleTimestamp()) ||
                sales.get(0).getSaleTimestamp().equals(sales.get(1).getSaleTimestamp()));
    }

    @Test
    @DisplayName("findByClientUserIdOrderBySaleTimestampDesc avec pagination devrait fonctionner")
    void findByClientUserIdWithPagination_ShouldWork() {
        // Given
        Pageable pageable = PageRequest.of(0, 1);

        // When
        Page<Sale> salesPage = saleRepository.findByClientUserIdOrderBySaleTimestampDesc(
                testClient.getUserId(), pageable);

        // Then
        assertNotNull(salesPage);
        assertEquals(1, salesPage.getContent().size());
        assertEquals(2, salesPage.getTotalElements());
        assertEquals(2, salesPage.getTotalPages());
    }

    @Test
    @DisplayName("findTodaySalesByClient devrait retourner les ventes du jour")
    void findTodaySalesByClient_ShouldReturnTodaySales() {
        // When
        List<Sale> todaySales = saleRepository.findTodaySalesByClient(testClient.getUserId());

        // Then
        assertNotNull(todaySales);
        assertEquals(2, todaySales.size()); // Les deux ventes ont été créées aujourd'hui
    }

    @Test
    @DisplayName("getTodayTotalSales devrait calculer le total du jour")
    void getTodayTotalSales_ShouldCalculateTodayTotal() {
        // When
        BigDecimal todayTotal = saleRepository.getTodayTotalSales(testClient.getUserId());

        // Then
        assertNotNull(todayTotal);
        assertEquals(new BigDecimal("1499.99"), todayTotal); // 999.99 + 500.00
    }

    @Test
    @DisplayName("findPendingSalesByClient devrait retourner les ventes en attente")
    void findPendingSalesByClient_ShouldReturnPendingSales() {
        // When
        List<Sale> pendingSales = saleRepository.findPendingSalesByClient(testClient.getUserId());

        // Then
        assertNotNull(pendingSales);
        assertEquals(1, pendingSales.size());
        assertEquals(pendingSale.getSaleId(), pendingSales.get(0).getSaleId());
    }

    @Test
    @DisplayName("existsByIdAndClientId devrait vérifier l'appartenance de la vente")
    void existsByIdAndClientId_ShouldCheckSaleOwnership() {
        // When - vente appartenant au client
        boolean exists = saleRepository.existsByIdAndClientId(
                paidSale.getSaleId(), testClient.getUserId());

        // When - vente avec UUID random
        boolean notExists = saleRepository.existsByIdAndClientId(
                UUID.randomUUID(), testClient.getUserId());

        // Then
        assertTrue(exists);
        assertFalse(notExists);
    }

    @Test
    @DisplayName("findTodayTopSellingProducts devrait retourner les produits populaires")
    void findTodayTopSellingProducts_ShouldReturnTopProducts() {
        // When
        List<Object[]> topProducts = saleRepository.findTodayTopSellingProducts(testClient.getUserId());

        // Then
        assertNotNull(topProducts);
        assertEquals(1, topProducts.size());

        Object[] productData = topProducts.get(0);
        assertEquals(testProduct.getProductId(), productData[0]);
        assertEquals("iPhone 15", productData[1]);
        // La quantité totale devrait être 1.5 (1 + 0.5)
        assertEquals(new BigDecimal("1.5"), productData[2]);
    }

    @Test
    @DisplayName("findSalesByClientAndPeriod devrait filtrer par période")
    void findSalesByClientAndPeriod_ShouldFilterByPeriod() {
        // Given
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        Timestamp startDate = Timestamp.valueOf(startOfDay);
        Timestamp endDate = Timestamp.valueOf(endOfDay);

        // When
        List<Sale> salesInPeriod = saleRepository.findSalesByClientAndPeriod(
                testClient.getUserId(), startDate, endDate);

        // Then
        assertNotNull(salesInPeriod);
        assertEquals(2, salesInPeriod.size());
    }

    @Test
    @DisplayName("getTotalSalesAmount devrait calculer le total sur une période")
    void getTotalSalesAmount_ShouldCalculatePeriodTotal() {
        // Given
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        Timestamp startDate = Timestamp.valueOf(startOfDay);
        Timestamp endDate = Timestamp.valueOf(endOfDay);

        // When
        BigDecimal totalAmount = saleRepository.getTotalSalesAmount(
                testClient.getUserId(), startDate, endDate);

        // Then
        assertNotNull(totalAmount);
        assertEquals(new BigDecimal("1499.99"), totalAmount);
    }

    @Test
    @DisplayName("countSalesByPeriod devrait compter les ventes sur une période")
    void countSalesByPeriod_ShouldCountSalesInPeriod() {
        // Given
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        Timestamp startDate = Timestamp.valueOf(startOfDay);
        Timestamp endDate = Timestamp.valueOf(endOfDay);

        // When
        long salesCount = saleRepository.countSalesByPeriod(
                testClient.getUserId(), startDate, endDate);

        // Then
        assertEquals(2L, salesCount);
    }

    @Test
    @DisplayName("findPendingPaymentSales devrait retourner les ventes différées sans paiement")
    void findPendingPaymentSales_ShouldReturnDeferredSalesWithoutPayment() {
        // Given - Créer une vente différée sans paiement
        Sale deferredSale = new Sale();
        deferredSale.setClient(testClient);
        deferredSale.setSaleTimestamp(Timestamp.valueOf(LocalDateTime.now()));
        deferredSale.setTotalAmount(new BigDecimal("750.00"));
        deferredSale.setIsDeferred(true); // Différée
        entityManager.persistAndFlush(deferredSale);

        // When
        List<Sale> pendingPaymentSales = saleRepository.findPendingPaymentSales(testClient.getUserId());

        // Then
        assertNotNull(pendingPaymentSales);
        assertEquals(1, pendingPaymentSales.size());
        assertEquals(deferredSale.getSaleId(), pendingPaymentSales.get(0).getSaleId());
        assertTrue(pendingPaymentSales.get(0).getIsDeferred());
    }

    @Test
    @DisplayName("findTopSellingProducts devrait retourner les produits avec pagination")
    void findTopSellingProducts_ShouldReturnTopProductsWithPagination() {
        // Given
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        Timestamp startDate = Timestamp.valueOf(startOfDay);
        Timestamp endDate = Timestamp.valueOf(endOfDay);
        Pageable pageable = PageRequest.of(0, 5);

        // When
        List<Object[]> topProducts = saleRepository.findTopSellingProducts(
                testClient.getUserId(), startDate, endDate, pageable);

        // Then
        assertNotNull(topProducts);
        assertEquals(1, topProducts.size());
    }

    @Test
    @DisplayName("findSalesForStatistics devrait charger les items avec FETCH")
    void findSalesForStatistics_ShouldLoadItemsWithFetch() {
        // Given
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        Timestamp startDate = Timestamp.valueOf(startOfDay);
        Timestamp endDate = Timestamp.valueOf(endOfDay);

        // When
        List<Sale> salesForStats = saleRepository.findSalesForStatistics(
                testClient.getUserId(), startDate, endDate);

        // Then
        assertNotNull(salesForStats);
        assertEquals(2, salesForStats.size());

        // Vérifier que les SaleItems sont bien chargés (pas de lazy loading)
        for (Sale sale : salesForStats) {
            assertNotNull(sale.getSaleItems());
            assertFalse(sale.getSaleItems().isEmpty());
        }
    }

    @Test
    @DisplayName("Ne devrait pas retourner de ventes pour un client inexistant")
    void shouldNotReturnSalesForNonExistentClient() {
        // Given
        UUID nonExistentClientId = UUID.randomUUID();

        // When
        List<Sale> sales = saleRepository.findByClientUserIdOrderBySaleTimestampDesc(nonExistentClientId);
        BigDecimal todayTotal = saleRepository.getTodayTotalSales(nonExistentClientId);

        // Then
        assertNotNull(sales);
        assertTrue(sales.isEmpty());
        assertEquals(BigDecimal.ZERO, todayTotal);
    }
}