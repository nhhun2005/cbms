package cbms_backend.core.exception;

public class MaterialAlreadyExistsException extends RuntimeException {
    public MaterialAlreadyExistsException(String message) {
        super(message);
    }
}
