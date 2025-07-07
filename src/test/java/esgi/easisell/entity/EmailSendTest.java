/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : EmailSendTest.java
 * @description : Tests unitaires pour l'entité EmailSend
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 03/07/2025
 * @package     : esgi.easisell.entity
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The type Email send test.
 */
class EmailSendTest {

    private EmailSend emailSend;
    private Email email;
    private Client client;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        emailSend = new EmailSend();

        email = new Email();
        email.setEmailId(UUID.randomUUID());
        email.setSubject("Test Email");

        client = new Client();
        client.setUserId(UUID.randomUUID());
        client.setName("Test Store");
    }

    /**
     * Test email send getters setters.
     */
    @Test
    @DisplayName("✅ Getters/Setters emailSend")
    void testEmailSendGettersSetters() {
        UUID emailSendId = UUID.randomUUID();
        Timestamp sentAt = Timestamp.valueOf(LocalDateTime.now());
        String status = "SENT";
        String emailType = "PROMOTION";

        emailSend.setEmailSendId(emailSendId);
        emailSend.setEmail(email);
        emailSend.setClient(client);
        emailSend.setSentAt(sentAt);
        emailSend.setStatus(status);
        emailSend.setEmailType(emailType);

        assertEquals(emailSendId, emailSend.getEmailSendId());
        assertEquals(email, emailSend.getEmail());
        assertEquals(client, emailSend.getClient());
        assertEquals(sentAt, emailSend.getSentAt());
        assertEquals(status, emailSend.getStatus());
        assertEquals(emailType, emailSend.getEmailType());
    }

    /**
     * Test required relations.
     */
    @Test
    @DisplayName("✅ Relations obligatoires")
    void testRequiredRelations() {
        emailSend.setEmail(email);
        emailSend.setClient(client);

        assertNotNull(emailSend.getEmail());
        assertNotNull(emailSend.getClient());
    }

    /**
     * Test valid statuses.
     */
    @Test
    @DisplayName("✅ Statuts d'envoi valides")
    void testValidStatuses() {
        String[] validStatuses = {"SENT", "FAILED", "PENDING"};

        for (String status : validStatuses) {
            emailSend.setStatus(status);
            assertEquals(status, emailSend.getStatus());
        }
    }

    /**
     * Test valid email types.
     */
    @Test
    @DisplayName("✅ Types d'email valides")
    void testValidEmailTypes() {
        String[] validTypes = {"WELCOME", "PROMOTION", "REMINDER", "NOTIFICATION"};

        for (String type : validTypes) {
            emailSend.setEmailType(type);
            assertEquals(type, emailSend.getEmailType());
        }
    }
}