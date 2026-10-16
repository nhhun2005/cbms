package cbms_backend.core.exception;

public class InvitationTokenExpiredException extends RuntimeException {
    public InvitationTokenExpiredException(String message) {
        super(message);
    }
}
