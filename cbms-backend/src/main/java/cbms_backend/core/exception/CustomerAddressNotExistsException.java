package cbms_backend.core.exception;

public class CustomerAddressNotExistsException extends RuntimeException {
    public CustomerAddressNotExistsException(String message) {
        super(message);
    }
}
