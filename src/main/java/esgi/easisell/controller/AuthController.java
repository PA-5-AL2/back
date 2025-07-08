package esgi.easisell.controller;

import esgi.easisell.dto.AuthDTO;
import esgi.easisell.entity.User;
import esgi.easisell.service.AuthService;
import esgi.easisell.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : AuthController.java
 * @description : Contrôleur REST pour l'authentification et l'inscription
 * @author      : Samira SEDDAR
 * @version     : v1.0.0
 * @date        : 01/07/2025
 * @package     : esgi.easisell.controller
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 *
 * Ce contrôleur expose les endpoints publics pour :
 * - Inscription des nouveaux utilisateurs (clients et admins)
 * - Authentification et génération de tokens JWT
 * - Envoi automatique d'emails de bienvenue
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;

    /**
     * Inscrire un nouvel utilisateur dans le système
     * POST /api/auth/register
     *
     * Envoi automatique d'email de bienvenue pour les clients
     *
     * @param authDTO les données d'inscription (username, password, role, etc.)
     * @return l'utilisateur créé ou message d'erreur
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthDTO authDTO) {
        if (!authService.isUsernameAvailable(authDTO.getUsername())) {
            return ResponseEntity.badRequest().body("Username is already in use");
        }

        try {
            User registeredUser = authService.registerUser(authDTO);
            if ("client".equalsIgnoreCase(authDTO.getRole())) {
                emailService.sendPreRegistrationEmail(registeredUser, authDTO.getPassword());
            }
            return ResponseEntity.ok(registeredUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error during registration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating user: " + e.getMessage());
        }
    }

    /**
     * Authentifier un utilisateur et générer un token JWT
     * POST /api/auth/login
     *
     * Retourne un token JWT valide pour les requêtes authentifiées
     *
     * @param authDTO les identifiants de connexion (username, password)
     * @return token JWT et informations utilisateur ou erreur 401
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDTO authDTO) {
        try {
            return ResponseEntity.ok(authService.authenticateUser(authDTO));
        } catch (AuthenticationException e) {
            log.error("Authentication failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }
}