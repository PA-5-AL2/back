package esgi.easisell.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptDTO {
    private UUID saleId;
    private String receiptNumber;
    private LocalDateTime dateTime;
    private ClientInfoDTO client;
    private List<ReceiptItemDTO> items;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private List<PaymentInfoDTO> payments;
    private BigDecimal changeAmount;
    private String currency;
    private String cashierName;
}