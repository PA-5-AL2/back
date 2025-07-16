package esgi.easisell.service;

import esgi.easisell.dto.StatisticsDto;
import esgi.easisell.entity.Sale;
import esgi.easisell.entity.SaleItem;
import esgi.easisell.repository.SaleRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final SaleRepository saleRepository;

    @Getter
    public enum StatisticsPeriod {
        WEEKLY("semaine"),
        MONTHLY("mois"),
        YEARLY("année");

        private final String displayName;

        StatisticsPeriod(String displayName) {
            this.displayName = displayName;
        }
    }

    public StatisticsDto getStatistics(UUID clientId, StatisticsPeriod period) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = calculateStartDate(endDate, period);

        log.info("Calcul des statistiques pour le client {} sur la période {} - {} à {}",
                clientId, period, startDate, endDate);

        return calculateStatisticsForPeriod(clientId, startDate, endDate, period, null);
    }

    public StatisticsDto getStatisticsForDateRange(UUID clientId, LocalDate startDate, LocalDate endDate) {
        log.info("Calcul des statistiques pour le client {} sur la période personnalisée {} à {}",
                clientId, startDate, endDate);

        return calculateStatisticsForPeriod(clientId, startDate, endDate, null, null);
    }

    private StatisticsDto calculateStatisticsForPeriod(UUID clientId, LocalDate startDate,
                                                       LocalDate endDate, StatisticsPeriod period, List<Sale> sales) {
        Timestamp startTimestamp = Timestamp.valueOf(startDate.atStartOfDay());
        Timestamp endTimestamp = Timestamp.valueOf(endDate.atTime(23, 59, 59));

        if(sales == null) sales = saleRepository.findSalesForStatistics(clientId, startTimestamp, endTimestamp);

        if (sales.isEmpty()) {
            return createEmptyStatistics(startDate, endDate, period);
        }
        BigDecimal totalRevenue = calculateTotalRevenue(sales);
        Integer totalVolume = calculateTotalVolume(sales);
        BigDecimal totalPurchaseCost = calculateTotalPurchaseCost(sales);
        BigDecimal totalProfit = totalRevenue.subtract(totalPurchaseCost);
        BigDecimal profitMargin = calculateProfitMargin(totalRevenue, totalProfit);

        String topProductId = findTopProductId(sales);

        StatisticsCalculation calculation = calculateAverages(totalRevenue, totalVolume, totalProfit,
                startDate, endDate, period);

        return StatisticsDto.builder()
                .totalRevenue(totalRevenue)
                .totalVolume(totalVolume)
                .averageRevenuePerPeriod(calculation.averageRevenue)
                .averageVolumePerPeriod(calculation.averageVolume)
                .numberOfPeriods(calculation.numberOfPeriods)
                .startDate(startDate)
                .endDate(endDate)
                .periodType(period != null ? period.name() : "CUSTOM")
                .topProductId(topProductId)
                .totalPurchaseCost(totalPurchaseCost)
                .totalProfit(totalProfit)
                .profitMargin(profitMargin)
                .averageProfitPerPeriod(calculation.averageProfit)
                .build();
    }
    /**
     * Calcule le coût total d'achat des produits vendus
     */
    private BigDecimal calculateTotalPurchaseCost(List<Sale> sales) {
        return sales.stream()
                .flatMap(sale -> sale.getSaleItems().stream())
                .map(this::calculateItemPurchaseCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcule le coût d'achat d'un article vendu
     */
    private BigDecimal calculateItemPurchaseCost(SaleItem saleItem) {
        BigDecimal purchasePrice = saleItem.getProduct().getPurchasePrice();
        if (purchasePrice == null) {
            purchasePrice = BigDecimal.ZERO; // Si pas de prix d'achat configuré
        }
        return purchasePrice.multiply(saleItem.getQuantitySold());
    }

    /**
     * Calcule la marge bénéficiaire en pourcentage
     */
    private BigDecimal calculateProfitMargin(BigDecimal totalRevenue, BigDecimal totalProfit) {
        if (totalRevenue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalProfit
                .divide(totalRevenue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Mettre à jour le record StatisticsCalculation
     */
    private record StatisticsCalculation(
            BigDecimal averageRevenue,
            Integer averageVolume,
            Integer numberOfPeriods,
            BigDecimal averageProfit
    ) {}

    private BigDecimal calculateTotalRevenue(List<Sale> sales) {
        return sales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ✅ FIX LIGNE 102 - Conversion BigDecimal vers int pour summingInt
    private Integer calculateTotalVolume(List<Sale> sales) {
        return sales.stream()
                .flatMap(sale -> sale.getSaleItems().stream())
                .mapToInt(item -> item.getQuantitySold().intValue())  // ✅ FIX : Conversion BigDecimal vers int
                .sum();
    }

    private StatisticsCalculation calculateAverages(BigDecimal totalRevenue, Integer totalVolume,
                                                    BigDecimal totalProfit, LocalDate startDate,
                                                    LocalDate endDate, StatisticsPeriod period) {
        int numberOfPeriods;
        BigDecimal averageRevenue;
        Integer averageVolume;
        BigDecimal averageProfit;

        if (period != null) {
            numberOfPeriods = calculateNumberOfPeriods(startDate, endDate, period);
        } else {
            numberOfPeriods = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
        }

        if (numberOfPeriods > 0) {
            averageRevenue = totalRevenue.divide(BigDecimal.valueOf(numberOfPeriods), 2, RoundingMode.HALF_UP);
            averageVolume = totalVolume / numberOfPeriods;
            averageProfit = totalProfit.divide(BigDecimal.valueOf(numberOfPeriods), 2, RoundingMode.HALF_UP);
        } else {
            averageRevenue = BigDecimal.ZERO;
            averageVolume = 0;
            averageProfit = BigDecimal.ZERO;
            numberOfPeriods = 1;
        }

        return new StatisticsCalculation(averageRevenue, averageVolume, numberOfPeriods, averageProfit);
    }

    private LocalDate calculateStartDate(LocalDate endDate, StatisticsPeriod period) {
        return switch (period) {
            case WEEKLY -> endDate.minusWeeks(1);
            case MONTHLY -> endDate.minusMonths(1);
            case YEARLY -> endDate.minusYears(1);
        };
    }

    private int calculateNumberOfPeriods(LocalDate startDate, LocalDate endDate, StatisticsPeriod period) {
        return switch (period) {
            case WEEKLY -> Math.max(1, (int) ChronoUnit.WEEKS.between(startDate, endDate));
            case MONTHLY -> Math.max(1, (int) ChronoUnit.MONTHS.between(startDate, endDate));
            case YEARLY -> Math.max(1, (int) ChronoUnit.YEARS.between(startDate, endDate));
        };
    }
    private String findTopProductId(List<Sale> sales) {
        return sales.stream()
                .flatMap(sale -> sale.getSaleItems().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getProductId().toString(),
                        Collectors.summingInt(item -> item.getQuantitySold().intValue())  // ✅ FIX : Conversion BigDecimal vers int
                ))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public StatisticsDto getStatisticsForCategory(UUID clientId, UUID categoryId, LocalDate startDate, LocalDate endDate) {
        Timestamp startTimestamp = Timestamp.valueOf(startDate.atStartOfDay());
        Timestamp endTimestamp = Timestamp.valueOf(endDate.atTime(23, 59, 59));

        List<Sale> sales = saleRepository.findSalesForCategoryStatistics(clientId, categoryId, startTimestamp, endTimestamp);

        return calculateStatisticsForPeriod(clientId, startDate, endDate, null, sales);
    }
    private StatisticsDto createEmptyStatistics(LocalDate startDate, LocalDate endDate, StatisticsPeriod period) {
        return StatisticsDto.builder()
                .totalRevenue(BigDecimal.ZERO)
                .totalVolume(0)
                .averageRevenuePerPeriod(BigDecimal.ZERO)
                .averageVolumePerPeriod(0)
                .numberOfPeriods(0)
                .startDate(startDate)
                .endDate(endDate)
                .periodType(period != null ? period.name() : "CUSTOM")
                .totalPurchaseCost(BigDecimal.ZERO)
                .totalProfit(BigDecimal.ZERO)
                .profitMargin(BigDecimal.ZERO)
                .averageProfitPerPeriod(BigDecimal.ZERO)
                .topProductId(null)
                .build();
    }
}