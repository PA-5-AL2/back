package esgi.easisell.service;

import esgi.easisell.entity.Client;
import esgi.easisell.entity.User;
import esgi.easisell.exception.EmailException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final Environment env;

    @Value("${spring.mail.username:easysell@gmail.com}")
    private String fromEmail;

    @Value("${spring.mail.from.name:EasiSell Support}")
    private String fromName;

    @Async
    @Override
    public void sendPreRegistrationEmail(User user, String rawPassword) throws EmailException {
        log.info("Préparation de l'email de pré-inscription pour: {}", user.getUsername());

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("user", user);
            variables.put("tempPassword", rawPassword);

            if (user instanceof Client client) {
                String accessCode = client.getAccessCode();
                variables.put("accessCode", accessCode);
                log.info("Code d'accès du client ajouté: '{}'", accessCode);
            } else {
                variables.put("accessCode", null);
                log.info("Utilisateur n'est pas un Client, pas de code d'accès");
            }

            // Vérification des valeurs null pour les URLs
            String frontendUrl = env.getProperty("app.frontend.url", "https://www.easy-sell-esgi.com");
            variables.put("loginUrl", frontendUrl + "/login");
            variables.put("contactUrl", env.getProperty("app.contact.url", "info@easy-sell.net"));
            variables.put("termsUrl", env.getProperty("app.terms.url", frontendUrl + "/terms"));
            variables.put("logoUrl", env.getProperty("app.logo.url", "https://via.placeholder.com/200x80/4CAF50/FFFFFF?text=EasiSell"));

            log.info("Variables envoyées au template:");
            log.info("   - accessCode: '{}'", variables.get("accessCode"));

            sendHtmlEmail(user.getUsername(),
                    "Bienvenue sur EasiSell - Votre compte est activé !",
                    "emails/client/pre-inscription",
                    variables);

            log.info("Email de pré-inscription envoyé à: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Échec de l'envoi de l'email de pré-inscription à: {}", user.getUsername(), e);
            throw new EmailException("Échec de l'envoi de l'email de pré-inscription à: " + user.getUsername(), e);
        }
    }

    @Async
    @Override
    public void sendPaymentReminder(Client client, String serviceName, BigDecimal amount,
                                    String currency, LocalDate dueDate, boolean isLate) throws EmailException {
        log.info("Préparation du rappel de paiement pour: {} (service: {})", client.getUsername(), serviceName);
        try {
            String frontendUrl = env.getProperty("app.frontend.url", "http://localhost:3000");

            Map<String, Object> variables = new HashMap<>();
            variables.put("client", client);
            variables.put("user", client); // Pour compatibilité avec les templates
            variables.put("serviceName", serviceName);
            variables.put("amount", amount);
            variables.put("currency", currency);
            variables.put("dueDate", dueDate);
            variables.put("isLate", isLate);
            variables.put("paymentUrl", env.getProperty("app.payment.url", frontendUrl + "/payment"));
            variables.put("cancellationUrl", env.getProperty("app.cancellation.url", frontendUrl + "/cancel"));
            variables.put("logoUrl", env.getProperty("app.logo.url", "https://via.placeholder.com/150"));

            sendHtmlEmail(client.getUsername(),
                    "Rappel de paiement pour votre service " + serviceName,
                    "emails/client/rappel-paiement",
                    variables);

            log.info("Email de rappel de paiement envoyé à: {}", client.getUsername());
        } catch (Exception e) {
            log.error("Échec de l'envoi du rappel de paiement à: {}", client.getUsername(), e);
            throw new EmailException("Échec de l'envoi du rappel de paiement à: " + client.getUsername(), e);
        }
    }

    @Async
    @Override
    public void sendCancellationConfirmation(Client client, String serviceName,
                                             LocalDate effectiveDate, LocalDate endDate,
                                             String reference) throws EmailException {
        log.info("Préparation de la confirmation de résiliation pour: {} (service: {})", client.getUsername(), serviceName);
        try {
            String frontendUrl = env.getProperty("app.frontend.url", "http://localhost:3000");

            Map<String, Object> variables = new HashMap<>();
            variables.put("client", client);
            variables.put("user", client); // Pour compatibilité avec les templates
            variables.put("serviceName", serviceName);
            variables.put("effectiveDate", effectiveDate);
            variables.put("endDate", endDate);
            variables.put("reference", reference);
            variables.put("reactivationUrl", env.getProperty("app.reactivation.url", frontendUrl + "/reactivate"));
            variables.put("logoUrl", env.getProperty("app.logo.url", "https://via.placeholder.com/150"));

            sendHtmlEmail(client.getUsername(),
                    "Confirmation de résiliation de votre abonnement",
                    "emails/client/resiliation",
                    variables);

            log.info("Email de confirmation de résiliation envoyé à: {}", client.getUsername());
        } catch (Exception e) {
            log.error("Échec de l'envoi de la confirmation de résiliation à: {}", client.getUsername(), e);
            throw new EmailException("Échec de l'envoi de la confirmation de résiliation à: " + client.getUsername(), e);
        }
    }

    @Async
    @Override
    public void sendHtmlEmail(String to, String subject, String templateName,
                              Map<String, Object> variables) throws EmailException {
        log.debug("Préparation de l'email HTML vers: {} (sujet: {}, template: {})", to, subject, templateName);
        try {
            Context context = new Context();
            context.setVariables(variables);

            String content = templateEngine.process(templateName, context);
            if (content == null || content.isEmpty()) {
                throw new EmailException("Le contenu de l'email généré est vide pour le template: " + templateName);
            }

            log.debug("Contenu de l'email généré avec succès, taille: {} caractères", content.length());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Amélioration: utiliser setFrom avec nom et email
            try {
                helper.setFrom(fromEmail, fromName);
            } catch (Exception e) {
                // Fallback si le nom pose problème
                helper.setFrom(fromEmail);
            }

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            log.debug("Envoi de l'email depuis: {} vers: {}", fromEmail, to);
            mailSender.send(message);
            log.info("Email envoyé avec succès depuis {} vers: {}", fromEmail, to);
        } catch (MessagingException e) {
            log.error("Erreur lors de la création/envoi de l'email à: {}", to, e);
            throw new EmailException("Erreur lors de la création/envoi de l'email à: " + to, e);
        } catch (Exception e) {
            log.error("Erreur inattendue lors de l'envoi de l'email à: {}", to, e);
            throw new EmailException("Erreur inattendue lors de l'envoi de l'email à: " + to, e);
        }
    }

    @Async
    @Override
    public void sendEmployeeAccessRequest(Client client, String employeeName, String employeeEmail) throws EmailException {
        log.info("Préparation de l'email de demande d'accès employé pour le client: {}", client.getUsername());

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("client", client);
            variables.put("employeeName", employeeName);
            variables.put("employeeEmail", employeeEmail);
            variables.put("supportEmail", "info@easy-sell.net");
            variables.put("logoUrl", "https://via.placeholder.com/200x80/4CAF50/FFFFFF?text=EasiSell");

            // URL pour gérer la demande (optionnel)
            String frontendUrl = env.getProperty("app.frontend.url", "https://www.easy-sell-esgi.com");
            variables.put("manageUrl", frontendUrl + "/client/employee-access");

            sendHtmlEmail(
                    client.getUsername(),
                    "Demande d'accès employé - " + employeeName,
                    "emails/client/employee-access-request",
                    variables
            );

            log.info("Email de demande d'accès employé envoyé avec succès à: {}", client.getUsername());

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de demande d'accès employé", e);
            throw new EmailException("Erreur lors de l'envoi de l'email de demande d'accès employé", e);
        }
    }
}