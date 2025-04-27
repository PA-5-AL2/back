package esgi.easisell.controller;

import esgi.easisell.configuration.JwtUtils;
import esgi.easisell.dto.AuthDTO;
import esgi.easisell.entity.AdminUser;
import esgi.easisell.entity.Client;
import esgi.easisell.entity.User;
import esgi.easisell.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthDTO authDTO) {
        if (userRepository.findByUsername(authDTO.getUsername()) != null) {
            return ResponseEntity.badRequest().body("Username is already in use");
        }
        User user;
        if ("administrateur".equalsIgnoreCase(authDTO.getRole()) || "admin".equalsIgnoreCase(authDTO.getRole())) {
            user = new AdminUser();
            user.setRole("ADMIN");
        } else if ("client".equalsIgnoreCase(authDTO.getRole())) {
            Client client = new Client();
            client.setContractStatus(authDTO.getContractStatus());
            client.setCurrencyPreference(authDTO.getCurrencyPreference());

            user = client;
            user.setRole("CLIENT");
        } else {
            return ResponseEntity.badRequest().body("Role must be 'administrateur' or 'client'");
        }
        user.setUsername(authDTO.getUsername());
        user.setPassword(passwordEncoder.encode(authDTO.getPassword()));

        try {
            return ResponseEntity.ok(userRepository.save(user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating user: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDTO authDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authDTO.getUsername(), authDTO.getPassword())
            );
            if (authentication.isAuthenticated()) {
                User user = userRepository.findByUsername(authDTO.getUsername());

                Map<String, Object> authData = new HashMap<>();
                authData.put("token", jwtUtils.generateToken(authDTO.getUsername()));
                authData.put("type", "Bearer");
                authData.put("role", user.getRole());

                return ResponseEntity.ok(authData);
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        } catch (AuthenticationException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }
}