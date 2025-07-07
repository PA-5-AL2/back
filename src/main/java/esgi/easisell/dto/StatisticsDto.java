package esgi.easisell.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDto {
    private BigDecimal averageRevenuePerPeriod;
    private Integer averageVolumePerPeriod;
    private BigDecimal totalRevenue;
    private Integer totalVolume;
    private Integer numberOfPeriods;
    private LocalDate startDate;
    private LocalDate endDate;
    private String periodType;
    private String topProductId;
}