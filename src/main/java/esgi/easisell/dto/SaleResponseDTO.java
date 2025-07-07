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
public class SaleResponseDTO {
    private UUID saleId;
    private Timestamp saleTimestamp;
    private BigDecimal totalAmount;
    private Boolean isDeferred;
    private UUID clientId;
    private String clientName;
    private String clientUsername;
    private int itemCount;
    private Boolean isPaid;
}