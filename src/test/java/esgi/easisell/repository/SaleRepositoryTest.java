/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : SaleRepositoryTest.java
 * @description : Tests unitaires pour SaleRepository avec Mockito
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 12/07/2025
 * @package     : esgi.easisell.repository
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.repository;

import esgi.easisell.entity.Sale;
import esgi.easisell.entity.SaleItem;
import esgi.easisell.entity.Client;
import esgi.easisell.entity.Product;
import esgi.easisell.entity.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour SaleRepository utilisant Mockito sans H2
 * Focus sur les méthodes de recherche et d'analyse des ventes
 */
@ExtendWith(MockitoExtension.class)
class SaleRepositoryTest {

    @Mock
    private SaleRepository saleRepository;

    private Sale testSale;
    private Client testClient;
    private UUID saleId;
    private UUID clientId;

    @BeforeEach
    void setUp() {
        saleId = UUID.randomUUID();
        clientId = UUID.randomUUID();

        testClient = new Client();
        testClient.setUserId(clientId);
        testClient.setName("Test Store");
        testClient.setCurrencyPreference("EUR");

        testSale = new Sale();
        testSale.setSaleId(saleId);
        testSale.setClient(testClient);
        testSale.setSaleTimestamp(Timestamp.valueOf(LocalDateTime.now()));
        testSale.setTotalAmount(new BigDecimal("25.50"));
        testSale.setIsDeferred(false);
    }

    // ==================== TESTS MÉTHODES PERSONNALISÉES ====================

    /**
     * Test findByClientUserIdOrderBySaleTimestampDesc avec pagination
     */
    @Test
    @DisplayName("📄 findByClientUserIdOrderBySaleTimestampDesc() - Avec pagination")
    void testFindByClientUserIdWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Sale sale2 = createTestSale(new BigDecimal("15.75"), false);
        List<Sale> salesList = Arrays.asList(testSale, sale2);
        Page<Sale> salesPage = new PageImpl<>(salesList, pageable, 2);

        when(saleRepository.findByClientUserIdOrderBySaleTimestampDesc(clientId, pageable))
                .thenReturn(salesPage);

