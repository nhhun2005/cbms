package cbms_backend.user.dto.get_user;

import cbms_backend.user.entity.Role;
import cbms_backend.user.entity.User;
import cbms_backend.user.entity.UserStatus;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record GetUserResponse(
        UUID id,
        String name,
        String phone,
        UserStatus status,
        Set<String> roles
) {
    public static GetUserResponse fromEntity(User user) {
        Set<String> roleNames = null;
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());
        }
        return new GetUserResponse(
                user.getId(),
                user.getName(),
                user.getPhone(),
                user.getStatus(),
                roleNames
        );
    }
}
