package esgi.easisell.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class PaymentResult {
    private boolean successful;
    private UUID paymentId;
    private BigDecimal amountPaid;
    private BigDecimal changeAmount;
    private String currency;
    private String message;
    private String errorMessage;
}