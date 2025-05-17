package esgi.easisell.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class StockItemResponseDTO {
    private String stockItemId;
    private String productId;
    private String productName;
    private String productBarcode;
    private String clientId;
    private Integer quantity;
    private Integer reorderThreshold;
    private Timestamp purchaseDate;
    private Timestamp expirationDate;
    private BigDecimal purchasePrice;
    private String supplierId;
    private String supplierName;
    private boolean lowStock;
    private boolean expiringSoon;
    private boolean hasBarcode;
}