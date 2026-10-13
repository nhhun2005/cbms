package cbms_backend.user.dto.update_user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateNameRequest(
        @NotBlank(message = "Name must not be blank")
        @Size(min=2, max=50, message = "Name must contain 02-50 characters")
        String name,
        @NotBlank(message ="Password must not be blank")
        @Size(min=6, max=50, message = "Password must contain 02-50 characters")
        String password
) {
}
