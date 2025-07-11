/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : SaleDetailsMapperTest.java
 * @description : Tests unitaires pour le mapper SaleDetailsMapper
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 11/07/2025
 * @package     : esgi.easisell.mapper
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.mapper;

import esgi.easisell.dto.*;
import esgi.easisell.entity.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour SaleDetailsMapper
 *
 * Teste la méthode :
 * - toDTO() - Mapping complet avec relations et calculs
 */
class SaleDetailsMapperTest {

    private Sale sale;
    private Client client;
    private List<SaleItem> saleItems;
    private List<Payment> payments;
    private Product product1;
    private Product product2;
    private Category category;

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
        client = new Client();
        client.setUserId(UUID.randomUUID());
        client.setUsername("supermarket@easisell.com");
        client.setFirstName("SuperMarket");
        client.setName("SuperMarket Central");
        client.setAddress("123 Rue du Commerce");
        client.setCurrencyPreference("EUR");

        // Créer la catégorie
        category = new Category();
        category.setCategoryId(UUID.randomUUID());
        category.setName("Alimentaire");
        category.setClient(client);

        // Créer les produits
        product1 = new Product();
        product1.setProductId(UUID.randomUUID());
        product1.setName("Bananes");
        product1.setBarcode("1111111111111");
        product1.setUnitPrice(new BigDecimal("2.50"));
        product1.setCategory(category);
        product1.setClient(client);
        product1.setIsSoldByWeight(true);
        product1.setUnitLabel("kg");

        product2 = new Product();
        product2.setProductId(UUID.randomUUID());
        product2.setName("Pain");
        product2.setBarcode("2222222222222");
        product2.setUnitPrice(new BigDecimal("1.20"));
        product2.setCategory(category);
        product2.setClient(client);
        product2.setIsSoldByWeight(false);
        product2.setUnitLabel("pièce");

        // Créer la vente
        sale = new Sale();
        sale.setSaleId(UUID.randomUUID());
        sale.setClient(client);
        sale.setSaleTimestamp(Timestamp.valueOf(LocalDateTime.now()));
        sale.setTotalAmount(new BigDecimal("8.70"));
        sale.setIsDeferred(false);

        // Créer les articles de vente
        saleItems = new ArrayList<>();

        SaleItem item1 = new SaleItem();
        item1.setSaleItemId(UUID.randomUUID());
        item1.setSale(sale);
        item1.setProduct(product1);
        item1.setQuantitySold(new BigDecimal("1.500"));
        item1.setPriceAtSale(new BigDecimal("3.75"));
        saleItems.add(item1);

        SaleItem item2 = new SaleItem();
        item2.setSaleItemId(UUID.randomUUID());
        item2.setSale(sale);
        item2.setProduct(product2);
        item2.setQuantitySold(new BigDecimal("4.000"));
        item2.setPriceAtSale(new BigDecimal("4.80"));
        saleItems.add(item2);

        sale.setSaleItems(saleItems);

        // Créer les paiements
        payments = new ArrayList<>();

        Payment payment1 = new Payment();
        payment1.setPaymentId(UUID.randomUUID());
        payment1.setSale(sale);
        payment1.setType("ESPECES");
        payment1.setAmount(new BigDecimal("5.00"));
        payment1.setCurrency("EUR");
        payment1.setPaymentDate(Timestamp.valueOf(LocalDateTime.now()));
        payments.add(payment1);

        Payment payment2 = new Payment();
        payment2.setPaymentId(UUID.randomUUID());
        payment2.setSale(sale);
        payment2.setType("CARTE");
        payment2.setAmount(new BigDecimal("3.70"));
        payment2.setCurrency("EUR");
        payment2.setPaymentDate(Timestamp.valueOf(LocalDateTime.now()));
        payments.add(payment2);

