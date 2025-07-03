/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : PromotionTest.java
 * @description : Tests unitaires pour l'entité Promotion
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
 * The type Promotion test.
 */
class PromotionTest {

    private Promotion promotion;
    private Product product;
    private Client client;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        promotion = new Promotion();

        client = new Client();
        client.setUserId(UUID.randomUUID());
        client.setName("Test Store");

        product = new Product();
        product.setProductId(UUID.randomUUID());
        product.setName("Test Product");
        product.setClient(client);
    }

    /**
     * Test promotion getters setters.
     */
    @Test
    @DisplayName("✅ Getters/Setters promotion")
    void testPromotionGettersSetters() {
        UUID promotionId = UUID.randomUUID();
        String promotionCode = "PROMO2025";
        String description = "Promotion de test";
        String discountType = "PERCENT";
        BigDecimal discountValue = new BigDecimal("20.00");
        Timestamp startDate = Timestamp.valueOf(LocalDateTime.now());
        Timestamp endDate = Timestamp.valueOf(LocalDateTime.now().plusDays(7));

        promotion.setPromotionId(promotionId);
        promotion.setPromotionCode(promotionCode);
        promotion.setDescription(description);
        promotion.setDiscountType(discountType);
        promotion.setDiscountValue(discountValue);
        promotion.setStartDate(startDate);
        promotion.setEndDate(endDate);
        promotion.setProduct(product);
        promotion.setClient(client);

        assertEquals(promotionId, promotion.getPromotionId());
        assertEquals(promotionCode, promotion.getPromotionCode());
        assertEquals(description, promotion.getDescription());
        assertEquals(discountType, promotion.getDiscountType());
        assertEquals(discountValue, promotion.getDiscountValue());
        assertEquals(startDate, promotion.getStartDate());
        assertEquals(endDate, promotion.getEndDate());
        assertEquals(product, promotion.getProduct());
        assertEquals(client, promotion.getClient());
    }

    /**
     * Test required relations.
     */
    @Test
    @DisplayName("✅ Relations obligatoires")
    void testRequiredRelations() {
        promotion.setProduct(product);
        promotion.setClient(client);

        assertNotNull(promotion.getProduct());
        assertNotNull(promotion.getClient());
    }

    /**
     * Test date validation.
     */
    @Test
    @DisplayName("✅ Validation métier - dates cohérentes")
    void testDateValidation() {
        Timestamp startDate = Timestamp.valueOf(LocalDateTime.now());
        Timestamp endDate = Timestamp.valueOf(LocalDateTime.now().plusDays(7));

        promotion.setStartDate(startDate);
        promotion.setEndDate(endDate);

        assertTrue(promotion.getEndDate().after(promotion.getStartDate()));
    }

    /**
     * Test unique promotion code.
     */
    @Test
    @DisplayName("✅ Validation métier - code unique")
    void testUniquePromotionCode() {
        promotion.setPromotionCode("UNIQUE2025");

        assertNotNull(promotion.getPromotionCode());
        assertFalse(promotion.getPromotionCode().isEmpty());
    }

    /**
     * Test discount types.
     */
    @Test
    @DisplayName("✅ Types de réduction valides")
    void testDiscountTypes() {
        String[] validTypes = {"PERCENT", "FIXED"};

        for (String type : validTypes) {
            promotion.setDiscountType(type);
            assertEquals(type, promotion.getDiscountType());
        }
    }
}