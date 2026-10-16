package cbms_backend.user.dto.invitation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcceptInvitationRequest(
        @NotBlank(message = "Token must not be blank")
        String token,

        @NotBlank(message = "Password must not be blank")
        @Size(min = 6, max = 50, message = "Password must contain 06-50 characters")
        String password
) {
}
