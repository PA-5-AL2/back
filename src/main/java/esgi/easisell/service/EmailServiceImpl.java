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
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final Environment env;

    @Async
    @Override
    public void sendPreRegistrationEmail(User user, String tempPassword) throws EmailException {
        try {
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("tempPassword", tempPassword);
            context.setVariable("loginUrl", env.getProperty("app.frontend.url") + "/login");
            context.setVariable("contactUrl", env.getProperty("app.contact.url"));
            context.setVariable("termsUrl", env.getProperty("app.terms.url"));

            String content = templateEngine.process("emails/client/pre-inscription", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(user.getUsername());
            helper.setSubject("Bienvenue sur EasiSell - Inscription confirmée");
            helper.setText(content, true);

            mailSender.send(message);

        } catch (Exception e) {
            throw new EmailException("Failed to send pre-registration email to " + user.getUsername(), e);
        }
    }

    @Async
    @Override
    public void sendPaymentReminder(Client client, String serviceName, BigDecimal amount,
                                    String currency, LocalDate dueDate, boolean isLate) throws EmailException {
        try {
            Context context = new Context();
            context.setVariable("user", client);
            context.setVariable("serviceName", serviceName);
            context.setVariable("amount", amount);
            context.setVariable("currency", currency);
            context.setVariable("dueDate", dueDate);
            context.setVariable("latePayment", isLate);
            context.setVariable("paymentUrl", env.getProperty("app.payment.url"));
            context.setVariable("cancellationUrl", env.getProperty("app.cancellation.url"));

            String content = templateEngine.process("emails/client/rappel-paiement", context);

            sendHtmlEmail(client.getUsername(), "EasiSell - Rappel de paiement", content);

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
            Context context = new Context();
            context.setVariable("user", client);
            context.setVariable("serviceName", serviceName);
            context.setVariable("effectiveDate", effectiveDate);
            context.setVariable("endDate", endDate);
            context.setVariable("reference", reference);
            context.setVariable("reactivationUrl", env.getProperty("app.reactivation.url"));

            String content = templateEngine.process("emails/client/resiliation", context);

            sendHtmlEmail(client.getUsername(), "EasiSell - Confirmation de résiliation", content);

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
            variables.forEach(context::setVariable);

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

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}