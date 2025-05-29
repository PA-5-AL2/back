package esgi.easisell.exception;

public class InvalidJwtTokenException extends JwtTokenException {
    public InvalidJwtTokenException(String message) {
        super("Token JWT invalide: " + message);
    }

    public InvalidJwtTokenException(String message, Throwable cause) {
        super("Token JWT invalide: " + message, cause);
    }
}
