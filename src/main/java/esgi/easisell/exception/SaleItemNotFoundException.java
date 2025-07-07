package esgi.easisell.exception;

import java.util.UUID;

public class SaleItemNotFoundException extends RuntimeException {
    public SaleItemNotFoundException(UUID saleItemId) {
        super("Article de vente non trouvé avec l'ID: " + saleItemId);
    }
}