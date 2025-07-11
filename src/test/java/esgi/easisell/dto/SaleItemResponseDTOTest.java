/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : SaleItemResponseDTOTest.java
 * @description : Tests unitaires pour le DTO SaleItemResponseDTO
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 11/07/2025
 * @package     : esgi.easisell.dto
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeAll;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour SaleItemResponseDTO
 *
 * Teste les méthodes avec logique métier :
 * - generateFormattedQuantity() - Formatage de la quantité
 * - generateFormattedUnitPrice() - Formatage du prix unitaire
 * - generateFormattedTotalPrice() - Formatage du prix total
 * - fromSaleItem() - Construction avec remplissage automatique
 */
class SaleItemResponseDTOTest {

    private SaleItemResponseDTO saleItemDTO;
    private UUID saleItemId;
    private UUID saleId;
    private UUID productId;

    /**
     * Forcer la locale US pour avoir des points comme séparateurs décimaux
     * Évite les problèmes de localisation française (virgules vs points)
     */
    @BeforeAll
    static void setUpLocale() {
        Locale.setDefault(Locale.US);
    }

    @BeforeEach
    void setUp() {
        saleItemDTO = new SaleItemResponseDTO();
        saleItemId = UUID.randomUUID();
        saleId = UUID.randomUUID();
        productId = UUID.randomUUID();
    }

    // ==================== TESTS generateFormattedQuantity() ====================

    /**
     * Test generateFormattedQuantity() pour produit au poids
     */
    @Test
    @DisplayName("✅ generateFormattedQuantity() - Produit au poids avec 3 décimales")
    void testGenerateFormattedQuantityByWeight() {
        // Given
        saleItemDTO.setQuantitySold(new BigDecimal("2.350"));
        saleItemDTO.setUnitLabel("kg");
        saleItemDTO.setIsSoldByWeight(true);

        // When
        String result = saleItemDTO.generateFormattedQuantity();

        // Then
        assertEquals("2.350 kg", result);
    }

    /**
     * Test generateFormattedQuantity() pour produit à la pièce (entier)
     */
    @Test
    @DisplayName("✅ generateFormattedQuantity() - Produit à la pièce, quantité entière")
    void testGenerateFormattedQuantityByPieceWhole() {
        // Given
        saleItemDTO.setQuantitySold(new BigDecimal("5.000"));
        saleItemDTO.setUnitLabel("pièce");
        saleItemDTO.setIsSoldByWeight(false);

        // When
        String result = saleItemDTO.generateFormattedQuantity();

        // Then
        assertEquals("5 pièce", result);
    }

    /**
     * Test generateFormattedQuantity() pour produit à la pièce (décimal)
     */
    @Test
    @DisplayName("✅ generateFormattedQuantity() - Produit à la pièce, quantité décimale")
    void testGenerateFormattedQuantityByPieceDecimal() {
        // Given
        saleItemDTO.setQuantitySold(new BigDecimal("2.50"));
        saleItemDTO.setUnitLabel("portion");
        saleItemDTO.setIsSoldByWeight(false);

        // When
        String result = saleItemDTO.generateFormattedQuantity();

        // Then
        assertEquals("2.50 portion", result);
    }

    /**
     * Test generateFormattedQuantity() avec isSoldByWeight null
     */
    @Test
    @DisplayName("✅ generateFormattedQuantity() - isSoldByWeight null, traité comme pièce")
    void testGenerateFormattedQuantityNullWeight() {
        // Given
        saleItemDTO.setQuantitySold(new BigDecimal("3.000"));
        saleItemDTO.setUnitLabel("unité");
        saleItemDTO.setIsSoldByWeight(null);

        // When
        String result = saleItemDTO.generateFormattedQuantity();

        // Then
        assertEquals("3 unité", result);
    }

    /**
     * Test generateFormattedQuantity() avec quantité null
     */
    @Test
    @DisplayName("✅ generateFormattedQuantity() - Quantité null, retourne valeur par défaut")
    void testGenerateFormattedQuantityNullQuantity() {
        // Given
        saleItemDTO.setQuantitySold(null);
        saleItemDTO.setUnitLabel("kg");
        saleItemDTO.setIsSoldByWeight(true);

        // When
        String result = saleItemDTO.generateFormattedQuantity();

        // Then
        assertEquals("0 unité", result);
    }

    /**
     * Test generateFormattedQuantity() avec unité null
     */
    @Test
    @DisplayName("✅ generateFormattedQuantity() - Unité null, retourne valeur par défaut")
    void testGenerateFormattedQuantityNullUnit() {
        // Given
        saleItemDTO.setQuantitySold(new BigDecimal("2.5"));
        saleItemDTO.setUnitLabel(null);
        saleItemDTO.setIsSoldByWeight(true);

        // When
        String result = saleItemDTO.generateFormattedQuantity();

        // Then
        assertEquals("0 unité", result);
    }

