package cbms_backend.core.exception;

public class OrderAssignmentAlreadyExistsException extends RuntimeException {
    public OrderAssignmentAlreadyExistsException(String message) {
        super(message);
    }
}
