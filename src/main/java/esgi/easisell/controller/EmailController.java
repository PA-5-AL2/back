package esgi.easisell.controller;

import esgi.easisell.dto.EmailPreInscriptionDTO;
import esgi.easisell.dto.EmailReminderDTO;
import esgi.easisell.dto.EmailCancellationDTO;
import esgi.easisell.entity.Client;
import esgi.easisell.exception.EmailException;
import esgi.easisell.repository.ClientRepository;
import esgi.easisell.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
@Slf4j
public class EmailController {

    private final EmailService emailService;
    private final ClientRepository clientRepository;

    /**
     * Endpoint pour envoyer un rappel de paiement à un client
     */
    @PostMapping("/payment-reminder")
    public ResponseEntity<?> sendPaymentReminder(@RequestBody EmailReminderDTO reminderDTO) {
        log.info("Demande d'envoi de rappel de paiement pour le client ID: {}", reminderDTO.getClientId());

        try {
            Optional<Client> clientOpt = clientRepository.findById(UUID.fromString(reminderDTO.getClientId()));

            if (clientOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Client non trouvé avec l'ID: " + reminderDTO.getClientId()
                ));
            }

            Client client = clientOpt.get();

            emailService.sendPaymentReminder(
                    client,
                    reminderDTO.getServiceName(),
                    reminderDTO.getAmount(),
                    reminderDTO.getCurrency(),
                    reminderDTO.getDueDate(),
                    reminderDTO.isLate()
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Email de rappel de paiement envoyé avec succès à " + client.getUsername()
            ));

        } catch (EmailException e) {
            log.error("Erreur lors de l'envoi du rappel de paiement", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Erreur lors de l'envoi de l'email: " + e.getMessage()
            ));
        }
    }

    /**
     * Endpoint pour envoyer une confirmation de résiliation à un client
     */
    @PostMapping("/cancellation")
    public ResponseEntity<?> sendCancellationConfirmation(@RequestBody EmailCancellationDTO cancellationDTO) {
        log.info("Demande d'envoi de confirmation de résiliation pour le client ID: {}", cancellationDTO.getClientId());

        try {
            Optional<Client> clientOpt = clientRepository.findById(UUID.fromString(cancellationDTO.getClientId()));

            if (clientOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Client non trouvé avec l'ID: " + cancellationDTO.getClientId()
                ));
            }

            Client client = clientOpt.get();

            // Mettre à jour le statut du contrat
            client.setContractStatus("CANCELLED");
            clientRepository.save(client);

            emailService.sendCancellationConfirmation(
                    client,
                    cancellationDTO.getServiceName(),
                    cancellationDTO.getEffectiveDate(),
                    cancellationDTO.getEndDate(),
                    cancellationDTO.getReference()
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Email de confirmation de résiliation envoyé avec succès à " + client.getUsername()
            ));

        } catch (EmailException e) {
            log.error("Erreur lors de l'envoi de la confirmation de résiliation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Erreur lors de l'envoi de l'email: " + e.getMessage()
            ));
        }
    }

    /**
     * Endpoint pour tester si le service d'email fonctionne correctement
     */
    @GetMapping("/status")
    public ResponseEntity<?> getEmailServiceStatus() {
        return ResponseEntity.ok(Map.of(
                "status", "active",
                "message", "Service d'email opérationnel"
        ));
    }

    /**
     * Endpoint pour envoyer un email de pré-inscription à un client
     */
    @PostMapping("/pre-inscription")
    public ResponseEntity<?> sendPreInscriptionEmail(@RequestBody EmailPreInscriptionDTO preInscriptionDTO) {
        log.info("Demande d'envoi d'email de pré-inscription pour le client ID: {}", preInscriptionDTO.getClientId());

        try {
            Optional<Client> clientOpt = clientRepository.findById(UUID.fromString(preInscriptionDTO.getClientId()));

            if (clientOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Client non trouvé avec l'ID: " + preInscriptionDTO.getClientId()
                ));
            }

            Client client = clientOpt.get();

            // ✅ Utiliser la méthode existante mais avec token au lieu de password
            emailService.sendAccountActivationEmail(client, preInscriptionDTO.getActivationToken());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Email de pré-inscription envoyé avec succès à " + client.getUsername()
            ));

        } catch (EmailException e) {
            log.error("Erreur lors de l'envoi de l'email de pré-inscription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Erreur lors de l'envoi de l'email: " + e.getMessage()
            ));
        }
    }
}