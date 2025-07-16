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

        // === NOUVEAUX CHAMPS STOCK ===
        this.isSoldByWeight = product.getIsSoldByWeight();
        this.unitLabel = product.getUnitLabel();
        this.formattedPrice = product.getFormattedPrice();
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
    }
}