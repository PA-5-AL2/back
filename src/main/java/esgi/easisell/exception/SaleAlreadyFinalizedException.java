package esgi.easisell.exception;

public class SaleAlreadyFinalizedException extends RuntimeException {
    public SaleAlreadyFinalizedException(String message) {
        super(message);
    }
}
