package esgi.easisell.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) {
        super("Produit non trouvé: " + message);
    }
}