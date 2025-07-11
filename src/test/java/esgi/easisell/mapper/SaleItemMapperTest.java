/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : SaleItemMapperTest.java
 * @description : Tests unitaires pour le mapper SaleItemMapper
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 11/07/2025
 * @package     : esgi.easisell.mapper
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.mapper;

import esgi.easisell.dto.SaleItemResponseDTO;
import esgi.easisell.entity.*;
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
 * Tests unitaires pour SaleItemMapper
 *
 * Teste les méthodes :
 * - toResponseDTO() - Mapping simple avec formatage automatique
 * - toResponseDTODetailed() - Mapping détaillé avec formatage explicite
 */
class SaleItemMapperTest {

    private SaleItem saleItem;
    private Sale sale;
    private Product product;
    private Client client;
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
        client.setUsername("teststore@easisell.com");
        client.setName("Test Store");

        // Créer la catégorie
        category = new Category();
        category.setCategoryId(UUID.randomUUID());
        category.setName("Fruits");
        category.setClient(client);

        // Créer le produit
        product = new Product();
        product.setProductId(UUID.randomUUID());
        product.setName("Pommes Golden");
        product.setBarcode("1234567890123");
        product.setUnitPrice(new BigDecimal("3.50"));
        product.setCategory(category);
        product.setClient(client);
        product.setIsSoldByWeight(true);
        product.setUnitLabel("kg");

        // Créer la vente
        sale = new Sale();
        sale.setSaleId(UUID.randomUUID());
        sale.setClient(client);
        sale.setSaleTimestamp(Timestamp.valueOf(LocalDateTime.now()));

