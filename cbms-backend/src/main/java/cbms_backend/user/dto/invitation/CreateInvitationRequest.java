package cbms_backend.user.dto.invitation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public record CreateInvitationRequest(
        @NotBlank(message = "Name must not be blank")
        @Size(min = 2, max = 50, message = "Name must contain 02-50 characters")
        String name,

        @NotBlank(message = "Phone must not be blank")
        @Size(min = 10, max = 10, message = "Phone must contain 10 digits")
        @Pattern(regexp = "0\\d{9}", message = "Phone must be a valid Vietnamese phone number")
        String phone,

        Set<UUID> roleIds
) {
}
