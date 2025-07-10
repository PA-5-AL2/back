package esgi.easisell.dto;

import esgi.easisell.entity.Product;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

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
    }
}