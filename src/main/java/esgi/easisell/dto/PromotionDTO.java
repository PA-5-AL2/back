package esgi.easisell.dto;

import esgi.easisell.entity.Promotion;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 *  DTO PROMOTION - DONNÉES D'ENTRÉE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
@Data
public class PromotionDTO {
    private String name;
    private String description;
    private UUID productId;
    private UUID clientId;
    private Promotion.PromotionType promotionType;

    // Valeurs selon le type de promotion
    private BigDecimal discountPercentage;  // Pour PERCENTAGE
    private BigDecimal discountAmount;      // Pour FIXED_AMOUNT
    private BigDecimal promotionPrice;      // Pour FIXED_PRICE

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive = true;
}