package esgi.easisell.exception;

public class ProductNotBelongToClientException extends RuntimeException {
    public ProductNotBelongToClientException(String message) {
        super(message);
    }
}
