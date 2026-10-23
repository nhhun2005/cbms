package cbms_backend.core.exception;

public class InvoiceAlreadyExistsException extends RuntimeException {
    public InvoiceAlreadyExistsException(String message) {
        super(message);
    }
}
