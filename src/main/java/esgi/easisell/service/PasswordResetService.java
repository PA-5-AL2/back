package esgi.easisell.service;

import esgi.easisell.entity.Client;
import esgi.easisell.entity.User;
import esgi.easisell.exception.EmailException;
import esgi.easisell.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : PasswordResetService.java
 * @description : Service pour la gestion des mots de passe oubliés
 * @author      : Votre nom
 * @version     : v1.0.0
 * @date        : 08/07/2025
 * @package     : esgi.easisell.service
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    // Emails des admins qui doivent être notifiés
    private static final String[] ADMIN_EMAILS = {
            "info@easy-sell.net",
            "samira@easy-sell.net"
    };

    /**
     * Traite une demande de réinitialisation de mot de passe
     *
     * @param email Email de l'utilisateur qui a oublié son mot de passe
     * @return true si l'email existe, false sinon
     */
    public boolean requestPasswordReset(String email) {
        log.info("🔍 Traitement demande réinitialisation pour: {}", email);

        try {
            // Chercher l'utilisateur par email
            User user = userRepository.findByUsername(email);

            if (user == null) {
                log.warn("Utilisateur non trouvé pour l'email: {}", email);
                return false;
            }

            sendPasswordResetRequest(user);

            // Envoyer email de notification aux admins
            notifyAdminsPasswordReset(user);

            log.info("Demande de réinitialisation traitée pour: {}", email);
            return true;

        } catch (Exception e) {
            log.error("Erreur lors du traitement de la demande de réinitialisation", e);
            return false;
        }
    }

    /**
     * Notifie l'utilisateur que son mot de passe a été changé
     *
     * @param email Email de l'utilisateur
     * @return true si l'email a été envoyé avec succès
     */
    public boolean notifyPasswordChanged(String email) {
        log.info("📧 Envoi notification changement mot de passe pour: {}", email);

        try {
            User user = userRepository.findByUsername(email);

            if (user == null) {
                log.warn("⚠️ Utilisateur non trouvé pour notification: {}", email);
                return false;
            }

            // Envoyer email de confirmation à l'utilisateur
            sendPasswordChangedConfirmation(user);

            log.info("Notification de changement envoyée à: {}", email);
            return true;

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de la notification", e);
            return false;
        }
    }

    /**
     * Notifie les admins qu'un utilisateur a demandé une réinitialisation
     */
    private void notifyAdminsPasswordReset(User user) {
        for (String adminEmail : ADMIN_EMAILS) {
            try {
                Map<String, Object> variables = new HashMap<>();
                variables.put("user", user);
                variables.put("adminEmail", adminEmail);
                variables.put("requestDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));
                variables.put("dashboardUrl", "https://www.easy-sell-esgi.com/admin/users");
                variables.put("userManagementUrl", "https://www.easy-sell-esgi.com/admin/users/" + user.getUserId());
                variables.put("logoUrl", "https://via.placeholder.com/200x80/4CAF50/FFFFFF?text=EasiSell");

                // Différencier le type d'utilisateur pour le template
                if (user instanceof Client client) {
                    variables.put("userType", "Client");
                    variables.put("clientName", client.getName());
                    variables.put("clientAddress", client.getAddress());
                } else {
                    variables.put("userType", "Administrateur");
                    variables.put("clientName", null);
                    variables.put("clientAddress", null);
                }

                emailService.sendHtmlEmail(
                        adminEmail,
                        "🔑 Demande de réinitialisation de mot de passe - " + user.getUsername(),
                        "emails/admin/password-reset-notification",
                        variables
                );

                log.info("Email envoyé à l'admin: {}", adminEmail);

            } catch (EmailException e) {
                log.error("Erreur envoi email admin: {}", adminEmail, e);
            }
        }
    }

    /**
     * Envoie un email de confirmation à l'utilisateur que son mot de passe a été changé
     */
    private void sendPasswordChangedConfirmation(User user) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("user", user);
            variables.put("changeDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));
            variables.put("loginUrl", "https://www.easy-sell-esgi.com/login");
            variables.put("supportEmail", "info@easy-sell.net");
            variables.put("logoUrl", "https://via.placeholder.com/200x80/4CAF50/FFFFFF?text=EasiSell");

            // Si c'est un client, ajouter des infos spécifiques
            if (user instanceof Client client) {
                variables.put("clientName", client.getName());
                variables.put("userType", "Client");
            } else {
                variables.put("clientName", null);
                variables.put("userType", "Administrateur");
            }

            emailService.sendHtmlEmail(
                    user.getUsername(),
                    "🔐 Confirmation de changement de mot de passe - EasiSell",
                    "emails/client/password-changed-confirmation",
                    variables
            );

        } catch (EmailException e) {
            log.error("Erreur envoi confirmation changement mot de passe", e);
            throw new RuntimeException("Erreur lors de l'envoi de l'email de confirmation", e);
        }
    }

    /**
     * Envoie un email de demande de réinitialisation à l'utilisateur
     */
    private void sendPasswordResetRequest(User user) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("user", user);
            variables.put("requestDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));
            variables.put("resetUrl", "https://www.easy-sell-esgi.com/reset-password?email=" + user.getUsername());
            variables.put("supportEmail", "info@easy-sell.net");
            variables.put("logoUrl", "https://via.placeholder.com/200x80/4CAF50/FFFFFF?text=EasiSell");

            // Si c'est un client, ajouter des infos spécifiques
            if (user instanceof Client client) {
                variables.put("clientName", client.getName());
                variables.put("userType", "Client");
            } else {
                variables.put("clientName", null);
                variables.put("userType", "Administrateur");
            }

            emailService.sendHtmlEmail(
                    user.getUsername(),
                    "Réinitialisation de votre mot de passe - EasiSell",
                    "emails/client/password-reset-request",
                    variables
            );

        } catch (EmailException e) {
            log.error("Erreur envoi email réinitialisation", e);
            throw new RuntimeException("Erreur lors de l'envoi de l'email de réinitialisation", e);
        }
    }
}