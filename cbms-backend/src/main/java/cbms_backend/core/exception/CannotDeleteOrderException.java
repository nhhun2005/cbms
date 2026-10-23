package cbms_backend.core.exception;

public class CannotDeleteOrderException extends RuntimeException {
    public CannotDeleteOrderException(String message) {
        super(message);
    }
}
