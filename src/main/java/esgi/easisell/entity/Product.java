package esgi.easisell.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.sql.Timestamp;

/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : Product.java
 * @description : Entité produit commercialisé (avec support promotions)
 * @author      : SEDDAR SAMIRA
 * @version     : v2.0.0
 * @date        : 2/07/2025
 * @package     : esgi.easisell.entity
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "PRODUCT",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"client_id", "barcode"})
        })
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "product_id")
    private UUID productId;

    @Column(nullable = false)
    private String name;

    @Version
    private Long version;


    @Lob
    private String description;

    @Column(name = "barcode")
    private String barcode;

    private String brand;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @ToString.Exclude
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @ToString.Exclude
    private Client client;

    @Column(name = "purchase_price", precision = 19, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;

    @Column(name = "reorder_threshold")
    private Integer reorderThreshold;

    @Column(name = "purchase_date")
    private Timestamp purchaseDate;

    @Column(name = "expiration_date")
    private Timestamp expirationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    @ToString.Exclude
    private Supplier supplier;


    @Column(name = "last_modified")
    private Timestamp lastModified;

    /**
     * Type de vente : true = au poids (kg), false = à la pièce
     */
    @Column(nullable = false)
    private Boolean isSoldByWeight = false;

    /**
     * Unité d'affichage : "kg", "pièce", "L", "paquet"
     */
    @Column(length = 10, nullable = false)
    private String unitLabel = "pièce";

    // ========== RELATION AVEC LES PROMOTIONS ==========
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Promotion> promotions = new ArrayList<>();

    // ========== CALLBACKS ==========
    @PreUpdate
    @PrePersist
    protected void onUpdate() {
        this.lastModified = new Timestamp(System.currentTimeMillis());
    }

    // ========== MÉTHODES MÉTIER ==========

    /**
     * Calcule le prix total pour une quantité (avec 2 décimales)
     */
    public BigDecimal calculateTotalPrice(BigDecimal quantity) {
        if (quantity == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal currentPrice = getCurrentPrice();
        return currentPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Retourne le prix actuel (promotion ou prix normal)
     */
    public BigDecimal getCurrentPrice() {
        Promotion activePromotion = getActivePromotion();
        return activePromotion != null ? activePromotion.getPromotionPrice() : unitPrice;
    }

    /**
     * Formate le prix avec l'unité
     */
    public String getFormattedPrice() {
        BigDecimal currentPrice = getCurrentPrice();
        return String.format("%.2f €/%s", currentPrice, unitLabel);
    }

    /**
     * Formate le prix avec indication de promotion
     */
    public String getFormattedPriceWithPromotion() {
        Promotion activePromotion = getActivePromotion();
        if (activePromotion != null) {
            return String.format("%.2f €/%s (au lieu de %.2f €)",
                    activePromotion.getPromotionPrice(),
                    unitLabel,
                    unitPrice);
        }
        return getFormattedPrice();
    }

    /**
     * Vérifie si le produit est actuellement en promotion
     */
    public boolean isOnPromotion() {
        return getActivePromotion() != null;
    }

    /**
     * Retourne la promotion active (s'il y en a une)
     */
    public Promotion getActivePromotion() {
        if (promotions == null || promotions.isEmpty()) {
            return null;
        }

        return promotions.stream()
                .filter(Promotion::isCurrentlyActive)
                .findFirst()
                .orElse(null);
    }

    /**
     * Retourne toutes les promotions futures
     */
    public List<Promotion> getFuturePromotions() {
        if (promotions == null || promotions.isEmpty()) {
            return new ArrayList<>();
        }

        return promotions.stream()
                .filter(Promotion::isPending)
                .toList();
    }

    /**
     * Retourne toutes les promotions expirées
     */
    public List<Promotion> getExpiredPromotions() {
        if (promotions == null || promotions.isEmpty()) {
            return new ArrayList<>();
        }

        return promotions.stream()
                .filter(Promotion::isExpired)
                .toList();
    }

    /**
     * Ajoute une nouvelle promotion
     */
    public void addPromotion(Promotion promotion) {
        if (promotions == null) {
            promotions = new ArrayList<>();
        }
        promotions.add(promotion);
        promotion.setProduct(this);
    }

    /**
     * Supprime une promotion
     */
    public void removePromotion(Promotion promotion) {
        if (promotions != null) {
            promotions.remove(promotion);
            promotion.setProduct(null);
        }
    }

    /**
     * Désactive toutes les promotions actives
     */
    public void deactivateAllPromotions() {
        if (promotions != null) {
            promotions.forEach(promotion -> promotion.setIsActive(false));
        }
    }

    /**
     * Calcule le montant d'économie si en promotion
     */
    public BigDecimal getSavingsAmount() {
        Promotion activePromotion = getActivePromotion();
        return activePromotion != null ? activePromotion.getSavingsAmount() : BigDecimal.ZERO;
    }

    /**
     * Calcule le pourcentage d'économie si en promotion
     */
    public BigDecimal getSavingsPercentage() {
        Promotion activePromotion = getActivePromotion();
        return activePromotion != null ? activePromotion.getSavingsPercentage() : BigDecimal.ZERO;
    }

    /**
     * Retourne les informations de promotion pour l'affichage
     */
    public Map<String, Object> getPromotionInfo() {
        Map<String, Object> info = new HashMap<>();
        Promotion activePromotion = getActivePromotion();

        if (activePromotion != null) {
            info.put("isOnPromotion", true);
            info.put("promotionName", activePromotion.getName());
            info.put("originalPrice", unitPrice);
            info.put("promotionPrice", activePromotion.getPromotionPrice());
            info.put("savings", activePromotion.getSavingsAmount());
            info.put("savingsPercentage", activePromotion.getSavingsPercentage());
            info.put("promotionType", activePromotion.getPromotionType());
            info.put("endDate", activePromotion.getEndDate());
            info.put("formattedDiscount", activePromotion.getFormattedDiscount());
        } else {
            info.put("isOnPromotion", false);
            info.put("currentPrice", unitPrice);
        }

        return info;
    }

    // ========== GETTERS/SETTERS SPÉCIAUX ==========

    public Boolean getIsSoldByWeight() {
        return isSoldByWeight;
    }

    public void setIsSoldByWeight(Boolean isSoldByWeight) {
        this.isSoldByWeight = isSoldByWeight;
    }

    public String getUnitLabel() {
        return unitLabel;
    }

    public void setUnitLabel(String unitLabel) {
        this.unitLabel = unitLabel;
    }
}