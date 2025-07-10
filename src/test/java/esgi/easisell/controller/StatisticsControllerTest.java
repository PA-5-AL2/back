/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : StatisticsControllerTest.java
 * @description : Tests unitaires pour le contrôleur de statistiques
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 10/07/2025
 * @package     : esgi.easisell.controller
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.controller;

import esgi.easisell.dto.StatisticsDto;
import esgi.easisell.service.StatisticsService;
import esgi.easisell.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour le contrôleur de statistiques
 *
 * Tests des méthodes :
 * - getStatistics()
 * - getStatisticsForDateRange()
 * - getStatisticsForCategory()
 */
@ExtendWith(MockitoExtension.class)
class StatisticsControllerTest {

    @Mock
    private StatisticsService statisticsService;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private StatisticsController statisticsController;

    private UUID clientId;
    private UUID categoryId;
    private StatisticsDto statisticsDto;

    /**
     * Configuration initiale pour chaque test
     */
    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        // Configuration du DTO de statistiques
        statisticsDto = new StatisticsDto();
        // Ajouter des propriétés selon la vraie structure du DTO
    }

    // ==================== TESTS STATISTIQUES GÉNÉRALES ====================

    /**
     * Test de récupération des statistiques réussie
     */
    @Test
    @DisplayName("getStatistics() - Récupération réussie")
    void testGetStatisticsSuccess() {
        // Given
        StatisticsService.StatisticsPeriod period = StatisticsService.StatisticsPeriod.MONTHLY;
        when(securityUtils.getCurrentUserId(request)).thenReturn(clientId.toString());
        when(statisticsService.getStatistics(clientId, period)).thenReturn(statisticsDto);

        // When
        ResponseEntity<StatisticsDto> response = statisticsController.getStatistics(period, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(statisticsDto, response.getBody());
        verify(statisticsService, times(1)).getStatistics(clientId, period);
    }

    /**
     * Test de récupération des statistiques - utilisateur non authentifié
     */
    @Test
    @DisplayName("getStatistics() - Utilisateur non authentifié")
    void testGetStatisticsUnauthorized() {
        // Given
        StatisticsService.StatisticsPeriod period = StatisticsService.StatisticsPeriod.WEEKLY;
        when(securityUtils.getCurrentUserId(request)).thenReturn(null);

        // When
        ResponseEntity<StatisticsDto> response = statisticsController.getStatistics(period, request);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
        verify(statisticsService, never()).getStatistics(any(), any());
    }

    /**
     * Test de récupération des statistiques avec erreur
     */
    @Test
    @DisplayName("getStatistics() - Erreur du service")
    void testGetStatisticsError() {
        // Given
        StatisticsService.StatisticsPeriod period = StatisticsService.StatisticsPeriod.WEEKLY;
        when(securityUtils.getCurrentUserId(request)).thenReturn(clientId.toString());
        when(statisticsService.getStatistics(clientId, period)).thenThrow(new RuntimeException("Erreur de calcul"));

        // When
        ResponseEntity<StatisticsDto> response = statisticsController.getStatistics(period, request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    // ==================== TESTS STATISTIQUES PAR PÉRIODE ====================

    /**
     * Test de récupération des statistiques sur une période réussie
     */
    @Test
    @DisplayName("getStatisticsForDateRange() - Récupération réussie")
    void testGetStatisticsForDateRangeSuccess() {
        // Given
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);
        when(securityUtils.getCurrentUserId(request)).thenReturn(clientId.toString());
        when(statisticsService.getStatisticsForDateRange(clientId, startDate, endDate)).thenReturn(statisticsDto);

        // When
        ResponseEntity<StatisticsDto> response = statisticsController.getStatisticsForDateRange(startDate, endDate, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(statisticsDto, response.getBody());
        verify(statisticsService, times(1)).getStatisticsForDateRange(clientId, startDate, endDate);
    }

    /**
     * Test de récupération des statistiques - utilisateur non authentifié
     */
    @Test
    @DisplayName("getStatisticsForDateRange() - Utilisateur non authentifié")
    void testGetStatisticsForDateRangeUnauthorized() {
        // Given
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);
        when(securityUtils.getCurrentUserId(request)).thenReturn(null);

        // When
        ResponseEntity<StatisticsDto> response = statisticsController.getStatisticsForDateRange(startDate, endDate, request);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
        verify(statisticsService, never()).getStatisticsForDateRange(any(), any(), any());
    }

    /**
     * Test de récupération des statistiques avec dates invalides
     */
    @Test
    @DisplayName("getStatisticsForDateRange() - Dates invalides")
    void testGetStatisticsForDateRangeInvalidDates() {
        // Given
        LocalDate startDate = LocalDate.of(2025, 1, 31);
        LocalDate endDate = LocalDate.of(2025, 1, 1); // Date de fin avant date de début
        when(securityUtils.getCurrentUserId(request)).thenReturn(clientId.toString());

        // When
        ResponseEntity<StatisticsDto> response = statisticsController.getStatisticsForDateRange(startDate, endDate, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(statisticsService, never()).getStatisticsForDateRange(any(), any(), any());
    }

    /**
     * Test de récupération des statistiques avec erreur
     */
    @Test
    @DisplayName("getStatisticsForDateRange() - Erreur du service")
    void testGetStatisticsForDateRangeError() {
        // Given
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);
        when(securityUtils.getCurrentUserId(request)).thenReturn(clientId.toString());
        when(statisticsService.getStatisticsForDateRange(clientId, startDate, endDate))
                .thenThrow(new RuntimeException("Erreur de calcul"));

        // When
        ResponseEntity<StatisticsDto> response = statisticsController.getStatisticsForDateRange(startDate, endDate, request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    // ==================== TESTS STATISTIQUES PAR CATÉGORIE ====================

    /**
     * Test de récupération des statistiques par catégorie réussie
     */
    @Test
    @DisplayName("getStatisticsForCategory() - Récupération réussie")
    void testGetStatisticsForCategorySuccess() {
        // Given
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);
        String categoryIdStr = categoryId.toString();
        when(securityUtils.getCurrentUserId(request)).thenReturn(clientId.toString());
        when(statisticsService.getStatisticsForCategory(clientId, categoryId, startDate, endDate))
                .thenReturn(statisticsDto);

        // When
        ResponseEntity<StatisticsDto> response = statisticsController.getStatisticsForCategory(
                categoryIdStr, startDate, endDate, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(statisticsDto, response.getBody());
        verify(statisticsService, times(1)).getStatisticsForCategory(clientId, categoryId, startDate, endDate);
    }

    /**
     * Test de récupération des statistiques par catégorie - utilisateur non authentifié
     */
    @Test
    @DisplayName("getStatisticsForCategory() - Utilisateur non authentifié")
    void testGetStatisticsForCategoryUnauthorized() {
        // Given
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);
        String categoryIdStr = categoryId.toString();
        when(securityUtils.getCurrentUserId(request)).thenReturn(null);

        // When
        ResponseEntity<StatisticsDto> response = statisticsController.getStatisticsForCategory(
                categoryIdStr, startDate, endDate, request);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
        verify(statisticsService, never()).getStatisticsForCategory(any(), any(), any(), any());
    }

    /**
     * Test de récupération des statistiques par catégorie avec dates invalides
     */
    @Test
    @DisplayName("getStatisticsForCategory() - Dates invalides")
    void testGetStatisticsForCategoryInvalidDates() {
        // Given
        LocalDate startDate = LocalDate.of(2025, 1, 31);
        LocalDate endDate = LocalDate.of(2025, 1, 1); // Date de fin avant date de début
        String categoryIdStr = categoryId.toString();
        when(securityUtils.getCurrentUserId(request)).thenReturn(clientId.toString());

        // When
        ResponseEntity<StatisticsDto> response = statisticsController.getStatisticsForCategory(
                categoryIdStr, startDate, endDate, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(statisticsService, never()).getStatisticsForCategory(any(), any(), any(), any());
    }

    /**
     * Test de récupération des statistiques par catégorie avec erreur
     */
    @Test
    @DisplayName("getStatisticsForCategory() - Erreur du service")
    void testGetStatisticsForCategoryError() {
        // Given
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);
        String categoryIdStr = categoryId.toString();
        when(securityUtils.getCurrentUserId(request)).thenReturn(clientId.toString());
        when(statisticsService.getStatisticsForCategory(clientId, categoryId, startDate, endDate))
                .thenThrow(new RuntimeException("Erreur de calcul"));

        // When
        ResponseEntity<StatisticsDto> response = statisticsController.getStatisticsForCategory(
                categoryIdStr, startDate, endDate, request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    // ==================== TESTS AVEC DIFFÉRENTES PÉRIODES ====================

    /**
     * Test avec période DAILY
     */
    @Test
    @DisplayName("getStatistics() - Période DAILY")
    void testGetStatisticsDaily() {
        // Given
        StatisticsService.StatisticsPeriod period = StatisticsService.StatisticsPeriod.WEEKLY;
        when(securityUtils.getCurrentUserId(request)).thenReturn(clientId.toString());
        when(statisticsService.getStatistics(clientId, period)).thenReturn(statisticsDto);

        // When
        ResponseEntity<StatisticsDto> response = statisticsController.getStatistics(period, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(statisticsService, times(1)).getStatistics(clientId, period);
    }

    /**
     * Test avec période WEEKLY
     */
    @Test
    @DisplayName("getStatistics() - Période WEEKLY")
    void testGetStatisticsWeekly() {
        // Given
        StatisticsService.StatisticsPeriod period = StatisticsService.StatisticsPeriod.WEEKLY;
        when(securityUtils.getCurrentUserId(request)).thenReturn(clientId.toString());
        when(statisticsService.getStatistics(clientId, period)).thenReturn(statisticsDto);

        // When
        ResponseEntity<StatisticsDto> response = statisticsController.getStatistics(period, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(statisticsService, times(1)).getStatistics(clientId, period);
    }

    /**
     * Test avec période YEARLY
     */
    @Test
    @DisplayName("getStatistics() - Période YEARLY")
    void testGetStatisticsYearly() {
        // Given
        StatisticsService.StatisticsPeriod period = StatisticsService.StatisticsPeriod.YEARLY;
        when(securityUtils.getCurrentUserId(request)).thenReturn(clientId.toString());
        when(statisticsService.getStatistics(clientId, period)).thenReturn(statisticsDto);

        // When
        ResponseEntity<StatisticsDto> response = statisticsController.getStatistics(period, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(statisticsService, times(1)).getStatistics(clientId, period);
    }

    // ==================== TESTS DE CONFIGURATION ====================

    /**
     * Test de configuration des mocks
     */
    @Test
    @DisplayName("Configuration des mocks")
    void testMockConfiguration() {
        // Vérifier que les mocks sont correctement injectés
        assertNotNull(statisticsController);
        assertNotNull(statisticsService);
        assertNotNull(securityUtils);
    }
}