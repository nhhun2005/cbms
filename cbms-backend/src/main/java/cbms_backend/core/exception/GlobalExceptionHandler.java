package cbms_backend.core.exception;

import cbms_backend.core.util.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserAlreadyExists( UserAlreadyExistsException ex){
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex){
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("Validation failed");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
    }
    @ExceptionHandler(UserNotExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotExists(UserNotExistsException ex){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }
    @ExceptionHandler(InvalidUserCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidUserCredentials(InvalidUserCredentialsException ex){
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(UserAlreadyInactiveException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserAlreadyInactive(UserAlreadyInactiveException ex){
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(UserAlreadyActiveException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserAlreadyActive(UserAlreadyActiveException ex){
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }


    @ExceptionHandler(PermissionAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handlePermissionAlreadyExists(PermissionAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(RoleAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleRoleAlreadyExists(RoleAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(RoleInUseException.class)
    public ResponseEntity<ApiResponse<Void>> handleRoleInUse(RoleInUseException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(PermissionInUseException.class)
    public ResponseEntity<ApiResponse<Void>> handlePermissionInUse(PermissionInUseException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }


    @ExceptionHandler(PermissionNotExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handlePermissionNotExists(PermissionNotExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(RoleNotExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleRoleNotExists(RoleNotExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(InvalidInvitationTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidInvitationToken(InvalidInvitationTokenException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(InvitationTokenExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvitationTokenExpired(InvitationTokenExpiredException ex) {
        return ResponseEntity
                .status(HttpStatus.GONE)
                .body(ApiResponse.error(ex.getMessage()));
    }
}


