package esgi.easisell.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleItemResponseDTO {
    private UUID saleItemId;
    private UUID saleId;
    private UUID productId;
    private String productName;
    private String productBarcode;
    private int quantitySold;
    private BigDecimal unitPrice;
    private BigDecimal priceAtSale;
    private BigDecimal totalPrice;
}
