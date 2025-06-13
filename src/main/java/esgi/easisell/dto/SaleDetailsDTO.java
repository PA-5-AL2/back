package esgi.easisell.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleDetailsDTO {
    private UUID saleId;
    private Timestamp saleTimestamp;
    private BigDecimal totalAmount;
    private Boolean isDeferred;
    private ClientInfoDTO client;
    private List<SaleItemResponseDTO> items;
    private List<PaymentInfoDTO> payments;
    private Boolean isPaid;
}