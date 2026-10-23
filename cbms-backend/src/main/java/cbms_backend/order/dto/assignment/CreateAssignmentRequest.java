package cbms_backend.order.dto.assignment;

import cbms_backend.order.entity.AssignmentRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateAssignmentRequest(
        @NotNull(message = "User ID must not be null")
        UUID userId,

        @NotNull(message = "Role must not be null")
        AssignmentRole role
) {}
