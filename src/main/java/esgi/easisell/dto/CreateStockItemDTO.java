package esgi.easisell.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateStockItemDTO {
    private String productId;
    private String clientId;
    private Integer quantity;
    private Integer reorderThreshold;
    private LocalDateTime purchaseDate;
    private LocalDateTime expirationDate;
    private BigDecimal purchasePrice;
    private String supplierId;
}