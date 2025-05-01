package esgi.easisell.service;

import esgi.easisell.entity.Client;
import esgi.easisell.entity.User;
import esgi.easisell.exception.EmailException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
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
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final Environment env;

    @Async
    @Override
    public void sendPreRegistrationEmail(User user, String rawPassword) throws EmailException {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("user", user);
            variables.put("tempPassword", rawPassword);

            // Vérification des valeurs null pour les URLs
            String frontendUrl = env.getProperty("app.frontend.url");
            variables.put("loginUrl", frontendUrl != null ? frontendUrl + "/login" : "#");
            variables.put("contactUrl", env.getProperty("app.contact.url", "#"));
            variables.put("termsUrl", env.getProperty("app.terms.url", "#"));

            sendHtmlEmail(user.getUsername(),
                    "Bienvenue sur EasiSell - Inscription confirmée",
                    "emails/client/pre-inscription",
                    variables);
        } catch (Exception e) {
            throw new EmailException("Failed to send pre-registration email to " + user.getUsername(), e);
        }
    }

    @Async
    @Override
    public void sendPaymentReminder(Client client, String serviceName, BigDecimal amount,
                                    String currency, LocalDate dueDate, boolean isLate) throws EmailException {
        try {
            Map<String, Object> variables = Map.of(
                    "client", client,
                    "serviceName", serviceName,
                    "amount", amount,
                    "currency", currency,
                    "dueDate", dueDate,
                    "isLate", isLate,
                    "paymentUrl", env.getProperty("app.payment.url")
            );

            sendHtmlEmail(client.getUsername(),
                    "Rappel de paiement pour votre service " + serviceName,
                    "emails/payment-reminder",
                    variables);
        } catch (Exception e) {
            throw new EmailException("Failed to send payment reminder to " + client.getUsername(), e);
        }
    }

    @Async
    @Override
    public void sendCancellationConfirmation(Client client, String serviceName,
                                             LocalDate effectiveDate, LocalDate endDate,
                                             String reference) throws EmailException {
        try {
            Map<String, Object> variables = Map.of(
                    "client", client,
                    "serviceName", serviceName,
                    "effectiveDate", effectiveDate,
                    "endDate", endDate,
                    "reference", reference
            );

            sendHtmlEmail(client.getUsername(),
                    "Confirmation de résiliation de votre abonnement",
                    "emails/cancellation-confirmation",
                    variables);
        } catch (Exception e) {
            throw new EmailException("Failed to send cancellation confirmation to " + client.getUsername(), e);
        }
    }

    @Async
    @Override
    public void sendHtmlEmail(String to, String subject, String templateName,
                              Map<String, Object> variables) throws EmailException {
        try {
            Context context = new Context();
            context.setVariables(variables);

            String content = templateEngine.process(templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);
        } catch (Exception e) {
            throw new EmailException("Failed to send HTML email to " + to, e);
        }
    }
}