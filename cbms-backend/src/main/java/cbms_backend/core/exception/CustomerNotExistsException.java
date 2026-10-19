package cbms_backend.core.exception;

public class CustomerNotExistsException extends RuntimeException {
    public CustomerNotExistsException(String message) {
        super(message);
    }
}
