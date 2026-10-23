package cbms_backend.core.exception;

public class OrderNotExistsException extends RuntimeException {
    public OrderNotExistsException(String message) {
        super(message);
    }
}
