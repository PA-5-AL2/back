/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : ReceiptItemDTOTest.java
 * @description : Tests unitaires pour le DTO ReceiptItemDTO
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
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour ReceiptItemDTO
 *
 * Teste les méthodes avec logique métier :
 * - fromSaleItem() - Construction avec formatage automatique
 * - generateReceiptLine() - Génération de ligne de ticket
 */
class ReceiptItemDTOTest {

    private ReceiptItemDTO receiptItemDTO;

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
        receiptItemDTO = new ReceiptItemDTO();
    }

    // ==================== TESTS fromSaleItem() ====================

    /**
     * Test fromSaleItem() pour un produit vendu au poids
     */
    @Test
    @DisplayName("✅ fromSaleItem() - Produit au poids avec formatage correct")
    void testFromSaleItemByWeight() {
        // Given
        String productName = "Tomates cerises";
        String barcode = "1234567890123";
        BigDecimal quantitySold = new BigDecimal("2.350");
        BigDecimal unitPrice = new BigDecimal("4.50");
        BigDecimal total = new BigDecimal("10.58");
        Boolean isSoldByWeight = true;
        String unitLabel = "kg";

        // When
        ReceiptItemDTO result = ReceiptItemDTO.fromSaleItem(
                productName, barcode, quantitySold, unitPrice, total, isSoldByWeight, unitLabel
        );

        // Then
        assertNotNull(result);
        assertEquals(productName, result.getProductName());
        assertEquals(barcode, result.getBarcode());
        assertEquals(2, result.getQuantity()); // Conversion int pour legacy
        assertEquals(unitPrice, result.getUnitPrice());
        assertEquals(total, result.getTotal());
        assertEquals(isSoldByWeight, result.getIsSoldByWeight());
        assertEquals(unitLabel, result.getUnitLabel());

        // Vérifier le formatage automatique
        assertEquals("2.350 kg", result.getFormattedQuantity());
        assertEquals("4.50 €/kg", result.getFormattedUnitPrice());
    }

    /**
     * Test fromSaleItem() pour un produit vendu à la pièce
     */
    @Test
    @DisplayName("✅ fromSaleItem() - Produit à la pièce avec formatage correct")
    void testFromSaleItemByPiece() {
        // Given
        String productName = "Bouteilles d'eau";
        String barcode = "9876543210987";
        BigDecimal quantitySold = new BigDecimal("5.000");
        BigDecimal unitPrice = new BigDecimal("1.20");
        BigDecimal total = new BigDecimal("6.00");
        Boolean isSoldByWeight = false;
        String unitLabel = "pièce";

        // When
        ReceiptItemDTO result = ReceiptItemDTO.fromSaleItem(
                productName, barcode, quantitySold, unitPrice, total, isSoldByWeight, unitLabel
        );

        // Then
        assertNotNull(result);
        assertEquals(productName, result.getProductName());
        assertEquals(barcode, result.getBarcode());
        assertEquals(5, result.getQuantity());
        assertEquals(unitPrice, result.getUnitPrice());
        assertEquals(total, result.getTotal());
        assertEquals(isSoldByWeight, result.getIsSoldByWeight());
        assertEquals(unitLabel, result.getUnitLabel());

        // Vérifier le formatage pour les pièces
        assertEquals("5 pièce", result.getFormattedQuantity());
        assertEquals("1.20 €/pièce", result.getFormattedUnitPrice());
    }

    /**
     * Test fromSaleItem() avec isSoldByWeight null
     */
    @Test
    @DisplayName("✅ fromSaleItem() - isSoldByWeight null, utilise formatage pièces")
    void testFromSaleItemWithNullWeight() {
        // Given
        String productName = "Produit test";
        String barcode = "1111111111111";
        BigDecimal quantitySold = new BigDecimal("3.000");
        BigDecimal unitPrice = new BigDecimal("2.50");
        BigDecimal total = new BigDecimal("7.50");
        Boolean isSoldByWeight = null;
        String unitLabel = "unité";

        // When
        ReceiptItemDTO result = ReceiptItemDTO.fromSaleItem(
                productName, barcode, quantitySold, unitPrice, total, isSoldByWeight, unitLabel
        );

        // Then
        assertEquals("3 unité", result.getFormattedQuantity());
        assertEquals("2.50 €/unité", result.getFormattedUnitPrice());
    }

    // ==================== TESTS generateReceiptLine() ====================

    /**
     * Test generateReceiptLine() avec champs formatés disponibles
     */
    @Test
    @DisplayName("✅ generateReceiptLine() - Avec formatage moderne")
    void testGenerateReceiptLineWithFormatting() {
        // Given
        receiptItemDTO.setProductName("Bananes");
        receiptItemDTO.setFormattedQuantity("1.250 kg");
        receiptItemDTO.setFormattedUnitPrice("2.40 €/kg");
        receiptItemDTO.setTotal(new BigDecimal("3.00"));

        // When
        String result = receiptItemDTO.generateReceiptLine();

        // Then
        assertEquals("Bananes: 1.250 kg × 2.40 €/kg = 3.00 €", result);
    }

    /**
     * Test generateReceiptLine() avec fallback legacy
     */
    @Test
    @DisplayName("✅ generateReceiptLine() - Fallback mode legacy")
    void testGenerateReceiptLineLegacyFallback() {
        // Given - Pas de champs formatés
        receiptItemDTO.setProductName("Pain");
        receiptItemDTO.setQuantity(2);
        receiptItemDTO.setUnitPrice(new BigDecimal("1.50"));
        receiptItemDTO.setTotal(new BigDecimal("3.00"));
        receiptItemDTO.setFormattedQuantity(null);
        receiptItemDTO.setFormattedUnitPrice(null);

        // When
        String result = receiptItemDTO.generateReceiptLine();

        // Then
        assertEquals("Pain: 2 × 1.50 € = 3.00 €", result);
    }

    /**
     * Test generateReceiptLine() avec formattedQuantity mais pas formattedUnitPrice
     */
    @Test
    @DisplayName("✅ generateReceiptLine() - Formatage partiel, utilise fallback")
    void testGenerateReceiptLinePartialFormatting() {
        // Given
        receiptItemDTO.setProductName("Pommes");
        receiptItemDTO.setQuantity(3);
        receiptItemDTO.setUnitPrice(new BigDecimal("2.20"));
        receiptItemDTO.setTotal(new BigDecimal("6.60"));
        receiptItemDTO.setFormattedQuantity("3.000 kg");
        receiptItemDTO.setFormattedUnitPrice(null); // Manquant

        // When
        String result = receiptItemDTO.generateReceiptLine();

        // Then
        // Doit utiliser le fallback car formattedUnitPrice est null
        assertEquals("Pommes: 3 × 2.20 € = 6.60 €", result);
    }

    // ==================== TESTS EDGE CASES ====================

    /**
     * Test avec des valeurs décimales complexes
     */
    @Test
    @DisplayName("✅ fromSaleItem() - Valeurs décimales complexes")
    void testFromSaleItemComplexDecimals() {
        // Given
        BigDecimal quantitySold = new BigDecimal("0.123");
        BigDecimal unitPrice = new BigDecimal("12.45"); // Changé pour éviter l'arrondi
        BigDecimal total = new BigDecimal("1.53");

        // When
        ReceiptItemDTO result = ReceiptItemDTO.fromSaleItem(
                "Produit précis", "1234567890", quantitySold, unitPrice, total, true, "kg"
        );

        // Then
        assertEquals("0.123 kg", result.getFormattedQuantity());
        assertEquals("12.45 €/kg", result.getFormattedUnitPrice());
    }

    /**
     * Test avec quantité entière vendue au poids
     */
    @Test
    @DisplayName("✅ fromSaleItem() - Quantité entière au poids")
    void testFromSaleItemWholeNumberByWeight() {
        // Given
        BigDecimal quantitySold = new BigDecimal("2.000");

        // When
        ReceiptItemDTO result = ReceiptItemDTO.fromSaleItem(
                "Produit entier", "1234567890", quantitySold, new BigDecimal("5.00"),
                new BigDecimal("10.00"), true, "kg"
        );

        // Then
        assertEquals("2.000 kg", result.getFormattedQuantity());
    }

    /**
     * Test avec des noms de produits spéciaux
     */
    @Test
    @DisplayName("✅ generateReceiptLine() - Caractères spéciaux dans nom produit")
    void testGenerateReceiptLineSpecialCharacters() {
        // Given
        receiptItemDTO.setProductName("Café moulu 100% arabica");
        receiptItemDTO.setFormattedQuantity("1 paquet");
        receiptItemDTO.setFormattedUnitPrice("8.50 €/paquet");
        receiptItemDTO.setTotal(new BigDecimal("8.50"));

        // When
        String result = receiptItemDTO.generateReceiptLine();

        // Then
        assertEquals("Café moulu 100% arabica: 1 paquet × 8.50 €/paquet = 8.50 €", result);
    }
}