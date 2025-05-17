package esgi.easisell.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateStockItemDTO {
    private Integer quantity;
    private Integer reorderThreshold;
    private LocalDateTime purchaseDate;
    private LocalDateTime expirationDate;
    private BigDecimal purchasePrice;
    private String supplierId;
}