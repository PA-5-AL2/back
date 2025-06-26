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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;

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