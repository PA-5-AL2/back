package esgi.easisell.controller;

import esgi.easisell.dto.PasswordResetRequestDTO;
import esgi.easisell.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : PasswordResetController.java
 * @description : Contrôleur REST pour la gestion des mots de passe oubliés
 * @author      : Votre nom
 * @version     : v1.0.0
 * @date        : 08/07/2025
 * @package     : esgi.easisell.controller
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
@RestController
@RequestMapping("/api/password-reset")
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * ENDPOINT PUBLIC - Demande de réinitialisation de mot de passe
     * Le client Flutter appelle cette API quand l'utilisateur clique sur "Mot de passe oublié"
     *
     * POST /api/password-reset/request
     */
    @PostMapping("/request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody PasswordResetRequestDTO requestDTO) {
        try {
            log.info("🔑 Demande de réinitialisation de mot de passe pour: {}", requestDTO.getEmail());

            boolean success = passwordResetService.requestPasswordReset(requestDTO.getEmail());

            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Si votre email existe dans notre système, vous recevrez des instructions pour réinitialiser votre mot de passe."
                ));
            } else {
                // Pour la sécurité, on retourne toujours le même message
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Si votre email existe dans notre système, vous recevrez des instructions pour réinitialiser votre mot de passe."
                ));
            }

        } catch (Exception e) {
            log.error("Erreur lors de la demande de réinitialisation pour: {}", requestDTO.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Une erreur technique est survenue. Veuillez réessayer."
            ));
        }
    }

    /**
     * ENDPOINT PUBLIC - Confirmation que le mot de passe a été changé par l'utilisateur
     * Appelé par le frontend quand l'utilisateur change son mot de passe depuis son profil
     *
     * POST /api/password-reset/changed-notification
     */
    @PostMapping("/changed-notification")
    public ResponseEntity<?> notifyPasswordChanged(@RequestBody PasswordResetRequestDTO requestDTO) {
        try {
            log.info("📧 Notification de changement de mot de passe pour: {}", requestDTO.getEmail());

            boolean success = passwordResetService.notifyPasswordChanged(requestDTO.getEmail());

            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Email de confirmation envoyé"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Utilisateur non trouvé"
                ));
            }

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de notification de changement", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Erreur lors de l'envoi de la notification"
            ));
        }
    }

    /**
     * Test endpoint pour vérifier le service
     */
    @GetMapping("/status")
    public ResponseEntity<?> getPasswordResetStatus() {
        return ResponseEntity.ok(Map.of(
                "status", "active",
                "message", "Service de réinitialisation de mot de passe opérationnel"
        ));
    }
}