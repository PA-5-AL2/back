package esgi.easisell.controller;

import esgi.easisell.dto.*;
import esgi.easisell.service.DeferredPaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/deferred-payments")
@RequiredArgsConstructor
@Slf4j
public class DeferredPaymentController {

    private final DeferredPaymentService deferredPaymentService;

    /**
     * Créer un nouveau paiement différé
     * POST /api/deferred-payments
     */
    @PostMapping
    public ResponseEntity<?> createDeferredPayment(
            @RequestBody DeferredPaymentCreateDTO createDTO,
            HttpServletRequest request) {

        log.info("Création d'un paiement différé pour la vente: {}", createDTO.getSaleId());

        try {
            DeferredPaymentResponseDTO response = deferredPaymentService.createDeferredPayment(createDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Erreur lors de la création du paiement différé", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Récupérer tous les paiements différés d'un client
     * GET /api/deferred-payments/client/{clientId}
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<?> getDeferredPaymentsByClient(
            @PathVariable UUID clientId,
            HttpServletRequest request) {

        log.info("Récupération des paiements différés pour le client: {}", clientId);

        try {
            List<DeferredPaymentResponseDTO> payments = deferredPaymentService.getDeferredPaymentsByClient(clientId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des paiements différés", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Récupérer les paiements en retard
     * GET /api/deferred-payments/client/{clientId}/overdue
     */
    @GetMapping("/client/{clientId}/overdue")
    public ResponseEntity<?> getOverduePayments(
            @PathVariable UUID clientId,
            HttpServletRequest request) {

        log.info("Récupération des paiements en retard pour le client: {}", clientId);

        try {
            List<DeferredPaymentResponseDTO> overduePayments = deferredPaymentService.getOverduePayments(clientId);
            return ResponseEntity.ok(overduePayments);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des paiements en retard", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Mettre à jour un paiement différé
     * PUT /api/deferred-payments/{paymentId}
     */
    @PutMapping("/{paymentId}")
    public ResponseEntity<?> updateDeferredPayment(
            @PathVariable UUID paymentId,
            @RequestBody DeferredPaymentUpdateDTO updateDTO,
            HttpServletRequest request) {

        log.info("Mise à jour du paiement différé: {}", paymentId);

        try {
            DeferredPaymentResponseDTO response = deferredPaymentService.updateDeferredPayment(paymentId, updateDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du paiement différé", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Enregistrer un paiement (partiel ou total)
     * POST /api/deferred-payments/{paymentId}/pay
     */
    @PostMapping("/{paymentId}/pay")
    public ResponseEntity<?> recordPayment(
            @PathVariable UUID paymentId,
            @RequestParam BigDecimal amount,
            HttpServletRequest request) {

        log.info("Enregistrement d'un paiement de {} pour le paiement différé: {}", amount, paymentId);

        try {
            DeferredPaymentResponseDTO response = deferredPaymentService.recordPayment(paymentId, amount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de l'enregistrement du paiement", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Supprimer un paiement différé
     * DELETE /api/deferred-payments/{paymentId}
     */
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<?> deleteDeferredPayment(
            @PathVariable UUID paymentId,
            HttpServletRequest request) {

        log.info("Suppression du paiement différé: {}", paymentId);

        try {
            deferredPaymentService.deleteDeferredPayment(paymentId);
            return ResponseEntity.ok(Map.of("message", "Paiement différé supprimé avec succès"));
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du paiement différé", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Rechercher des paiements différés
     * GET /api/deferred-payments/client/{clientId}/search
     */
    @GetMapping("/client/{clientId}/search")
    public ResponseEntity<?> searchDeferredPayments(
            @PathVariable UUID clientId,
            @RequestParam String q,
            HttpServletRequest request) {

        log.info("Recherche de paiements différés pour le client: {} avec le terme: {}", clientId, q);

        try {
            List<DeferredPaymentResponseDTO> results = deferredPaymentService.searchDeferredPayments(clientId, q);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Erreur lors de la recherche de paiements différés", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtenir les statistiques des paiements différés
     * GET /api/deferred-payments/client/{clientId}/stats
     */
    @GetMapping("/client/{clientId}/stats")
    public ResponseEntity<?> getDeferredPaymentStats(
            @PathVariable UUID clientId,
            HttpServletRequest request) {

        log.info("Récupération des statistiques de paiements différés pour le client: {}", clientId);

        try {
            DeferredPaymentStatsDTO stats = deferredPaymentService.getDeferredPaymentStats(clientId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Envoyer des rappels de paiement
     * POST /api/deferred-payments/client/{clientId}/send-reminders
     */
    @PostMapping("/client/{clientId}/send-reminders")
    public ResponseEntity<?> sendPaymentReminders(
            @PathVariable UUID clientId,
            HttpServletRequest request) {

        log.info("Envoi des rappels de paiement pour le client: {}", clientId);

        try {
            deferredPaymentService.sendPaymentReminders(clientId);
            return ResponseEntity.ok(Map.of("message", "Rappels de paiement envoyés avec succès"));
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi des rappels", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Mettre à jour les statuts des paiements en retard (endpoint admin)
     * POST /api/deferred-payments/update-overdue-status
     */
    @PostMapping("/update-overdue-status")
    public ResponseEntity<?> updateOverduePayments(HttpServletRequest request) {

        log.info("Mise à jour des statuts des paiements en retard");

        try {
            deferredPaymentService.updateOverduePayments();
            return ResponseEntity.ok(Map.of("message", "Statuts des paiements en retard mis à jour"));
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour des statuts", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}