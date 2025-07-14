package esgi.easisell.controller;

import esgi.easisell.dto.*;
import esgi.easisell.service.CustomerService;
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
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;
    private final DeferredPaymentService deferredPaymentService;

    /**
     * 🔍 ENDPOINT CLÉ : Reconnaître un client pour paiement différé
     * POST /api/customers/recognize
     */
    @PostMapping("/recognize")
    public ResponseEntity<?> recognizeCustomer(
            @RequestBody CustomerRecognitionDTO recognitionDTO,
            HttpServletRequest request) {

        log.info("Reconnaissance client: {} / {}", recognitionDTO.getFullName(), recognitionDTO.getPhone());

        try {
            CustomerRecognitionResponseDTO response = deferredPaymentService.recognizeCustomerForPayment(
                    recognitionDTO.getClientId(),
                    recognitionDTO.getFullName(),
                    recognitionDTO.getPhone(),
                    BigDecimal.valueOf(100) // Montant par défaut pour l'analyse
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la reconnaissance client", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Récupérer tous les clients d'une boutique
     * GET /api/customers/client/{clientId}
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<?> getAllCustomers(
            @PathVariable UUID clientId,
            HttpServletRequest request) {

        log.info("Récupération de tous les clients pour la boutique: {}", clientId);

        try {
            List<CustomerResponseDTO> customers = customerService.getAllCustomers(clientId);
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des clients", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Rechercher des clients
     * GET /api/customers/client/{clientId}/search
     */
    @GetMapping("/client/{clientId}/search")
    public ResponseEntity<?> searchCustomers(
            @PathVariable UUID clientId,
            @RequestParam String q,
            HttpServletRequest request) {

        log.info("Recherche clients: '{}' pour la boutique: {}", q, clientId);

        try {
            List<CustomerResponseDTO> results = customerService.searchCustomers(clientId, q);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Erreur lors de la recherche clients", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Récupérer les clients VIP
     * GET /api/customers/client/{clientId}/vip
     */
    @GetMapping("/client/{clientId}/vip")
    public ResponseEntity<?> getVipCustomers(
            @PathVariable UUID clientId,
            HttpServletRequest request) {

        try {
            List<CustomerResponseDTO> vipCustomers = customerService.getVipCustomers(clientId);
            return ResponseEntity.ok(vipCustomers);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des clients VIP", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Récupérer les clients à risque
     * GET /api/customers/client/{clientId}/risky
     */
    @GetMapping("/client/{clientId}/risky")
    public ResponseEntity<?> getRiskyCustomers(
            @PathVariable UUID clientId,
            HttpServletRequest request) {

        try {
            List<CustomerResponseDTO> riskyCustomers = customerService.getRiskyCustomers(clientId);
            return ResponseEntity.ok(riskyCustomers);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des clients à risque", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Créer un nouveau client
     * POST /api/customers/client/{clientId}
     */
    @PostMapping("/client/{clientId}")
    public ResponseEntity<?> createCustomer(
            @PathVariable UUID clientId,
            @RequestBody CustomerCreateDTO createDTO,
            HttpServletRequest request) {

        log.info("Création client: {} {} pour la boutique: {}",
                createDTO.getFirstName(), createDTO.getLastName(), clientId);

        try {
            CustomerResponseDTO response = customerService.createCustomer(clientId, createDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Erreur lors de la création du client", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Modifier un client existant
     * PUT /api/customers/{customerId}
     */
    @PutMapping("/{customerId}")
    public ResponseEntity<?> updateCustomer(
            @PathVariable UUID customerId,
            @RequestBody CustomerCreateDTO updateDTO,
            HttpServletRequest request) {

        log.info("Modification client: {}", customerId);

        try {
            CustomerResponseDTO response = customerService.updateCustomer(customerId, updateDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la modification du client", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtenir un client par ID
     * GET /api/customers/{customerId}
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<?> getCustomerById(
            @PathVariable UUID customerId,
            HttpServletRequest request) {

        try {
            CustomerResponseDTO customer = customerService.getCustomerById(customerId);
            return ResponseEntity.ok(customer);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du client", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Blacklister un client
     * POST /api/customers/{customerId}/blacklist
     */
    @PostMapping("/{customerId}/blacklist")
    public ResponseEntity<?> blacklistCustomer(
            @PathVariable UUID customerId,
            @RequestParam String reason,
            HttpServletRequest request) {

        log.info("Blacklistage client: {} - Raison: {}", customerId, reason);

        try {
            customerService.blacklistCustomer(customerId, reason);
            return ResponseEntity.ok(Map.of("message", "Client blacklisté avec succès"));
        } catch (Exception e) {
            log.error("Erreur lors du blacklistage", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Réhabiliter un client blacklisté
     * POST /api/customers/{customerId}/rehabilitate
     */
    @PostMapping("/{customerId}/rehabilitate")
    public ResponseEntity<?> rehabilitateCustomer(
            @PathVariable UUID customerId,
            HttpServletRequest request) {

        log.info("Réhabilitation client: {}", customerId);

        try {
            customerService.rehabilitateCustomer(customerId);
            return ResponseEntity.ok(Map.of("message", "Client réhabilité avec succès"));
        } catch (Exception e) {
            log.error("Erreur lors de la réhabilitation", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Statistiques des clients
     * GET /api/customers/client/{clientId}/stats
     */
    @GetMapping("/client/{clientId}/stats")
    public ResponseEntity<?> getCustomerStats(
            @PathVariable UUID clientId,
            HttpServletRequest request) {

        try {
            CustomerStatsDTO stats = customerService.getCustomerStats(clientId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques clients", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}