    // ==================== TESTS generateFormattedUnitPrice() ====================

    /**
     * Test generateFormattedUnitPrice() normal
     */
    @Test
    @DisplayName("✅ generateFormattedUnitPrice() - Prix unitaire normal")
    void testGenerateFormattedUnitPrice() {
        // Given
        saleItemDTO.setUnitPrice(new BigDecimal("4.50"));
        saleItemDTO.setUnitLabel("kg");

        // When
        String result = saleItemDTO.generateFormattedUnitPrice();

        // Then
        assertEquals("4.50 €/kg", result);
    }

    /**
     * Test generateFormattedUnitPrice() avec prix null
     */
    @Test
    @DisplayName("✅ generateFormattedUnitPrice() - Prix null, retourne valeur par défaut")
    void testGenerateFormattedUnitPriceNull() {
        // Given
        saleItemDTO.setUnitPrice(null);
        saleItemDTO.setUnitLabel("pièce");

        // When
        String result = saleItemDTO.generateFormattedUnitPrice();

        // Then
        assertEquals("0.00 €/unité", result);
    }

    /**
     * Test generateFormattedUnitPrice() avec unité null
     */
    @Test
    @DisplayName("✅ generateFormattedUnitPrice() - Unité null, retourne valeur par défaut")
    void testGenerateFormattedUnitPriceNullUnit() {
        // Given
        saleItemDTO.setUnitPrice(new BigDecimal("2.30"));
        saleItemDTO.setUnitLabel(null);

        // When
        String result = saleItemDTO.generateFormattedUnitPrice();

        // Then
        assertEquals("0.00 €/unité", result);
    }

    // ==================== TESTS generateFormattedTotalPrice() ====================

    /**
     * Test generateFormattedTotalPrice() normal
     */
    @Test
    @DisplayName("✅ generateFormattedTotalPrice() - Prix total normal")
    void testGenerateFormattedTotalPrice() {
        // Given
        saleItemDTO.setTotalPrice(new BigDecimal("12.45"));

        // When
        String result = saleItemDTO.generateFormattedTotalPrice();

        // Then
        assertEquals("12.45 €", result);
    }

    /**
     * Test generateFormattedTotalPrice() avec prix null
     */
    @Test
    @DisplayName("✅ generateFormattedTotalPrice() - Prix null, retourne 0.00 €")
    void testGenerateFormattedTotalPriceNull() {
        // Given
        saleItemDTO.setTotalPrice(null);

        // When
        String result = saleItemDTO.generateFormattedTotalPrice();

        // Then
        assertEquals("0.00 €", result);
    }

    // ==================== TESTS fromSaleItem() ====================

    /**
     * Test fromSaleItem() avec construction complète pour produit au poids
     */
    @Test
    @DisplayName("✅ fromSaleItem() - Construction complète produit au poids")
    void testFromSaleItemByWeight() {
        // Given
        String productName = "Tomates cerises";
        String productBarcode = "1234567890123";
        BigDecimal quantitySold = new BigDecimal("1.750");
        BigDecimal unitPrice = new BigDecimal("6.80");
        BigDecimal priceAtSale = new BigDecimal("11.90");
        Boolean isSoldByWeight = true;
        String unitLabel = "kg";

        // When
        SaleItemResponseDTO result = SaleItemResponseDTO.fromSaleItem(
                saleItemId, saleId, productId, productName, productBarcode,
                quantitySold, unitPrice, priceAtSale, isSoldByWeight, unitLabel
        );

        // Then
        assertNotNull(result);
        assertEquals(saleItemId, result.getSaleItemId());
        assertEquals(saleId, result.getSaleId());
        assertEquals(productId, result.getProductId());
        assertEquals(productName, result.getProductName());
        assertEquals(productBarcode, result.getProductBarcode());
        assertEquals(quantitySold, result.getQuantitySold());
        assertEquals(unitPrice, result.getUnitPrice());
        assertEquals(priceAtSale, result.getPriceAtSale());
        assertEquals(priceAtSale, result.getTotalPrice()); // totalPrice = priceAtSale
        assertEquals(isSoldByWeight, result.getIsSoldByWeight());
        assertEquals(unitLabel, result.getUnitLabel());

        // Vérifier le formatage automatique
        assertEquals("1.750 kg", result.getFormattedQuantity());
        assertEquals("6.80 €/kg", result.getFormattedUnitPrice());
        assertEquals("11.90 €", result.getFormattedTotalPrice());
    }

