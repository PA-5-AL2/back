package esgi.easisell.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductDTO {
    private String name;
    private String description;
    private String barcode;
    private String brand;
    private BigDecimal unitPrice;
    private String categoryId;
    private String clientId;
    private Boolean isSoldByWeight = false;
    private String unitLabel = "pièce";
    private BigDecimal purchasePrice;
    private Integer quantity = 0;
    private Integer reorderThreshold;
    private LocalDateTime purchaseDate;
    private LocalDateTime expirationDate;
    private String supplierId; // optionnel

}