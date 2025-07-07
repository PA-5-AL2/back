package esgi.easisell.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDTO {
    private String name;
    private String description;
    private String barcode;
    private String brand;
    private BigDecimal unitPrice;
    private String categoryId;
    private String clientId;
}