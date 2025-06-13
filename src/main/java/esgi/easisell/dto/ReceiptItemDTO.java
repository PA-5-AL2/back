package esgi.easisell.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptItemDTO {
    private String productName;
    private String barcode;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal total;
}