    /**
     * Test fromSaleItem() avec construction complète pour produit à la pièce
     */
    @Test
    @DisplayName("✅ fromSaleItem() - Construction complète produit à la pièce")
    void testFromSaleItemByPiece() {
        // Given
        String productName = "Bouteilles d'eau";
        String productBarcode = "9876543210987";
        BigDecimal quantitySold = new BigDecimal("6.000");
        BigDecimal unitPrice = new BigDecimal("1.20");
        BigDecimal priceAtSale = new BigDecimal("7.20");
        Boolean isSoldByWeight = false;
        String unitLabel = "pièce";

        // When
        SaleItemResponseDTO result = SaleItemResponseDTO.fromSaleItem(
                saleItemId, saleId, productId, productName, productBarcode,
                quantitySold, unitPrice, priceAtSale, isSoldByWeight, unitLabel
        );

        // Then
        assertEquals("6 pièce", result.getFormattedQuantity());
        assertEquals("1.20 €/pièce", result.getFormattedUnitPrice());
        assertEquals("7.20 €", result.getFormattedTotalPrice());
    }

    // ==================== TESTS EDGE CASES ====================

    /**
     * Test avec des valeurs décimales très précises
     */
    @Test
    @DisplayName("✅ generateFormattedQuantity() - Valeurs décimales précises")
    void testGenerateFormattedQuantityPreciseDecimals() {
        // Given
        saleItemDTO.setQuantitySold(new BigDecimal("0.001"));
        saleItemDTO.setUnitLabel("kg");
        saleItemDTO.setIsSoldByWeight(true);

        // When
        String result = saleItemDTO.generateFormattedQuantity();

        // Then
        assertEquals("0.001 kg", result);
    }

    /**
     * Test avec quantité ayant des zéros non significatifs
     */
    @Test
    @DisplayName("✅ generateFormattedQuantity() - Suppression zéros non significatifs")
    void testGenerateFormattedQuantityTrailingZeros() {
        // Given
        saleItemDTO.setQuantitySold(new BigDecimal("3.0000"));
        saleItemDTO.setUnitLabel("L");
        saleItemDTO.setIsSoldByWeight(false);

        // When
        String result = saleItemDTO.generateFormattedQuantity();

        // Then
        assertEquals("3 L", result);
    }

    /**
     * Test avec unités spéciales
     */
    @Test
    @DisplayName("✅ fromSaleItem() - Unités spéciales")
    void testFromSaleItemSpecialUnits() {
        // Given
        String unitLabel = "m²";
        BigDecimal quantitySold = new BigDecimal("12.50");

        // When
        SaleItemResponseDTO result = SaleItemResponseDTO.fromSaleItem(
                saleItemId, saleId, productId, "Tissu", "1111111111",
                quantitySold, new BigDecimal("15.00"), new BigDecimal("187.50"),
                false, unitLabel
        );

        // Then
        assertEquals("12.50 m²", result.getFormattedQuantity());
        assertEquals("15.00 €/m²", result.getFormattedUnitPrice());
    }

    /**
     * Test de cohérence entre les méthodes de formatage
     */
    @Test
    @DisplayName("✅ Cohérence - Formatage manuel vs automatique")
    void testFormattingConsistency() {
        // Given
        saleItemDTO.setQuantitySold(new BigDecimal("2.500"));
        saleItemDTO.setUnitPrice(new BigDecimal("8.75"));
        saleItemDTO.setTotalPrice(new BigDecimal("21.88"));
        saleItemDTO.setUnitLabel("kg");
        saleItemDTO.setIsSoldByWeight(true);

        // When - Formatage manuel
        String manualQuantity = saleItemDTO.generateFormattedQuantity();
        String manualUnitPrice = saleItemDTO.generateFormattedUnitPrice();
        String manualTotalPrice = saleItemDTO.generateFormattedTotalPrice();

        // When - Formatage automatique via fromSaleItem
        SaleItemResponseDTO autoFormatted = SaleItemResponseDTO.fromSaleItem(
                saleItemId, saleId, productId, "Test Product", "1234567890",
                new BigDecimal("2.500"), new BigDecimal("8.75"), new BigDecimal("21.88"),
                true, "kg"
        );

        // Then - Les deux méthodes doivent donner le même résultat
        assertEquals(manualQuantity, autoFormatted.getFormattedQuantity());
        assertEquals(manualUnitPrice, autoFormatted.getFormattedUnitPrice());
        assertEquals(manualTotalPrice, autoFormatted.getFormattedTotalPrice());
    }
}