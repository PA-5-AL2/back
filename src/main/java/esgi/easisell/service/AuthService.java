package esgi.easisell.service;

import esgi.easisell.configuration.JwtUtils;
import esgi.easisell.dto.AuthDTO;
import esgi.easisell.entity.AdminUser;
import esgi.easisell.entity.Client;
import esgi.easisell.entity.User;
import esgi.easisell.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    /**
     * Vérifie si un nom d'utilisateur est disponible
     */
    public boolean isUsernameAvailable(String username) {
        return userRepository.findByUsername(username) == null;
    }

    /**
     * Enregistre un nouvel utilisateur (Client ou Admin)
     */
    @Transactional
    public User registerUser(AuthDTO authDTO) {
        User user;

        if ("administrateur".equalsIgnoreCase(authDTO.getRole()) || "admin".equalsIgnoreCase(authDTO.getRole())) {
            user = new AdminUser();
            user.setRole("ADMIN");
        } else if ("client".equalsIgnoreCase(authDTO.getRole())) {
            Client client = new Client();
            client.setContractStatus(authDTO.getContractStatus());
            client.setCurrencyPreference(authDTO.getCurrencyPreference());
            client.setName(authDTO.getName());
            client.setAddress(authDTO.getAddress());

            user = client;
            user.setRole("CLIENT");
        } else {
            throw new IllegalArgumentException("Role must be 'administrateur' or 'client'");
        }

        user.setUsername(authDTO.getUsername());
        user.setPassword(passwordEncoder.encode(authDTO.getPassword()));
        user.setFirstName(authDTO.getUsername().split("@")[0]);

        return userRepository.save(user);
    }

    /**
     * Authentifie un utilisateur et retourne un token JWT
     */
    public Map<String, Object> authenticateUser(AuthDTO authDTO) {
        log.info("Tentative d'authentification pour: {}", authDTO.getUsername());

        User user = userRepository.findByUsername(authDTO.getUsername());
        if (user == null) {
            log.warn("Utilisateur non trouvé: {}", authDTO.getUsername());
            throw new BadCredentialsException("Utilisateur non trouvé");
        }

        if (!passwordEncoder.matches(authDTO.getPassword(), user.getPassword())) {
            log.warn("Mot de passe incorrect pour: {}", authDTO.getUsername());
            throw new BadCredentialsException("Mot de passe incorrect");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authDTO.getUsername(), authDTO.getPassword())
            );

            if (!authentication.isAuthenticated()) {
                throw new BadCredentialsException("Échec de l'authentification");
            }
        } catch (AuthenticationException e) {
            log.error("Erreur d'authentification pour {}: {}", authDTO.getUsername(), e.getMessage());
            throw new BadCredentialsException("Les identifications sont erronées");
        }

        log.info("Utilisateur authentifié: {} avec le rôle: {}", user.getUsername(), user.getRole());

        String token = jwtUtils.generateToken(
                user.getUsername(),
                user.getRole(),
                user.getUserId().toString()
        );

        Map<String, Object> authData = new HashMap<>();
        authData.put("token", token);
        authData.put("type", "Bearer");
        authData.put("role", user.getRole());
        authData.put("userId", user.getUserId());

        log.info("Token généré avec succès pour: {}", user.getUsername());
        return authData;
    }
}