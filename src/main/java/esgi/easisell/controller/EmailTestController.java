package esgi.easisell.controller;

import esgi.easisell.entity.Client;
import esgi.easisell.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/test/emails")
@RequiredArgsConstructor
public class EmailTestController {

    private final EmailService emailService;

    @GetMapping("/payment-reminder")
    public String testPaymentReminder() {
        try {
            Client client = new Client();
            client.setUsername("chancybeau@example.com");
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
            return "Email de rappel de paiement envoyé avec succès!";
        } catch (Exception e) {
            return "Erreur: " + e.getMessage();
        }
    }

    @GetMapping("/cancellation")
    public String testCancellation() {
        try {
            Client client = new Client();
            client.setUsername("chancybeau@example.com");
            client.setFirstName("Marie");

            emailService.sendCancellationConfirmation(
                    client,
                    "Abonnement Standard",
                    LocalDate.now(),
                    LocalDate.now().plusMonths(1),
                    "RES-123456"
            );
            return "Email de résiliation envoyé avec succès!";
        } catch (Exception e) {
            return "Erreur: " + e.getMessage();
        }
    }
}