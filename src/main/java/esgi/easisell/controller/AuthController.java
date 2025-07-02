package esgi.easisell.controller;

import esgi.easisell.dto.ActivationDTO;
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

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthDTO authDTO) {
        if (!authService.isUsernameAvailable(authDTO.getUsername())) {
            return ResponseEntity.badRequest().body("Username is already in use");
        }

        try {
            Map<String, Object> registrationResult = authService.registerUserWithActivation(authDTO);
            return ResponseEntity.ok(registrationResult);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error during registration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating user: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDTO authDTO) {
        try {
            return ResponseEntity.ok(authService.authenticateUser(authDTO));
        } catch (AuthenticationException e) {
            log.error("Authentication failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    @PostMapping("/activate")
    public ResponseEntity<?> activateAccount(@RequestBody ActivationDTO activationDTO) {
        try {
            authService.activateAccount(activationDTO.getToken(), activationDTO.getPassword());
            return ResponseEntity.ok(Map.of("message", "Compte activé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}