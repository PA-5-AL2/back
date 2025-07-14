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
public class CustomerStatsDTO {
    private int totalCustomers;
    private BigDecimal totalAmountSpent;
    private List<Object[]> statsByType;
}