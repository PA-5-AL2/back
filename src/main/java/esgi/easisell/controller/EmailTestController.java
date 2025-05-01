package esgi.easisell.controller;

import esgi.easisell.entity.Client;
import esgi.easisell.entity.User;
import esgi.easisell.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/test/emails")
@RequiredArgsConstructor
@Slf4j
public class EmailTestController {

    private final EmailService emailService;

    @GetMapping("/pre-registration")
    public ResponseEntity<String> testPreRegistrationEmail(@RequestParam(defaultValue = "test@example.com") String email) {
        try {
            log.info("Test d'envoi d'email de pré-inscription à: {}", email);

            User user = new Client();
            user.setUserId(UUID.randomUUID());
            user.setUsername(email);
            user.setFirstName("TestUser");
            user.setRole("CLIENT");
            user.setCreatedAt(LocalDateTime.now());

            emailService.sendPreRegistrationEmail(user, "testPassword123");

            return ResponseEntity.ok("Email de pré-inscription envoyé avec succès à: " + email);
        } catch (Exception e) {
            log.error("Erreur lors du test d'envoi d'email de pré-inscription", e);
            return ResponseEntity.internalServerError()
                    .body("Erreur: " + e.getMessage());
        }
    }

    @GetMapping("/payment-reminder")
    public ResponseEntity<String> testPaymentReminder(@RequestParam(defaultValue = "test@example.com") String email) {
        try {
            log.info("Test d'envoi d'email de rappel de paiement à: {}", email);

            Client client = new Client();
            client.setUserId(UUID.randomUUID());
            client.setUsername(email);
            client.setFirstName("Jean");
            client.setContractStatus("ACTIVE");

            emailService.sendPaymentReminder(
                    client,
                    "Abonnement Premium",
                    new BigDecimal("29.99"),
                    "EUR",
                    LocalDate.now().plusDays(7),
                    false
            );

            return ResponseEntity.ok("Email de rappel de paiement envoyé avec succès à: " + email);
        } catch (Exception e) {
            log.error("Erreur lors du test d'envoi d'email de rappel de paiement", e);
            return ResponseEntity.internalServerError()
                    .body("Erreur: " + e.getMessage());
        }
    }

    @GetMapping("/payment-reminder-late")
    public ResponseEntity<String> testLatePaymentReminder(@RequestParam(defaultValue = "test@example.com") String email) {
        try {
            log.info("Test d'envoi d'email de rappel de paiement en retard à: {}", email);

            Client client = new Client();
            client.setUserId(UUID.randomUUID());
            client.setUsername(email);
            client.setFirstName("Pierre");
            client.setContractStatus("ACTIVE");

            emailService.sendPaymentReminder(
                    client,
                    "Abonnement Standard",
                    new BigDecimal("19.99"),
                    "EUR",
                    LocalDate.now().minusDays(3), // Date dépassée
                    true
            );

            return ResponseEntity.ok("Email de rappel de paiement en retard envoyé avec succès à: " + email);
        } catch (Exception e) {
            log.error("Erreur lors du test d'envoi d'email de rappel de paiement en retard", e);
            return ResponseEntity.internalServerError()
                    .body("Erreur: " + e.getMessage());
        }
    }

    @GetMapping("/cancellation")
    public ResponseEntity<String> testCancellation(@RequestParam(defaultValue = "test@example.com") String email) {
        try {
            log.info("Test d'envoi d'email de confirmation de résiliation à: {}", email);

            Client client = new Client();
            client.setUserId(UUID.randomUUID());
            client.setUsername(email);
            client.setFirstName("Marie");

            emailService.sendCancellationConfirmation(
                    client,
                    "Abonnement Standard",
                    LocalDate.now(),
                    LocalDate.now().plusMonths(1),
                    "RES-" + UUID.randomUUID().toString().substring(0, 8)
            );

            return ResponseEntity.ok("Email de confirmation de résiliation envoyé avec succès à: " + email);
        } catch (Exception e) {
            log.error("Erreur lors du test d'envoi d'email de confirmation de résiliation", e);
            return ResponseEntity.internalServerError()
                    .body("Erreur: " + e.getMessage());
        }
    }
}