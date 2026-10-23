package cbms_backend.core.exception;

public class MaterialNotExistsException extends RuntimeException {
    public MaterialNotExistsException(String message) {
        super(message);
    }
}
