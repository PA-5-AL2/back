package esgi.easisell.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResultDTO {
    private Boolean successful;
    private UUID paymentId;
    private UUID saleId;
    private BigDecimal amountPaid;
    private BigDecimal changeAmount;
    private String currency;
    private String message;
    private String errorMessage;
}