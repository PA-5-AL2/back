package esgi.easisell.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInfoDTO {
    private UUID paymentId;
    private String type;
    private BigDecimal amount;
    private String currency;
    private Timestamp paymentDate;
}