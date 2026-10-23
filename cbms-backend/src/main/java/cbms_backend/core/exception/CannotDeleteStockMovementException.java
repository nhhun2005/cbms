package cbms_backend.core.exception;

public class CannotDeleteStockMovementException extends RuntimeException {
    public CannotDeleteStockMovementException(String message) {
        super(message);
    }
}
