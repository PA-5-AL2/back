package esgi.easisell.controller;

import esgi.easisell.entity.Client;
import esgi.easisell.repository.ClientRepository;
import esgi.easisell.service.EmailService;
import esgi.easisell.exception.EmailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/employee-access")
@RequiredArgsConstructor
@Slf4j
public class EmployeeAccessController {

    private final EmailService emailService;
    private final ClientRepository clientRepository;

    /**
     * Endpoint pour qu'un employé demande l'accès à un client
     * POST /api/employee-access/request
     */
    @PostMapping("/request")
    public ResponseEntity<?> requestAccess(@RequestBody EmployeeAccessRequestDTO request) {
        log.info("Demande d'accès employé reçue: {} pour le client ID: {}",
                request.getEmployeeName(), request.getClientId());

        try {
            // Vérifier que le client existe
            Optional<Client> clientOpt = clientRepository.findById(UUID.fromString(request.getClientId()));

            if (clientOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Client non trouvé avec l'ID: " + request.getClientId()
                ));
            }

            Client client = clientOpt.get();

            // Envoyer l'email au client
            emailService.sendEmployeeAccessRequest(
                    client,
                    request.getEmployeeName(),
                    request.getEmployeeEmail()
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Demande d'accès envoyée avec succès au client: " + client.getName()
            ));

        } catch (EmailException e) {
            log.error("Erreur lors de l'envoi de la demande d'accès", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Erreur lors de l'envoi de l'email: " + e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la demande d'accès", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Erreur inattendue: " + e.getMessage()
            ));
        }
    }

    /**
     * DTO pour la demande d'accès employé
     */
    public static class EmployeeAccessRequestDTO {
        private String clientId;
        private String employeeName;
        private String employeeEmail;

        // Constructeurs
        public EmployeeAccessRequestDTO() {}

        public EmployeeAccessRequestDTO(String clientId, String employeeName, String employeeEmail) {
            this.clientId = clientId;
            this.employeeName = employeeName;
            this.employeeEmail = employeeEmail;
        }

        // Getters et Setters
        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }

        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

        public String getEmployeeEmail() { return employeeEmail; }
        public void setEmployeeEmail(String employeeEmail) { this.employeeEmail = employeeEmail; }
    }
}