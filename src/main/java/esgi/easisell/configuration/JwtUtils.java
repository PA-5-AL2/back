package esgi.easisell.configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * The type Jwt utils.
 */
@Component
@Slf4j
public class JwtUtils {

    @Value("${app.jwtSecret}")
    private String secretJwt;

    @Value("${app.jwtExpirationMs}")
    private Long expirationJwt;

    /**
     * Génération du token avec informations minimales
     *
     * @param username the username
     * @param role     the role
     * @param userId   the user id
     * @return the string
     */
    public String generateToken(String username, String role, String userId) {
        log.debug("Génération du token pour l'utilisateur: {}", username);

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("userId", userId);

        return createToken(claims, username);
    }

    /**
     * Génération du token simple
     *
     * @param username the username
     * @return the string
     */
    public String generateToken(String username) {
        log.debug("Génération du token simple pour: {}", username);
        return createToken(new HashMap<>(), username);
    }

    /**
     * Validation du token
     *
     * @param token       the token
     * @param userDetails the user details
     * @return the boolean
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("Token invalide lors de la validation: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extraction du nom d'utilisateur
     *
     * @param token the token
     * @return the string
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extraction du rôle utilisateur
     *
     * @param token the token
     * @return the string
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extraction de l'ID utilisateur
     *
     * @param token the token
     * @return the string
     */
    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    /**
     * Création du token JWT plus court
     */
    private String createToken(Map<String, Object> claims, String subject) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationJwt))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Génération de la clé de signature
     */
    private Key getSigningKey() {
        byte[] keyBytes = secretJwt.getBytes();
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    /**
     * Extraction générique d'un claim
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extraction de tous les claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Vérification de l'expiration du token
     */
    private boolean isTokenExpired(String token) {
        return extractExpirationDate(token).before(new Date());
    }

    /**
     * Extraction de la date d'expiration
     */
    private Date extractExpirationDate(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}