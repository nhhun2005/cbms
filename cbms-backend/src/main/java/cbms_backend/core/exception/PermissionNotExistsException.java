package cbms_backend.core.exception;

public class PermissionNotExistsException extends RuntimeException {
    public PermissionNotExistsException(String message) {
        super(message);
    }
}
