package esgi.easisell.exception;

import java.util.UUID;

public class SaleNotFoundException extends RuntimeException {
    public SaleNotFoundException(UUID saleId) {
        super("Vente non trouvée avec l'ID: " + saleId);
    }
}