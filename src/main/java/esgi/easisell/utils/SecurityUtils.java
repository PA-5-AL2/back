package esgi.easisell.utils;

import esgi.easisell.configuration.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityUtils {

    private final JwtUtils jwtUtils;

    /**
     * Vérifie si l'utilisateur actuel est un admin
     */
    public boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ADMIN"));
    }

    /**
     * Récupère l'ID de l'utilisateur actuel depuis le JWT
     */
    public String getCurrentUserId(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        return token != null ? jwtUtils.extractUserId(token) : null;
    }

    /**
     * Vérifie si l'utilisateur actuel peut accéder aux données du client spécifié
     */
    public boolean canAccessClientData(UUID clientId, HttpServletRequest request) {
        log.debug("Vérification d'accès pour le client ID: {}", clientId);

        // Les admins peuvent accéder à tout
        if (isCurrentUserAdmin()) {
            log.debug("Accès autorisé - utilisateur admin");
            return true;
        }

        // Les clients ne peuvent accéder qu'à leurs propres données
        String currentUserId = getCurrentUserId(request);

        if (currentUserId == null) {
            log.warn("Token JWT invalide ou manquant");
            return false;
        }

        boolean canAccess = currentUserId.equals(clientId.toString());

        if (canAccess) {
            log.debug("Accès autorisé - utilisateur accède à ses propres données");
        } else {
            log.warn("Accès refusé - utilisateur {} tente d'accéder aux données du client {}",
                    currentUserId, clientId);
        }

        return canAccess;
    }

    /**
     * Extrait le token JWT de la requête
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " = 7 caractères
        }
        return null;
    }
}