package cbms_backend.core.exception;

public class CustomerAddressAlreadyInactiveException extends RuntimeException {
    public CustomerAddressAlreadyInactiveException(String message) {
        super(message);
    }
}