        // Créer l'article de vente
        saleItem = new SaleItem();
        saleItem.setSaleItemId(UUID.randomUUID());
        saleItem.setSale(sale);
        saleItem.setProduct(product);
        saleItem.setQuantitySold(new BigDecimal("2.500"));
        saleItem.setPriceAtSale(new BigDecimal("8.75"));
    }

    // ==================== TESTS toResponseDTO() ====================

    /**
     * Test toResponseDTO() avec produit au poids
     */
    @Test
    @DisplayName("✅ toResponseDTO() - Produit au poids avec formatage automatique")
    void testToResponseDTOByWeight() {
        // When
        SaleItemResponseDTO result = SaleItemMapper.toResponseDTO(saleItem);

        // Then
        assertNotNull(result);
        assertEquals(saleItem.getSaleItemId(), result.getSaleItemId());
        assertEquals(sale.getSaleId(), result.getSaleId());
        assertEquals(product.getProductId(), result.getProductId());
        assertEquals(product.getName(), result.getProductName());
        assertEquals(product.getBarcode(), result.getProductBarcode());
        assertEquals(saleItem.getQuantitySold(), result.getQuantitySold());
        assertEquals(product.getUnitPrice(), result.getUnitPrice());
        assertEquals(saleItem.getPriceAtSale(), result.getPriceAtSale());
        assertEquals(product.getIsSoldByWeight(), result.getIsSoldByWeight());
        assertEquals(product.getUnitLabel(), result.getUnitLabel());

        // Vérifier le formatage automatique via fromSaleItem
        assertEquals("2.500 kg", result.getFormattedQuantity());
        assertEquals("3.50 €/kg", result.getFormattedUnitPrice());
        assertEquals("8.75 €", result.getFormattedTotalPrice());
    }

    /**
     * Test toResponseDTO() avec produit à la pièce
     */
    @Test
    @DisplayName("✅ toResponseDTO() - Produit à la pièce")
    void testToResponseDTOByPiece() {
        // Given - Modifier le produit pour la vente à la pièce
        product.setIsSoldByWeight(false);
        product.setUnitLabel("pièce");
        product.setUnitPrice(new BigDecimal("1.50"));
        saleItem.setQuantitySold(new BigDecimal("6.000"));
        saleItem.setPriceAtSale(new BigDecimal("9.00"));

        // When
        SaleItemResponseDTO result = SaleItemMapper.toResponseDTO(saleItem);

        // Then
        assertNotNull(result);
        assertEquals(false, result.getIsSoldByWeight());
        assertEquals("pièce", result.getUnitLabel());
        assertEquals("6 pièce", result.getFormattedQuantity());
        assertEquals("1.50 €/pièce", result.getFormattedUnitPrice());
        assertEquals("9.00 €", result.getFormattedTotalPrice());
    }

    /**
     * Test toResponseDTO() avec saleItem null
     */
    @Test
    @DisplayName("✅ toResponseDTO() - Gestion de null")
    void testToResponseDTOWithNull() {
        // When
        SaleItemResponseDTO result = SaleItemMapper.toResponseDTO(null);

        // Then
        assertNull(result);
    }

    // ==================== TESTS toResponseDTODetailed() ====================

    /**
     * Test toResponseDTODetailed() avec formatage explicite
     */
    @Test
    @DisplayName("✅ toResponseDTODetailed() - Formatage explicite et détaillé")
    void testToResponseDTODetailed() {
        // When
        SaleItemResponseDTO result = SaleItemMapper.toResponseDTODetailed(saleItem);

        // Then
        assertNotNull(result);

        // Vérifier tous les champs basiques
        assertEquals(saleItem.getSaleItemId(), result.getSaleItemId());
        assertEquals(sale.getSaleId(), result.getSaleId());
        assertEquals(product.getProductId(), result.getProductId());
        assertEquals(product.getName(), result.getProductName());
        assertEquals(product.getBarcode(), result.getProductBarcode());
        assertEquals(saleItem.getQuantitySold(), result.getQuantitySold());
        assertEquals(product.getUnitPrice(), result.getUnitPrice());
        assertEquals(saleItem.getPriceAtSale(), result.getPriceAtSale());
        assertEquals(saleItem.getPriceAtSale(), result.getTotalPrice()); // totalPrice = priceAtSale

        // Vérifier les propriétés d'unités
        assertEquals(product.getIsSoldByWeight(), result.getIsSoldByWeight());
        assertEquals(product.getUnitLabel(), result.getUnitLabel());

        // Vérifier le formatage explicite
        assertNotNull(result.getFormattedQuantity());
        assertNotNull(result.getFormattedUnitPrice());
        assertNotNull(result.getFormattedTotalPrice());
        assertEquals("2.500 kg", result.getFormattedQuantity());
        assertEquals("3.50 €/kg", result.getFormattedUnitPrice());
        assertEquals("8.75 €", result.getFormattedTotalPrice());
    }

    /**
     * Test toResponseDTODetailed() avec quantités décimales complexes
     */
    @Test
    @DisplayName("✅ toResponseDTODetailed() - Quantités décimales complexes")
    void testToResponseDTODetailedComplexDecimals() {
        // Given
        saleItem.setQuantitySold(new BigDecimal("1.234"));
        saleItem.setPriceAtSale(new BigDecimal("4.32"));

        // When
        SaleItemResponseDTO result = SaleItemMapper.toResponseDTODetailed(saleItem);

        // Then
        assertEquals("1.234 kg", result.getFormattedQuantity());
        assertEquals("3.50 €/kg", result.getFormattedUnitPrice());
        assertEquals("4.32 €", result.getFormattedTotalPrice());
    }

    // ==================== TESTS EDGE CASES ====================

    /**
     * Test avec produit sans unité spécifiée
     */
    @Test
    @DisplayName("✅ toResponseDTO() - Produit sans unité spécifiée")
    void testToResponseDTOWithoutUnit() {
        // Given
        product.setIsSoldByWeight(null);
        product.setUnitLabel(null);

        // When
        SaleItemResponseDTO result = SaleItemMapper.toResponseDTO(saleItem);

        // Then
        assertNotNull(result);
        assertNull(result.getIsSoldByWeight());
        assertNull(result.getUnitLabel());
        // Le formatage devrait gérer les valeurs null
        assertNotNull(result.getFormattedQuantity());
        assertNotNull(result.getFormattedUnitPrice());
    }

    /**
     * Test avec prix à zéro
     */
    @Test
    @DisplayName("✅ toResponseDTODetailed() - Prix à zéro")
    void testToResponseDTODetailedZeroPrice() {
        // Given
        product.setUnitPrice(BigDecimal.ZERO);
        saleItem.setPriceAtSale(BigDecimal.ZERO);

        // When
        SaleItemResponseDTO result = SaleItemMapper.toResponseDTODetailed(saleItem);

        // Then
        assertEquals("0.00 €/kg", result.getFormattedUnitPrice());
        assertEquals("0.00 €", result.getFormattedTotalPrice());
    }

    /**
     * Test avec quantité fractionnaire pour pièce
     */
    @Test
    @DisplayName("✅ toResponseDTO() - Quantité fractionnaire pour produit à la pièce")
    void testToResponseDTOFractionalPiece() {
        // Given
        product.setIsSoldByWeight(false);
        product.setUnitLabel("portion");
        saleItem.setQuantitySold(new BigDecimal("2.50"));

        // When
        SaleItemResponseDTO result = SaleItemMapper.toResponseDTO(saleItem);

        // Then
        assertEquals("2.50 portion", result.getFormattedQuantity());
    }

    /**
     * Test de cohérence entre les deux méthodes
     */
    @Test
    @DisplayName("✅ Cohérence - toResponseDTO() vs toResponseDTODetailed()")
    void testConsistencyBetweenMethods() {
        // When
        SaleItemResponseDTO simple = SaleItemMapper.toResponseDTO(saleItem);
        SaleItemResponseDTO detailed = SaleItemMapper.toResponseDTODetailed(saleItem);

        // Then - Les champs de base doivent être identiques
        assertEquals(simple.getSaleItemId(), detailed.getSaleItemId());
        assertEquals(simple.getSaleId(), detailed.getSaleId());
        assertEquals(simple.getProductId(), detailed.getProductId());
        assertEquals(simple.getProductName(), detailed.getProductName());
        assertEquals(simple.getProductBarcode(), detailed.getProductBarcode());
        assertEquals(simple.getQuantitySold(), detailed.getQuantitySold());
        assertEquals(simple.getUnitPrice(), detailed.getUnitPrice());
        assertEquals(simple.getPriceAtSale(), detailed.getPriceAtSale());
        assertEquals(simple.getIsSoldByWeight(), detailed.getIsSoldByWeight());
        assertEquals(simple.getUnitLabel(), detailed.getUnitLabel());

        // Le formatage peut différer selon l'implémentation mais doit être cohérent
        assertEquals(simple.getFormattedQuantity(), detailed.getFormattedQuantity());
        assertEquals(simple.getFormattedUnitPrice(), detailed.getFormattedUnitPrice());
        assertEquals(simple.getFormattedTotalPrice(), detailed.getFormattedTotalPrice());
    }

    /**
     * Test avec unités spéciales
     */
    @Test
    @DisplayName("✅ toResponseDTODetailed() - Unités spéciales (litres, mètres)")
    void testToResponseDTODetailedSpecialUnits() {
        // Given
        product.setUnitLabel("L");
        product.setIsSoldByWeight(false);
        saleItem.setQuantitySold(new BigDecimal("1.500"));

        // When
        SaleItemResponseDTO result = SaleItemMapper.toResponseDTODetailed(saleItem);

        // Then
        assertEquals("1.50 L", result.getFormattedQuantity());
        assertEquals("3.50 €/L", result.getFormattedUnitPrice());
    }
}