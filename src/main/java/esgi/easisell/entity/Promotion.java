package esgi.easisell.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 *  ENTITÉ PROMOTION - GESTION DES OFFRES COMMERCIALES
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : Promotion.java
 * @description : Entité pour gérer les promotions des produits
 * @author      : SEDDAR SAMIRA
 * @version     : v1.0.0
 * @date        : 13/07/2025
 * @package     : esgi.easisell.entity
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "PROMOTION")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "promotion_id")
    private UUID promotionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    private Product product;

    @Column(name = "name", nullable = false)
    private String name; // Ex: "Soldes d'été", "Promotion flash"

    @Column(name = "description")
    private String description;

    @Column(name = "promotion_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PromotionType promotionType;

    // Prix original (sauvegardé au moment de la création de la promo)
    @Column(name = "original_price", precision = 19, scale = 2, nullable = false)
    private BigDecimal originalPrice;

    // Nouveau prix en promotion
    @Column(name = "promotion_price", precision = 19, scale = 2)
    private BigDecimal promotionPrice;

    // Pourcentage de réduction (ex: 20.00 pour 20%)
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    // Montant fixe de réduction (ex: 5.00€ de réduction)
    @Column(name = "discount_amount", precision = 19, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "start_date", nullable = false)
    private Timestamp startDate;

    @Column(name = "end_date", nullable = false)
    private Timestamp endDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @ToString.Exclude
    private Client client;

    // ========== ENUMÉRATION DES TYPES DE PROMOTION ==========
    public enum PromotionType {
        PERCENTAGE("Pourcentage"), // -20%
        FIXED_AMOUNT("Montant fixe"), // -5€
        FIXED_PRICE("Prix fixe"); // 2.99€ au lieu de 4.50€

        private final String label;

        PromotionType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    // ========== CALLBACKS ==========
    @PrePersist
    protected void onCreate() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.updatedAt = this.createdAt;
        calculatePromotionPrice();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Timestamp(System.currentTimeMillis());
        calculatePromotionPrice();
    }

    // ========== MÉTHODES MÉTIER ==========

    /**
     * Calcule automatiquement le prix en promotion selon le type
     */
    public void calculatePromotionPrice() {
        if (originalPrice == null) return;

        switch (promotionType) {
            case PERCENTAGE:
                if (discountPercentage != null) {
                    BigDecimal discount = originalPrice
                            .multiply(discountPercentage)
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    this.promotionPrice = originalPrice.subtract(discount);
                }
                break;

            case FIXED_AMOUNT:
                if (discountAmount != null) {
                    this.promotionPrice = originalPrice.subtract(discountAmount);
                    if (this.promotionPrice.compareTo(BigDecimal.ZERO) < 0) {
                        this.promotionPrice = BigDecimal.ZERO;
                    }
                }
                break;

            case FIXED_PRICE:
                // promotionPrice est déjà défini directement
                break;
        }

        // Arrondir à 2 décimales
        if (this.promotionPrice != null) {
            this.promotionPrice = this.promotionPrice.setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * Vérifie si la promotion est actuellement valide
     */
    public boolean isCurrentlyActive() {
        if (!isActive) return false;

        Timestamp now = new Timestamp(System.currentTimeMillis());
        return now.compareTo(startDate) >= 0 && now.compareTo(endDate) <= 0;
    }

    /**
     * Calcule le montant d'économie
     */
    public BigDecimal getSavingsAmount() {
        if (originalPrice == null || promotionPrice == null) {
            return BigDecimal.ZERO;
        }
        return originalPrice.subtract(promotionPrice);
    }

    /**
     * Calcule le pourcentage d'économie réel
     */
    public BigDecimal getSavingsPercentage() {
        if (originalPrice == null || promotionPrice == null ||
                originalPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal savings = getSavingsAmount();
        return savings.multiply(new BigDecimal("100"))
                .divide(originalPrice, 2, RoundingMode.HALF_UP);
    }

    /**
     * Formate l'affichage de la promotion
     */
    public String getFormattedDiscount() {
        switch (promotionType) {
            case PERCENTAGE:
                return String.format("-%s%%", discountPercentage);
            case FIXED_AMOUNT:
                return String.format("-%s€", discountAmount);
            case FIXED_PRICE:
                return String.format("%s€", promotionPrice);
            default:
                return "";
        }
    }

    /**
     * Vérifie si la promotion a expiré
     */
    public boolean isExpired() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return now.compareTo(endDate) > 0;
    }

    /**
     * Vérifie si la promotion va commencer
     */
    public boolean isPending() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return now.compareTo(startDate) < 0;
    }

    /**
     * Retourne le statut de la promotion
     */
    public String getStatus() {
        if (!isActive) return "INACTIVE";
        if (isPending()) return "PENDING";
        if (isExpired()) return "EXPIRED";
        if (isCurrentlyActive()) return "ACTIVE";
        return "UNKNOWN";
    }
}