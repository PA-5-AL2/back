package esgi.easisell.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeferredPaymentStatsEnrichedDTO {
    private DeferredPaymentStatsDTO classicStats;
    private List<Object[]> statsByCustomerType;
    private List<CustomerResponseDTO> topCustomersWithDeferredPayments;
}
