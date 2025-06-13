package esgi.easisell.exception;

import java.util.UUID;

public class ClientNotFoundException extends RuntimeException {
    public ClientNotFoundException(UUID clientId) {
        super("Client non trouvé avec l'ID: " + clientId);
    }

    public ClientNotFoundException(String message) {
        super(message);
    }
}