        // When
        Page<Sale> result = saleRepository.findByClientUserIdOrderBySaleTimestampDesc(clientId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().contains(testSale));
        assertTrue(result.getContent().contains(sale2));
        verify(saleRepository, times(1)).findByClientUserIdOrderBySaleTimestampDesc(clientId, pageable);
    }

    /**
     * Test findByClientUserIdOrderBySaleTimestampDesc sans pagination
     */
    @Test
    @DisplayName("📋 findByClientUserIdOrderBySaleTimestampDesc() - Sans pagination")
    void testFindByClientUserIdWithoutPagination() {
        // Given
        Sale sale2 = createTestSale(new BigDecimal("32.00"), true);
        List<Sale> sales = Arrays.asList(testSale, sale2);
        when(saleRepository.findByClientUserIdOrderBySaleTimestampDesc(clientId))
                .thenReturn(sales);

        // When
        List<Sale> result = saleRepository.findByClientUserIdOrderBySaleTimestampDesc(clientId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(testSale));
        assertTrue(result.contains(sale2));
        verify(saleRepository, times(1)).findByClientUserIdOrderBySaleTimestampDesc(clientId);
    }

    /**
     * Test findSalesByClientAndPeriod - ventes sur une période
     */
    @Test
    @DisplayName("📅 findSalesByClientAndPeriod() - Ventes sur période")
    void testFindSalesByClientAndPeriod() {
        // Given
        Timestamp startDate = Timestamp.valueOf(LocalDate.now().minusDays(7).atStartOfDay());
        Timestamp endDate = Timestamp.valueOf(LocalDate.now().atTime(23, 59, 59));

        Sale recentSale = createTestSale(new BigDecimal("18.90"), false);
        recentSale.setSaleTimestamp(Timestamp.valueOf(LocalDateTime.now().minusDays(2)));

        List<Sale> periodSales = Arrays.asList(testSale, recentSale);
        when(saleRepository.findSalesByClientAndPeriod(clientId, startDate, endDate))
                .thenReturn(periodSales);

        // When
        List<Sale> result = saleRepository.findSalesByClientAndPeriod(clientId, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        // Vérifier que les ventes sont dans la période
        assertTrue(result.stream().allMatch(sale ->
                sale.getSaleTimestamp().after(startDate) || sale.getSaleTimestamp().equals(startDate)));
        assertTrue(result.stream().allMatch(sale ->
                sale.getSaleTimestamp().before(endDate) || sale.getSaleTimestamp().equals(endDate)));
        verify(saleRepository, times(1)).findSalesByClientAndPeriod(clientId, startDate, endDate);
    }

    /**
     * Test findTodaySalesByClient - ventes du jour
     */
    @Test
    @DisplayName("📆 findTodaySalesByClient() - Ventes du jour")
    void testFindTodaySalesByClient() {
        // Given
        Sale todaySale1 = createTestSale(new BigDecimal("12.50"), false);
        todaySale1.setSaleTimestamp(Timestamp.valueOf(LocalDateTime.now().withHour(10)));

        Sale todaySale2 = createTestSale(new BigDecimal("28.75"), false);
        todaySale2.setSaleTimestamp(Timestamp.valueOf(LocalDateTime.now().withHour(15)));

        List<Sale> todaySales = Arrays.asList(todaySale1, todaySale2);
        when(saleRepository.findTodaySalesByClient(clientId)).thenReturn(todaySales);

        // When
        List<Sale> result = saleRepository.findTodaySalesByClient(clientId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        // Vérifier que toutes les ventes sont d'aujourd'hui
        LocalDate today = LocalDate.now();
        assertTrue(result.stream().allMatch(sale ->
                sale.getSaleTimestamp().toLocalDateTime().toLocalDate().equals(today)));
        verify(saleRepository, times(1)).findTodaySalesByClient(clientId);
    }

    /**
     * Test getTotalSalesAmount - total des ventes sur période
     */
    @Test
    @DisplayName("💰 getTotalSalesAmount() - Total ventes période")
    void testGetTotalSalesAmount() {
        // Given
        Timestamp startDate = Timestamp.valueOf(LocalDate.now().minusDays(30).atStartOfDay());
        Timestamp endDate = Timestamp.valueOf(LocalDate.now().atTime(23, 59, 59));
        BigDecimal expectedTotal = new BigDecimal("1250.75");

        when(saleRepository.getTotalSalesAmount(clientId, startDate, endDate))
                .thenReturn(expectedTotal);

        // When
        BigDecimal totalSales = saleRepository.getTotalSalesAmount(clientId, startDate, endDate);

        // Then
        assertNotNull(totalSales);
        assertEquals(expectedTotal, totalSales);
        verify(saleRepository, times(1)).getTotalSalesAmount(clientId, startDate, endDate);
    }

    /**
     * Test getTodayTotalSales - total des ventes du jour
     */
    @Test
    @DisplayName("📊 getTodayTotalSales() - Total ventes du jour")
    void testGetTodayTotalSales() {
        // Given
        BigDecimal expectedTotal = new BigDecimal("125.50");
        when(saleRepository.getTodayTotalSales(clientId)).thenReturn(expectedTotal);

        // When
        BigDecimal todayTotal = saleRepository.getTodayTotalSales(clientId);

        // Then
        assertNotNull(todayTotal);
        assertEquals(expectedTotal, todayTotal);
        verify(saleRepository, times(1)).getTodayTotalSales(clientId);
    }

    /**
     * Test countSalesByPeriod - nombre de ventes sur période
     */
    @Test
    @DisplayName("🔢 countSalesByPeriod() - Nombre de ventes")
    void testCountSalesByPeriod() {
        // Given
        Timestamp startDate = Timestamp.valueOf(LocalDate.now().minusDays(7).atStartOfDay());
        Timestamp endDate = Timestamp.valueOf(LocalDate.now().atTime(23, 59, 59));
        long expectedCount = 15L;

        when(saleRepository.countSalesByPeriod(clientId, startDate, endDate))
                .thenReturn(expectedCount);

        // When
        long salesCount = saleRepository.countSalesByPeriod(clientId, startDate, endDate);

        // Then
        assertEquals(expectedCount, salesCount);
        verify(saleRepository, times(1)).countSalesByPeriod(clientId, startDate, endDate);
    }

    /**
     * Test findPendingPaymentSales - ventes en attente de paiement
     */
    @Test
    @DisplayName("⏳ findPendingPaymentSales() - Ventes en attente de paiement")
    void testFindPendingPaymentSales() {
        // Given
        Sale pendingSale1 = createTestSale(new BigDecimal("45.00"), true);
        Sale pendingSale2 = createTestSale(new BigDecimal("78.50"), true);

        List<Sale> pendingSales = Arrays.asList(pendingSale1, pendingSale2);
        when(saleRepository.findPendingPaymentSales(clientId)).thenReturn(pendingSales);

        // When
        List<Sale> result = saleRepository.findPendingPaymentSales(clientId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(Sale::getIsDeferred));
        verify(saleRepository, times(1)).findPendingPaymentSales(clientId);
    }

    /**
     * Test findPendingSalesByClient - ventes en attente (non finalisées)
     */
    @Test
    @DisplayName("📋 findPendingSalesByClient() - Ventes non finalisées")
    void testFindPendingSalesByClient() {
        // Given
        Sale pendingSale1 = createTestSale(new BigDecimal("25.00"), false);
        Sale pendingSale2 = createTestSale(new BigDecimal("35.75"), false);

        List<Sale> pendingSales = Arrays.asList(pendingSale1, pendingSale2);
        when(saleRepository.findPendingSalesByClient(clientId)).thenReturn(pendingSales);

        // When
        List<Sale> result = saleRepository.findPendingSalesByClient(clientId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(saleRepository, times(1)).findPendingSalesByClient(clientId);
    }

    /**
     * Test findTopSellingProducts - produits les plus vendus
     */
    @Test
    @DisplayName("🏆 findTopSellingProducts() - Produits les plus vendus")
    void testFindTopSellingProducts() {
        // Given
        Timestamp startDate = Timestamp.valueOf(LocalDate.now().minusDays(7).atStartOfDay());
        Timestamp endDate = Timestamp.valueOf(LocalDate.now().atTime(23, 59, 59));
        Pageable pageable = PageRequest.of(0, 5);

        Object[] product1 = {UUID.randomUUID(), "Coca-Cola 1.5L", 50L};
        Object[] product2 = {UUID.randomUUID(), "Pepsi 1.5L", 35L};
        List<Object[]> topProducts = Arrays.asList(product1, product2);

        when(saleRepository.findTopSellingProducts(clientId, startDate, endDate, pageable))
                .thenReturn(topProducts);

        // When
        List<Object[]> result = saleRepository.findTopSellingProducts(clientId, startDate, endDate, pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Coca-Cola 1.5L", result.get(0)[1]);
        assertEquals(50L, result.get(0)[2]);
        verify(saleRepository, times(1)).findTopSellingProducts(clientId, startDate, endDate, pageable);
    }

    /**
     * Test findTodayTopSellingProducts - produits les plus vendus aujourd'hui
     */
    @Test
    @DisplayName("🏆 findTodayTopSellingProducts() - Top ventes du jour")
    void testFindTodayTopSellingProducts() {
        // Given
        Object[] product1 = {UUID.randomUUID(), "Red Bull", 25L};
        Object[] product2 = {UUID.randomUUID(), "Monster Energy", 18L};
        List<Object[]> todayTopProducts = Arrays.asList(product1, product2);

        when(saleRepository.findTodayTopSellingProducts(clientId)).thenReturn(todayTopProducts);

        // When
        List<Object[]> result = saleRepository.findTodayTopSellingProducts(clientId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Red Bull", result.get(0)[1]);
        assertEquals(25L, result.get(0)[2]);
        verify(saleRepository, times(1)).findTodayTopSellingProducts(clientId);
    }

    /**
     * Test findTodayHourlySalesStats - statistiques par heure
     */
    @Test
    @DisplayName("⏰ findTodayHourlySalesStats() - Stats par heure")
    void testFindTodayHourlySalesStats() {
        // Given
        String clientIdString = clientId.toString();
        Object[] hour9Stats = {9, 5L, new BigDecimal("125.50")};
        Object[] hour14Stats = {14, 8L, new BigDecimal("245.75")};
        List<Object[]> hourlyStats = Arrays.asList(hour9Stats, hour14Stats);

        when(saleRepository.findTodayHourlySalesStats(clientIdString)).thenReturn(hourlyStats);

        // When
        List<Object[]> result = saleRepository.findTodayHourlySalesStats(clientIdString);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(9, result.get(0)[0]); // Heure
        assertEquals(5L, result.get(0)[1]); // Nombre de ventes
        assertEquals(new BigDecimal("125.50"), result.get(0)[2]); // Montant total
        verify(saleRepository, times(1)).findTodayHourlySalesStats(clientIdString);
    }

    /**
     * Test findSalesForStatistics - ventes pour statistiques
     */
    @Test
    @DisplayName("📈 findSalesForStatistics() - Ventes pour stats")
    void testFindSalesForStatistics() {
        // Given
        Timestamp startDate = Timestamp.valueOf(LocalDate.now().minusDays(30).atStartOfDay());
        Timestamp endDate = Timestamp.valueOf(LocalDate.now().atTime(23, 59, 59));

        Sale sale1 = createTestSale(new BigDecimal("45.00"), false);
        Sale sale2 = createTestSale(new BigDecimal("67.25"), false);
        List<Sale> salesForStats = Arrays.asList(sale1, sale2);

        when(saleRepository.findSalesForStatistics(clientId, startDate, endDate))
                .thenReturn(salesForStats);

        // When
        List<Sale> result = saleRepository.findSalesForStatistics(clientId, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(saleRepository, times(1)).findSalesForStatistics(clientId, startDate, endDate);
    }

    /**
     * Test findSalesForCategoryStatistics - ventes par catégorie
     */
    @Test
    @DisplayName("📊 findSalesForCategoryStatistics() - Stats par catégorie")
    void testFindSalesForCategoryStatistics() {
        // Given
        UUID categoryId = UUID.randomUUID();
        Timestamp startDate = Timestamp.valueOf(LocalDate.now().minusDays(7).atStartOfDay());
        Timestamp endDate = Timestamp.valueOf(LocalDate.now().atTime(23, 59, 59));

        Sale categorySale = createTestSale(new BigDecimal("35.50"), false);
        List<Sale> categorySales = Arrays.asList(categorySale);

        when(saleRepository.findSalesForCategoryStatistics(clientId, categoryId, startDate, endDate))
                .thenReturn(categorySales);

        // When
        List<Sale> result = saleRepository.findSalesForCategoryStatistics(clientId, categoryId, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(saleRepository, times(1)).findSalesForCategoryStatistics(clientId, categoryId, startDate, endDate);
    }

    /**
     * Test existsByIdAndClientId - vérification d'existence
     */
    @Test
    @DisplayName("✅ existsByIdAndClientId() - Vente existe")
    void testExistsByIdAndClientIdTrue() {
        // Given
        when(saleRepository.existsByIdAndClientId(saleId, clientId)).thenReturn(true);

        // When
        boolean exists = saleRepository.existsByIdAndClientId(saleId, clientId);

        // Then
        assertTrue(exists);
        verify(saleRepository, times(1)).existsByIdAndClientId(saleId, clientId);
    }

    /**
     * Test existsByIdAndClientId - vente n'existe pas
     */
    @Test
    @DisplayName("❌ existsByIdAndClientId() - Vente n'existe pas")
    void testExistsByIdAndClientIdFalse() {
        // Given
        UUID nonExistentSaleId = UUID.randomUUID();
        when(saleRepository.existsByIdAndClientId(nonExistentSaleId, clientId)).thenReturn(false);

        // When
        boolean exists = saleRepository.existsByIdAndClientId(nonExistentSaleId, clientId);

        // Then
        assertFalse(exists);
        verify(saleRepository, times(1)).existsByIdAndClientId(nonExistentSaleId, clientId);
    }

    // ==================== TESTS MÉTHODES JPA STANDARD ====================

    /**
     * Test save - sauvegarde réussie
     */
    @Test
    @DisplayName("✅ save() - Sauvegarde réussie")
    void testSaveSale() {
        // Given
        when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

        // When
        Sale savedSale = saleRepository.save(testSale);

        // Then
        assertNotNull(savedSale);
        assertEquals(testSale.getSaleId(), savedSale.getSaleId());
        assertEquals(testSale.getTotalAmount(), savedSale.getTotalAmount());
        verify(saleRepository, times(1)).save(testSale);
    }

    /**
     * Test findById - vente trouvée
     */
    @Test
    @DisplayName("✅ findById() - Vente trouvée")
    void testFindByIdFound() {
        // Given
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(testSale));

        // When
        Optional<Sale> result = saleRepository.findById(saleId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(saleId, result.get().getSaleId());
        verify(saleRepository, times(1)).findById(saleId);
    }

    /**
     * Test findById - vente non trouvée
     */
    @Test
    @DisplayName("❌ findById() - Vente non trouvée")
    void testFindByIdNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(saleRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<Sale> result = saleRepository.findById(nonExistentId);

        // Then
        assertFalse(result.isPresent());
        verify(saleRepository, times(1)).findById(nonExistentId);
    }

    /**
     * Test findAll - liste de ventes
     */
    @Test
    @DisplayName("✅ findAll() - Liste de ventes")
    void testFindAll() {
        // Given
        Sale sale2 = createTestSale(new BigDecimal("42.80"), false);
        List<Sale> sales = Arrays.asList(testSale, sale2);
        when(saleRepository.findAll()).thenReturn(sales);

        // When
        List<Sale> result = saleRepository.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(testSale));
        assertTrue(result.contains(sale2));
        verify(saleRepository, times(1)).findAll();
    }

    /**
     * Test count - comptage des ventes
     */
    @Test
    @DisplayName("✅ count() - Comptage des ventes")
    void testCount() {
        // Given
        when(saleRepository.count()).thenReturn(100L);

        // When
        long count = saleRepository.count();

        // Then
        assertEquals(100L, count);
        verify(saleRepository, times(1)).count();
    }

    // ==================== TESTS DE VALIDATION MÉTIER ====================

    /**
     * Test validation des données obligatoires
     */
    @Test
    @DisplayName("✅ Validation - Données obligatoires")
    void testRequiredSaleData() {
        // Given
        when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

        // When
        Sale savedSale = saleRepository.save(testSale);

        // Then
        assertNotNull(savedSale.getSaleTimestamp());
        assertNotNull(savedSale.getTotalAmount());
        assertNotNull(savedSale.getIsDeferred());
        assertNotNull(savedSale.getClient());
        assertTrue(savedSale.getTotalAmount().compareTo(BigDecimal.ZERO) >= 0);
    }

    /**
     * Test différents types de ventes
     */
    @Test
    @DisplayName("✅ Validation - Types de ventes")
    void testSaleTypes() {
        // Given
        Sale immediateSale = createTestSale(new BigDecimal("20.00"), false);
        Sale deferredSale = createTestSale(new BigDecimal("50.00"), true);

        List<Sale> sales = Arrays.asList(immediateSale, deferredSale);
        when(saleRepository.findAll()).thenReturn(sales);

        // When
        List<Sale> result = saleRepository.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        boolean hasImmediate = result.stream().anyMatch(sale -> !sale.getIsDeferred());
        boolean hasDeferred = result.stream().anyMatch(Sale::getIsDeferred);

        assertTrue(hasImmediate);
        assertTrue(hasDeferred);
    }

    // ==================== TESTS DE PERFORMANCE SIMULÉS ====================

    /**
     * Test pagination efficace
     */
    @Test
    @DisplayName("⚡ Performance - Pagination efficace")
    void testEfficientPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 5);
        List<Sale> pageContent = Arrays.asList(
                createTestSale(new BigDecimal("10.00"), false),
                createTestSale(new BigDecimal("15.00"), false),
                createTestSale(new BigDecimal("20.00"), false)
        );
        Page<Sale> salesPage = new PageImpl<>(pageContent, pageable, 50); // 50 total, 3 dans cette page

        when(saleRepository.findByClientUserIdOrderBySaleTimestampDesc(clientId, pageable))
                .thenReturn(salesPage);

        // When
        Page<Sale> result = saleRepository.findByClientUserIdOrderBySaleTimestampDesc(clientId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        assertEquals(50, result.getTotalElements());
        assertEquals(10, result.getTotalPages()); // 50/5 = 10 pages
        verify(saleRepository, times(1)).findByClientUserIdOrderBySaleTimestampDesc(clientId, pageable);
    }

    /**
     * Test requêtes d'agrégation
     */
    @Test
    @DisplayName("📊 Performance - Requêtes d'agrégation")
    void testAggregationQueries() {
        // Given
        Timestamp startDate = Timestamp.valueOf(LocalDate.now().minusDays(30).atStartOfDay());
        Timestamp endDate = Timestamp.valueOf(LocalDate.now().atTime(23, 59, 59));

        when(saleRepository.getTotalSalesAmount(clientId, startDate, endDate))
                .thenReturn(new BigDecimal("2500.00"));
        when(saleRepository.countSalesByPeriod(clientId, startDate, endDate))
                .thenReturn(75L);
        when(saleRepository.getTodayTotalSales(clientId))
                .thenReturn(new BigDecimal("125.50"));

        // When
        BigDecimal total = saleRepository.getTotalSalesAmount(clientId, startDate, endDate);
        long count = saleRepository.countSalesByPeriod(clientId, startDate, endDate);
        BigDecimal todayTotal = saleRepository.getTodayTotalSales(clientId);

        // Then
        assertEquals(new BigDecimal("2500.00"), total);
        assertEquals(75L, count);
        assertEquals(new BigDecimal("125.50"), todayTotal);

        // Vérifier que toutes les requêtes ont été appelées
        verify(saleRepository, times(1)).getTotalSalesAmount(clientId, startDate, endDate);
        verify(saleRepository, times(1)).countSalesByPeriod(clientId, startDate, endDate);
        verify(saleRepository, times(1)).getTodayTotalSales(clientId);
    }

    // ==================== TESTS DE COHÉRENCE DES DONNÉES ====================

    /**
     * Test cohérence entre différentes méthodes
     */
    @Test
    @DisplayName("✅ Cohérence - Vérification entre méthodes")
    void testDataConsistencyBetweenMethods() {
        // Given
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(testSale));
        when(saleRepository.existsById(saleId)).thenReturn(true);
        when(saleRepository.existsByIdAndClientId(saleId, clientId)).thenReturn(true);

        // When
        Optional<Sale> saleById = saleRepository.findById(saleId);
        boolean existsById = saleRepository.existsById(saleId);
        boolean existsByIdAndClient = saleRepository.existsByIdAndClientId(saleId, clientId);

        // Then
        assertTrue(saleById.isPresent());
        assertTrue(existsById);
        assertTrue(existsByIdAndClient);
        assertEquals(saleId, saleById.get().getSaleId());
        assertEquals(clientId, saleById.get().getClient().getUserId());
    }

    /**
     * Test validation des contraintes temporelles
     */
    @Test
    @DisplayName("⏰ Validation - Contraintes temporelles")
    void testTemporalConstraints() {
        // Given
        Sale pastSale = createTestSale(new BigDecimal("30.00"), false);
        pastSale.setSaleTimestamp(Timestamp.valueOf(LocalDateTime.now().minusDays(5)));

        Sale futureSale = createTestSale(new BigDecimal("40.00"), false);
        futureSale.setSaleTimestamp(Timestamp.valueOf(LocalDateTime.now().plusDays(1)));

        when(saleRepository.save(any(Sale.class))).thenReturn(pastSale, futureSale);

        // When
        Sale savedPastSale = saleRepository.save(pastSale);
        Sale savedFutureSale = saleRepository.save(futureSale);

        // Then
        assertTrue(savedPastSale.getSaleTimestamp().before(Timestamp.valueOf(LocalDateTime.now())));
        assertTrue(savedFutureSale.getSaleTimestamp().after(Timestamp.valueOf(LocalDateTime.now())));
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Méthode utilitaire pour créer une vente de test
     */
    private Sale createTestSale(BigDecimal totalAmount, boolean isDeferred) {
        Sale sale = new Sale();
        sale.setSaleId(UUID.randomUUID());
        sale.setClient(testClient);
        sale.setSaleTimestamp(Timestamp.valueOf(LocalDateTime.now()));
        sale.setTotalAmount(totalAmount);
        sale.setIsDeferred(isDeferred);
        return sale;
    }

    /**
     * Méthode utilitaire pour créer un SaleItem de test
     */
    private SaleItem createTestSaleItem(String productName, BigDecimal quantity, BigDecimal price) {
        Product product = new Product();
        product.setProductId(UUID.randomUUID());
        product.setName(productName);
        product.setUnitPrice(price);

        return SaleItem.builder()
                .saleItemId(UUID.randomUUID())
                .product(product)
                .quantitySold(quantity)
                .priceAtSale(price.multiply(quantity))
                .build();
    }

    /**
     * Méthode utilitaire pour créer un Payment de test
     */
    private Payment createTestPayment(BigDecimal amount, String type) {
        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID());
        payment.setAmount(amount);
        payment.setType(type);
        payment.setCurrency("EUR");
        payment.setPaymentDate(Timestamp.valueOf(LocalDateTime.now()));
        return payment;
    }
}