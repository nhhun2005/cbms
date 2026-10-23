package cbms_backend.core.exception;

public class InvoiceNotExistsException extends RuntimeException {
    public InvoiceNotExistsException(String message) {
        super(message);
    }
}
