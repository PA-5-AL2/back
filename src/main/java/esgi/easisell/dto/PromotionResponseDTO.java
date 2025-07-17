package esgi.easisell.dto;

import esgi.easisell.entity.Promotion;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 *  DTO PROMOTION - DONNÉES DE SORTIE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
@Data
public class PromotionResponseDTO {
    private UUID promotionId;
    private String name;
    private String description;
    private Promotion.PromotionType promotionType;

    // Informations produit (aplaties)
    private UUID productId;
    private String productName;
    private String productUnitLabel;

    // Informations client (aplaties)
    private UUID clientId;
    private String clientUsername;

    // Prix et réductions
    private BigDecimal originalPrice;
    private BigDecimal promotionPrice;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;

    // Dates
    private Timestamp startDate;
    private Timestamp endDate;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Statut
    private Boolean isActive;
    private String status; // ACTIVE, PENDING, EXPIRED, INACTIVE

    // Calculs
    private BigDecimal savingsAmount;
    private BigDecimal savingsPercentage;
    private String formattedDiscount;
    private String formattedOriginalPrice;
    private String formattedPromotionPrice;

    // Indicateurs
    private boolean isCurrentlyActive;
    private boolean isExpired;
    private boolean isPending;
    private long daysUntilStart;
    private long daysUntilEnd;

    public PromotionResponseDTO() {}

    public PromotionResponseDTO(Promotion promotion) {
        this.promotionId = promotion.getPromotionId();
        this.name = promotion.getName();
        this.description = promotion.getDescription();
        this.promotionType = promotion.getPromotionType();

        // Informations produit
        if (promotion.getProduct() != null) {
            this.productId = promotion.getProduct().getProductId();
            this.productName = promotion.getProduct().getName();
            this.productUnitLabel = promotion.getProduct().getUnitLabel();
        }

        // Informations client
        if (promotion.getClient() != null) {
            this.clientId = promotion.getClient().getUserId();
            this.clientUsername = promotion.getClient().getUsername();
        }

        // Prix et réductions
        this.originalPrice = promotion.getOriginalPrice();
        this.promotionPrice = promotion.getPromotionPrice();
        this.discountPercentage = promotion.getDiscountPercentage();
        this.discountAmount = promotion.getDiscountAmount();

        // Dates
        this.startDate = promotion.getStartDate();
        this.endDate = promotion.getEndDate();
        this.createdAt = promotion.getCreatedAt();
        this.updatedAt = promotion.getUpdatedAt();

        // Statut
        this.isActive = promotion.getIsActive();
        this.status = promotion.getStatus();

        // Calculs
        this.savingsAmount = promotion.getSavingsAmount();
        this.savingsPercentage = promotion.getSavingsPercentage();
        this.formattedDiscount = promotion.getFormattedDiscount();

        // Formatage des prix
        if (originalPrice != null && productUnitLabel != null) {
            this.formattedOriginalPrice = String.format("%.2f €/%s", originalPrice, productUnitLabel);
        }
        if (promotionPrice != null && productUnitLabel != null) {
            this.formattedPromotionPrice = String.format("%.2f €/%s", promotionPrice, productUnitLabel);
        }

        // Indicateurs
        this.isCurrentlyActive = promotion.isCurrentlyActive();
        this.isExpired = promotion.isExpired();
        this.isPending = promotion.isPending();

        // Calcul des jours
        LocalDateTime now = LocalDateTime.now();
        if (startDate != null) {
            LocalDateTime start = startDate.toLocalDateTime();
            this.daysUntilStart = java.time.temporal.ChronoUnit.DAYS.between(now, start);
        }
        if (endDate != null) {
            LocalDateTime end = endDate.toLocalDateTime();
            this.daysUntilEnd = java.time.temporal.ChronoUnit.DAYS.between(now, end);
        }
    }
}