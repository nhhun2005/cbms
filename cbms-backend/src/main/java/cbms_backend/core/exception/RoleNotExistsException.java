package cbms_backend.core.exception;

public class RoleNotExistsException extends RuntimeException {
    public RoleNotExistsException(String message) {
        super(message);
    }
}
