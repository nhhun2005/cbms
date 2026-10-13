package cbms_backend.core.exception;

public class UserAlreadyInactiveException extends RuntimeException {
    public UserAlreadyInactiveException(String message) {
        super(message);
    }
}
