package cbms_backend.core.exception;

public class CustomerAddressAlreadyActiveException extends RuntimeException {
    public CustomerAddressAlreadyActiveException(String message) {
        super(message);
    }
}
