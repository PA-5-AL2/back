package esgi.easisell.controller;

import esgi.easisell.dto.StatisticsDto;
import esgi.easisell.service.StatisticsService;
import esgi.easisell.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final SecurityUtils securityUtils;

    @Autowired
    public StatisticsController(StatisticsService statisticsService, SecurityUtils securityUtils) {
        this.statisticsService = statisticsService;
        this.securityUtils = securityUtils;
    }


    @GetMapping
    public ResponseEntity<StatisticsDto> getStatistics(
            @RequestParam(defaultValue = "MONTHLY") StatisticsService.StatisticsPeriod period, HttpServletRequest request) {
        String clientId = securityUtils.getCurrentUserId(request);
        if (clientId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            log.info("Demande de statistiques pour le client {} avec la période {}", clientId, period);
            StatisticsDto statistics = statisticsService.getStatistics(UUID.fromString(clientId), period);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Erreur lors du calcul des statistiques pour le client {}", clientId, e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/custom")
    public ResponseEntity<StatisticsDto> getStatisticsForDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest request) {


        String clientId = securityUtils.getCurrentUserId(request);
        if (clientId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("Demande de statistiques pour le client {} du {} au {}", clientId, startDate, endDate);

        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }

        try {
            StatisticsDto statistics = statisticsService.getStatisticsForDateRange(UUID.fromString(clientId), startDate, endDate);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Erreur lors du calcul des statistiques pour le client {} sur la période {} - {}",
                    clientId, startDate, endDate, e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/category/{categoryId}")
    public ResponseEntity<StatisticsDto> getStatisticsForCategory(
            @PathVariable String categoryId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest request) {

        String clientId = securityUtils.getCurrentUserId(request);
        if (clientId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Statistiques pour la catégorie {} du client {} entre {} et {}",
                categoryId, clientId, startDate, endDate);

        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }

        try {
            StatisticsDto stats = statisticsService.getStatisticsForCategory(
                    UUID.fromString(clientId),
                    UUID.fromString(categoryId),
                    startDate,
                    endDate
            );
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Erreur statistiques catégorie", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    /**
     * Statistiques du jour
     * GET /api/statistics/today
     */
    @GetMapping("/today")
    public ResponseEntity<StatisticsDto> getTodayStatistics(HttpServletRequest request) {
        String clientId = securityUtils.getCurrentUserId(request);
        if (clientId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            LocalDate today = LocalDate.now();
            log.info("Statistiques du jour {} pour le client {}", today, clientId);

            StatisticsDto statistics = statisticsService.getStatisticsForDateRange(
                    UUID.fromString(clientId), today, today);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Erreur statistiques du jour", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Statistiques d'hier
     * GET /api/statistics/yesterday
     */
    @GetMapping("/yesterday")
    public ResponseEntity<StatisticsDto> getYesterdayStatistics(HttpServletRequest request) {
        String clientId = securityUtils.getCurrentUserId(request);
        if (clientId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            log.info("Statistiques d'hier {} pour le client {}", yesterday, clientId);

            StatisticsDto statistics = statisticsService.getStatisticsForDateRange(
                    UUID.fromString(clientId), yesterday, yesterday);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Erreur statistiques d'hier", e);
            return ResponseEntity.internalServerError().build();
        }
    }

}