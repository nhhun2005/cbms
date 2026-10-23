package cbms_backend.core.exception;

public class StockMovementNotExistsException extends RuntimeException {
    public StockMovementNotExistsException(String message) {
        super(message);
    }
}
