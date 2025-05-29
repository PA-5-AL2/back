package esgi.easisell.exception;

public class JwtClaimExtractionException extends JwtTokenException {
    public JwtClaimExtractionException(String claimName) {
        super("Impossible d'extraire le claim '" + claimName + "' du token JWT");
    }

    public JwtClaimExtractionException(String claimName, Throwable cause) {
        super("Impossible d'extraire le claim '" + claimName + "' du token JWT", cause);
    }
}