        sale.setPayments(payments);
    }

    // ==================== TESTS toDTO() ====================

    /**
     * Test toDTO() avec vente complète
     */
    @Test
    @DisplayName("✅ toDTO() - Vente complète avec articles et paiements")
    void testToDTOComplete() {
        // When
        SaleDetailsDTO result = SaleDetailsMapper.toDTO(sale);

        // Then
        assertNotNull(result);

        // Vérifier les informations de vente
        assertEquals(sale.getSaleId(), result.getSaleId());
        assertEquals(sale.getSaleTimestamp(), result.getSaleTimestamp());
        assertEquals(sale.getTotalAmount(), result.getTotalAmount());
        assertEquals(sale.getIsDeferred(), result.getIsDeferred());
        assertEquals(true, result.getIsPaid()); // A des paiements

        // Vérifier les informations client
        assertNotNull(result.getClient());
        ClientInfoDTO clientInfo = result.getClient();
        assertEquals(client.getUserId(), clientInfo.getClientId());
        assertEquals(client.getUsername(), clientInfo.getUsername());
        assertEquals(client.getFirstName(), clientInfo.getFirstName());
        assertEquals(client.getName(), clientInfo.getName());
        assertEquals(client.getAddress(), clientInfo.getAddress());
        assertEquals(client.getCurrencyPreference(), clientInfo.getCurrencyPreference());

        // Vérifier les articles
        assertNotNull(result.getItems());
        assertEquals(2, result.getItems().size());

        SaleItemResponseDTO item1 = result.getItems().get(0);
        assertEquals(saleItems.get(0).getSaleItemId(), item1.getSaleItemId());
        assertEquals(product1.getName(), item1.getProductName());
        assertEquals(product1.getBarcode(), item1.getProductBarcode());

        SaleItemResponseDTO item2 = result.getItems().get(1);
        assertEquals(saleItems.get(1).getSaleItemId(), item2.getSaleItemId());
        assertEquals(product2.getName(), item2.getProductName());
        assertEquals(product2.getBarcode(), item2.getProductBarcode());

        // Vérifier les paiements
        assertNotNull(result.getPayments());
        assertEquals(2, result.getPayments().size());

        PaymentInfoDTO payment1Info = result.getPayments().get(0);
        assertEquals(payments.get(0).getPaymentId(), payment1Info.getPaymentId());
        assertEquals("ESPECES", payment1Info.getType());
        assertEquals(new BigDecimal("5.00"), payment1Info.getAmount());
        assertEquals("EUR", payment1Info.getCurrency());

        PaymentInfoDTO payment2Info = result.getPayments().get(1);
        assertEquals(payments.get(1).getPaymentId(), payment2Info.getPaymentId());
        assertEquals("CARTE", payment2Info.getType());
        assertEquals(new BigDecimal("3.70"), payment2Info.getAmount());
        assertEquals("EUR", payment2Info.getCurrency());
    }

    /**
     * Test toDTO() avec vente sans paiements
     */
    @Test
    @DisplayName("✅ toDTO() - Vente sans paiements (non payée)")
    void testToDTOWithoutPayments() {
        // Given
        sale.setPayments(new ArrayList<>());

        // When
        SaleDetailsDTO result = SaleDetailsMapper.toDTO(sale);

        // Then
        assertNotNull(result);
        assertEquals(false, result.getIsPaid());
        assertNotNull(result.getPayments());
        assertEquals(0, result.getPayments().size());
    }

    /**
     * Test toDTO() avec vente différée
     */
    @Test
    @DisplayName("✅ toDTO() - Vente différée")
    void testToDTODeferred() {
        // Given
        sale.setIsDeferred(true);

        // When
        SaleDetailsDTO result = SaleDetailsMapper.toDTO(sale);

        // Then
        assertEquals(true, result.getIsDeferred());
    }

    /**
     * Test toDTO() avec articles vides
     */
    @Test
    @DisplayName("✅ toDTO() - Vente sans articles")
    void testToDTOWithoutItems() {
        // Given
        sale.setSaleItems(new ArrayList<>());

        // When
        SaleDetailsDTO result = SaleDetailsMapper.toDTO(sale);

        // Then
        assertNotNull(result);
        assertNotNull(result.getItems());
        assertEquals(0, result.getItems().size());
    }

    /**
     * Test toDTO() avec formatage des unités
     */
    @Test
    @DisplayName("✅ toDTO() - Vérification du formatage des unités dans les articles")
    void testToDTOFormattingUnits() {
        // When
        SaleDetailsDTO result = SaleDetailsMapper.toDTO(sale);

        // Then
        List<SaleItemResponseDTO> items = result.getItems();

        // Premier article (au poids)
        SaleItemResponseDTO item1 = items.get(0);
        assertEquals(true, item1.getIsSoldByWeight());
        assertEquals("kg", item1.getUnitLabel());
        // Le formatage sera fait par SaleItemMapper.toResponseDTO()

        // Deuxième article (à la pièce)
        SaleItemResponseDTO item2 = items.get(1);
        assertEquals(false, item2.getIsSoldByWeight());
        assertEquals("pièce", item2.getUnitLabel());
    }

    /**
     * Test toDTO() avec montants complexes
     */
    @Test
    @DisplayName("✅ toDTO() - Montants avec décimales complexes")
    void testToDTOComplexAmounts() {
        // Given
        sale.setTotalAmount(new BigDecimal("123.456"));

        Payment payment = payments.get(0);
        payment.setAmount(new BigDecimal("99.999"));

        // When
        SaleDetailsDTO result = SaleDetailsMapper.toDTO(sale);

        // Then
        assertEquals(new BigDecimal("123.456"), result.getTotalAmount());
        assertEquals(new BigDecimal("99.999"), result.getPayments().get(0).getAmount());
    }

    /**
     * Test toDTO() avec différentes devises
     */
    @Test
    @DisplayName("✅ toDTO() - Gestion de différentes devises")
    void testToDTODifferentCurrencies() {
        // Given
        client.setCurrencyPreference("USD");
        payments.get(0).setCurrency("USD");
        payments.get(1).setCurrency("EUR");

        // When
        SaleDetailsDTO result = SaleDetailsMapper.toDTO(sale);

        // Then
        assertEquals("USD", result.getClient().getCurrencyPreference());
        assertEquals("USD", result.getPayments().get(0).getCurrency());
        assertEquals("EUR", result.getPayments().get(1).getCurrency());
    }

    /**
     * Test toDTO() avec client ayant des informations minimales
     */
    @Test
    @DisplayName("✅ toDTO() - Client avec informations minimales")
    void testToDTOMinimalClientInfo() {
        // Given
        client.setFirstName(null);
        client.setAddress(null);
        client.setCurrencyPreference(null);

        // When
        SaleDetailsDTO result = SaleDetailsMapper.toDTO(sale);

        // Then
        ClientInfoDTO clientInfo = result.getClient();
        assertEquals(client.getUserId(), clientInfo.getClientId());
        assertEquals(client.getUsername(), clientInfo.getUsername());
        assertNull(clientInfo.getFirstName());
        assertNull(clientInfo.getAddress());
        assertNull(clientInfo.getCurrencyPreference());
        assertEquals(client.getName(), clientInfo.getName()); // Toujours présent
    }

    /**
     * Test toDTO() avec timestamps récents
     */
    @Test
    @DisplayName("✅ toDTO() - Vérification des timestamps")
    void testToDTOTimestamps() {
        // When
        SaleDetailsDTO result = SaleDetailsMapper.toDTO(sale);

        // Then
        assertNotNull(result.getSaleTimestamp());
        assertEquals(sale.getSaleTimestamp(), result.getSaleTimestamp());

        // Vérifier les timestamps des paiements
        for (int i = 0; i < result.getPayments().size(); i++) {
            assertEquals(payments.get(i).getPaymentDate(),
                    result.getPayments().get(i).getPaymentDate());
        }
    }
}