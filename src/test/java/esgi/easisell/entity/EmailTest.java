/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : EmailTest.java
 * @description : Tests unitaires pour l'entité Email
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

import java.util.UUID;

/**
 * The type Email test.
 */
class EmailTest {

    private Email email;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        email = new Email();
    }

    /**
     * Test email getters setters.
     */
    @Test
    @DisplayName("✅ Getters/Setters email")
    void testEmailGettersSetters() {
        UUID emailId = UUID.randomUUID();
        String subject = "Bienvenue chez EasiSell";
        String content = "Contenu de l'email de bienvenue";
        String type = "WELCOME";

        email.setEmailId(emailId);
        email.setSubject(subject);
        email.setContent(content);
        email.setType(type);

        assertEquals(emailId, email.getEmailId());
        assertEquals(subject, email.getSubject());
        assertEquals(content, email.getContent());
        assertEquals(type, email.getType());
    }

    /**
     * Test email sends management.
     */
    @Test
    @DisplayName("✅ Gestion des emailSends")
    void testEmailSendsManagement() {
        // Test initialisation par défaut
        assertNotNull(email.getEmailSends());
        assertTrue(email.getEmailSends().isEmpty());

        // Ajouter un emailSend
        EmailSend emailSend = new EmailSend();
        emailSend.setEmailSendId(UUID.randomUUID());
        email.getEmailSends().add(emailSend);

        assertEquals(1, email.getEmailSends().size());
        assertTrue(email.getEmailSends().contains(emailSend));
    }

    /**
     * Test required fields.
     */
    @Test
    @DisplayName("✅ Validation métier - champs obligatoires")
    void testRequiredFields() {
        email.setSubject("Sujet obligatoire");
        email.setContent("Contenu obligatoire");
        email.setType("PROMOTION");

        assertNotNull(email.getSubject());
        assertNotNull(email.getContent());
        assertNotNull(email.getType());
        assertFalse(email.getSubject().isEmpty());
        assertFalse(email.getContent().isEmpty());
    }

    /**
     * Test creation timestamp.
     */
    @Test
    @DisplayName("✅ CreationTimestamp automatique")
    void testCreationTimestamp() {
        // Le @CreationTimestamp est géré par Hibernate
        // On teste juste que le champ existe
        email.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));

        assertNotNull(email.getCreatedAt());
    }
}