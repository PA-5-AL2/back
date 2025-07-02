package esgi.easisell.service;

import esgi.easisell.entity.Client;
import esgi.easisell.entity.User;
import esgi.easisell.exception.EmailException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public interface EmailService {
    void sendPreRegistrationEmail(User user, String rawPassword) throws EmailException;
    void sendPaymentReminder(Client client, String serviceName, BigDecimal amount,
                             String currency, LocalDate dueDate, boolean isLate) throws EmailException;
    void sendCancellationConfirmation(Client client, String serviceName,
                                      LocalDate effectiveDate, LocalDate endDate,
                                      String reference) throws EmailException;
    void sendHtmlEmail(String to, String subject, String templateName,
                       Map<String, Object> variables) throws EmailException;

    void sendAccountActivationEmail(User user, String activationToken) throws EmailException;
}