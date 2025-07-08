package esgi.easisell.controller;

import esgi.easisell.dto.*;
import esgi.easisell.entity.Client;
import esgi.easisell.entity.ClientRequest;
import esgi.easisell.service.ClientRequestService;
import esgi.easisell.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/client-requests")
@RequiredArgsConstructor
@Slf4j
public class ClientRequestController {

    private final ClientRequestService clientRequestService;
    private final SecurityUtils securityUtils;

    /**
     * ENDPOINT PUBLIC - Soumettre une demande de création de compte
     * Accessible sans authentification
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitClientRequest(@RequestBody ClientRequestDTO requestDTO) {
        try {
            log.info("🌟 Nouvelle demande reçue de: {} - {}", requestDTO.getCompanyName(), requestDTO.getEmail());

            ClientRequest request = clientRequestService.submitRequest(requestDTO);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Votre demande a été envoyée avec succès ! Notre équipe vous contactera sous 24h.",
                    "requestId", request.getRequestId(),
                    "companyName", request.getCompanyName()
            ));
        } catch (IllegalArgumentException e) {
            log.warn("Demande rejetée: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la soumission de demande", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Une erreur technique est survenue. Veuillez réessayer."
            ));
        }
    }

    /**
     * ADMIN ONLY - Récupérer les demandes en attente
     */
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingRequests(HttpServletRequest request) {
        if (!securityUtils.isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accès réservé aux administrateurs"));
        }

        try {
            List<ClientRequestResponseDTO> pendingRequests = clientRequestService.getPendingRequests();
            return ResponseEntity.ok(Map.of(
                    "requests", pendingRequests,
                    "count", pendingRequests.size()
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des demandes en attente", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des demandes"));
        }
    }

    /**
     * ADMIN ONLY - Récupérer toutes les demandes
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllRequests(HttpServletRequest request) {
        if (!securityUtils.isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accès réservé aux administrateurs"));
        }

        try {
            List<ClientRequestResponseDTO> allRequests = clientRequestService.getAllRequests();
            return ResponseEntity.ok(Map.of(
                    "requests", allRequests,
                    "total", allRequests.size()
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de toutes les demandes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des demandes"));
        }
    }

    /**
     * ADMIN ONLY - Approuver une demande et créer le compte client
     */
    @PostMapping("/{requestId}/approve")
    public ResponseEntity<?> approveRequest(
            @PathVariable UUID requestId,
            @RequestBody ApproveRequestDTO approveDTO,
            HttpServletRequest request) {

        if (!securityUtils.isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accès réservé aux administrateurs"));
        }

        try {
            Client newClient = clientRequestService.approveRequest(requestId, approveDTO);
            log.info("Demande approuvée et compte créé pour: {}", newClient.getName());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Demande approuvée et compte client créé avec succès",
                    "clientId", newClient.getUserId(),
                    "clientEmail", newClient.getUsername()
            ));
        } catch (RuntimeException e) {
            log.warn("Erreur lors de l'approbation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Erreur lors de l'approbation de la demande", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Erreur lors de l'approbation de la demande"
            ));
        }
    }

    /**
     * ADMIN ONLY - Rejeter une demande
     */
    @PostMapping("/{requestId}/reject")
    public ResponseEntity<?> rejectRequest(
            @PathVariable UUID requestId,
            @RequestBody RejectRequestDTO rejectDTO,
            HttpServletRequest request) {

        if (!securityUtils.isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accès réservé aux administrateurs"));
        }

        try {
            clientRequestService.rejectRequest(requestId, rejectDTO);
            log.info("Demande rejetée: {} - Raison: {}", requestId, rejectDTO.getReason());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Demande rejetée avec succès"
            ));
        } catch (RuntimeException e) {
            log.warn("Erreur lors du rejet: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Erreur lors du rejet de la demande", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Erreur lors du rejet de la demande"
            ));
        }
    }
}