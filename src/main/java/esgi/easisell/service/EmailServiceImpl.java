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

    @Value("${spring.mail.username:no-reply@easisell.com}")
    private String fromEmail;

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
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            log.debug("Envoi de l'email à: {}", to);
            mailSender.send(message);
            log.info("Email envoyé avec succès à: {}", to);
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
    public void sendAccountActivationEmail(User user, String activationToken) throws EmailException {
        log.info("Préparation de l'email d'activation pour: {}", user.getUsername());
        try {
            String frontendUrl = env.getProperty("app.frontend.url", "http://localhost:3000");
            String activationUrl = env.getProperty("app.activation.url", frontendUrl + "/activate")
                    + "?token=" + activationToken;

            Map<String, Object> variables = new HashMap<>();
            variables.put("user", user);
            variables.put("activationToken", activationToken);
            variables.put("activationUrl", activationUrl);
            variables.put("logoUrl", env.getProperty("app.logo.url", "https://via.placeholder.com/150"));
            variables.put("contactUrl", env.getProperty("app.contact.url", frontendUrl + "/contact"));
            variables.put("termsUrl", env.getProperty("app.terms.url", frontendUrl + "/terms"));

            sendHtmlEmail(user.getUsername(),
                    "Activez votre compte EasiSell",
                    "emails/client/pre-inscription",
                    variables);

            log.info("Email d'activation envoyé à: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Échec envoi email d'activation à: {}", user.getUsername(), e);
            throw new EmailException("Échec envoi email d'activation à: " + user.getUsername(), e);
        }
    }
}