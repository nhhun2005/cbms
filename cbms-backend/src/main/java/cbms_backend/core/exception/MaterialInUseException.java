package cbms_backend.core.exception;

public class MaterialInUseException extends RuntimeException {
    public MaterialInUseException(String message) {
        super(message);
    }
}
