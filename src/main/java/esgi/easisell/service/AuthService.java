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

    public boolean isUsernameAvailable(String username) {
        return userRepository.findByUsername(username) == null;
    }

    @Transactional
    public User registerUser(AuthDTO authDTO) throws Exception {
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

        return userRepository.save(user);
    }

    public Map<String, Object> authenticateUser(AuthDTO authDTO) throws AuthenticationException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authDTO.getUsername(), authDTO.getPassword())
        );

        if (authentication.isAuthenticated()) {
            User user = userRepository.findByUsername(authDTO.getUsername());

            Map<String, Object> authData = new HashMap<>();
            authData.put("token", jwtUtils.generateToken(authDTO.getUsername()));
            authData.put("type", "Bearer");
            authData.put("role", user.getRole());

            return authData;
        }

        throw new AuthenticationException("Authentication failed") {};
    }
}