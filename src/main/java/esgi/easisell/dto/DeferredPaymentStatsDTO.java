package esgi.easisell.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeferredPaymentStatsDTO {

    private BigDecimal totalPendingAmount;
    private int overdueCount;
    private int totalCount;
    private int paidCount;
    private int partialCount;
    private int pendingCount;
    private List<Object[]> statusStats;

    // Constructeur avec calculs automatiques
    public DeferredPaymentStatsDTO(BigDecimal totalPendingAmount, int overdueCount, List<Object[]> statusStats) {
        this.totalPendingAmount = totalPendingAmount;
        this.overdueCount = overdueCount;
        this.statusStats = statusStats;

        // Calculer les totaux à partir des statistiques
        this.totalCount = 0;
        this.paidCount = 0;
        this.partialCount = 0;
        this.pendingCount = 0;

        if (statusStats != null) {
            for (Object[] stat : statusStats) {
                String status = (String) stat[0];
                Long count = (Long) stat[1];

                totalCount += count.intValue();

                switch (status) {
                    case "PAID":
                        paidCount = count.intValue();
                        break;
                    case "PARTIAL":
                        partialCount = count.intValue();
                        break;
                    case "PENDING":
                        pendingCount = count.intValue();
                        break;
                }
            }
        }
    }
}