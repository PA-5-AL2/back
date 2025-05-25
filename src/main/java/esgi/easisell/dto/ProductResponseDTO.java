package esgi.easisell.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {
    private UUID productId;
    private String name;
    private String description;
    private String barcode;
    private String brand;
    private BigDecimal unitPrice;

    // Informations de la catégorie
    private UUID categoryId;
    private String categoryName;

    // Informations du client
    private UUID clientId;
    private String clientName;

    // Informations calculées
    private int stockQuantityTotal;
    private boolean hasActivePromotion;
}