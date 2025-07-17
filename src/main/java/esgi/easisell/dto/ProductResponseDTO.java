package esgi.easisell.dto;

import esgi.easisell.entity.Product;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
public class ProductResponseDTO {
    private UUID productId;
    private String name;
    private String description;
    private String barcode;
    private String brand;
    private BigDecimal unitPrice;

    // Informations de la catégorie (aplaties)
    private UUID categoryId;
    private String categoryName;

    // Informations du client (aplaties)
    private UUID clientId;
    private String clientUsername;
    private String clientFirstName;

    private Boolean isSoldByWeight;
    private String unitLabel;
    private String formattedPrice;
    private BigDecimal purchasePrice;
    private Integer quantity;
    private Integer reorderThreshold;
    private Timestamp purchaseDate;
    private Timestamp expirationDate;

    // Informations du fournisseur (aplaties)
    private UUID supplierId;
    private String supplierName;

    // Indicateurs calculés
    private boolean lowStock;
    private boolean expiringSoon;

    // ========== NOUVELLES PROPRIÉTÉS PROMOTION ==========
    private boolean isOnPromotion;
    private BigDecimal currentPrice; // Prix actuel (promotion ou normal)
    private String formattedCurrentPrice;
    private String formattedPriceWithPromotion;

    // Informations de promotion active (si applicable)
    private UUID activePromotionId;
    private String promotionName;
    private String promotionType;
    private BigDecimal originalPrice;
    private BigDecimal promotionPrice;
    private BigDecimal savingsAmount;
    private BigDecimal savingsPercentage;
    private String formattedDiscount;
    private Timestamp promotionStartDate;
    private Timestamp promotionEndDate;
    private long daysUntilPromotionEnd;

    public ProductResponseDTO() {
    }

    public ProductResponseDTO(Product product) {
        this.productId = product.getProductId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.barcode = product.getBarcode();
        this.brand = product.getBrand();
        this.unitPrice = product.getUnitPrice();

        // Aplatir la catégorie
        if (product.getCategory() != null) {
            this.categoryId = product.getCategory().getCategoryId();
            this.categoryName = product.getCategory().getName();
        }

        // Aplatir le client
        if (product.getClient() != null) {
            this.clientId = product.getClient().getUserId();
            this.clientUsername = product.getClient().getUsername();
            this.clientFirstName = product.getClient().getFirstName();
        }

        // Propriétés de base
        this.isSoldByWeight = product.getIsSoldByWeight();
        this.unitLabel = product.getUnitLabel();
        this.purchasePrice = product.getPurchasePrice();
        this.quantity = product.getQuantity();
        this.reorderThreshold = product.getReorderThreshold();
        this.purchaseDate = product.getPurchaseDate();
        this.expirationDate = product.getExpirationDate();

        // Aplatir le fournisseur
        if (product.getSupplier() != null) {
            this.supplierId = product.getSupplier().getSupplierId();
            this.supplierName = product.getSupplier().getName();
        }

        // Calculer les indicateurs
        this.lowStock = product.getReorderThreshold() != null &&
                product.getQuantity() != null &&
                product.getQuantity() <= product.getReorderThreshold();

        // Vérifier si le produit expire bientôt (7 jours)
        if (product.getExpirationDate() != null) {
            LocalDateTime expirationTime = product.getExpirationDate().toLocalDateTime();
            LocalDateTime nowPlus7Days = LocalDateTime.now().plusDays(7);
            this.expiringSoon = expirationTime.isBefore(nowPlus7Days);
        }

        // ========== NOUVELLES PROPRIÉTÉS PROMOTION ==========
        this.isOnPromotion = product.isOnPromotion();
        this.currentPrice = product.getCurrentPrice();
        this.formattedPrice = product.getFormattedPrice();
        this.formattedCurrentPrice = String.format("%.2f €/%s", this.currentPrice, this.unitLabel);
        this.formattedPriceWithPromotion = product.getFormattedPriceWithPromotion();

        // Si le produit est en promotion, ajouter les détails
        if (this.isOnPromotion && product.getActivePromotion() != null) {
            var activePromotion = product.getActivePromotion();

            this.activePromotionId = activePromotion.getPromotionId();
            this.promotionName = activePromotion.getName();
            this.promotionType = activePromotion.getPromotionType().getLabel();
            this.originalPrice = activePromotion.getOriginalPrice();
            this.promotionPrice = activePromotion.getPromotionPrice();
            this.savingsAmount = activePromotion.getSavingsAmount();
            this.savingsPercentage = activePromotion.getSavingsPercentage();
            this.formattedDiscount = activePromotion.getFormattedDiscount();
            this.promotionStartDate = activePromotion.getStartDate();
            this.promotionEndDate = activePromotion.getEndDate();

            // Calculer les jours restants
            if (activePromotion.getEndDate() != null) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime end = activePromotion.getEndDate().toLocalDateTime();
                this.daysUntilPromotionEnd = java.time.temporal.ChronoUnit.DAYS.between(now, end);
            }
        }
    }

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Retourne les informations complètes de promotion
     */
    public java.util.Map<String, Object> getPromotionDetails() {
        java.util.Map<String, Object> details = new java.util.HashMap<>();

        details.put("isOnPromotion", this.isOnPromotion);
        details.put("currentPrice", this.currentPrice);
        details.put("formattedCurrentPrice", this.formattedCurrentPrice);

        if (this.isOnPromotion) {
            details.put("promotionId", this.activePromotionId);
            details.put("promotionName", this.promotionName);
            details.put("promotionType", this.promotionType);
            details.put("originalPrice", this.originalPrice);
            details.put("promotionPrice", this.promotionPrice);
            details.put("savings", this.savingsAmount);
            details.put("savingsPercentage", this.savingsPercentage);
            details.put("formattedDiscount", this.formattedDiscount);
            details.put("daysLeft", this.daysUntilPromotionEnd);
            details.put("endDate", this.promotionEndDate);
        }

        return details;
    }

    /**
     * Retourne un message de promotion pour l'affichage
     */
    public String getPromotionMessage() {
        if (!this.isOnPromotion) {
            return null;
        }

        StringBuilder message = new StringBuilder();
        message.append(" ").append(this.promotionName);

        if (this.formattedDiscount != null) {
            message.append(" - ").append(this.formattedDiscount);
        }

        if (this.daysUntilPromotionEnd > 0) {
            message.append(" (expire dans ").append(this.daysUntilPromotionEnd).append(" jour");
            if (this.daysUntilPromotionEnd > 1) {
                message.append("s");
            }
            message.append(")");
        } else if (this.daysUntilPromotionEnd == 0) {
            message.append(" (expire aujourd'hui)");
        }

        return message.toString();
    }

    /**
     * Indique si la promotion expire bientôt
     */
    public boolean isPromotionExpiringSoon() {
        return this.isOnPromotion && this.daysUntilPromotionEnd <= 3;
    }
}