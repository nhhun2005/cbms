package cbms_backend.user.service;

import cbms_backend.core.exception.*;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.user.dto.create_account.CreateAccountRequest;
import cbms_backend.user.dto.get_user.GetUserResponse;
import cbms_backend.user.dto.role.AssignRolesRequest;
import cbms_backend.user.dto.role.RoleResponse;
import cbms_backend.user.dto.update_user.UpdateNameRequest;
import cbms_backend.user.entity.Role;
import cbms_backend.user.entity.User;
import cbms_backend.user.entity.UserStatus;
import cbms_backend.user.repository.RoleRepository;
import cbms_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserServiceImplement implements UserService {
    private final UserRepository repository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public ApiResponse<GetUserResponse> createAccount(CreateAccountRequest request) {
        String phone = request.phone().trim();
        if (repository.existsByPhone(phone)) {
            throw new UserAlreadyExistsException("Phone number already exists");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setStatus(UserStatus.WORKING);

        if (request.roleIds() != null && !request.roleIds().isEmpty()) {
            List<Role> roles = roleRepository.findAllById(request.roleIds());
            if (roles.size() != request.roleIds().size()) {
                Set<UUID> foundIds = roles.stream().map(Role::getId).collect(Collectors.toSet());
                Set<UUID> missingIds = new HashSet<>(request.roleIds());
                missingIds.removeAll(foundIds);
                throw new RoleNotExistsException("Roles not found with IDs: " + missingIds);
            }
            for (Role role : roles) {
                user.addRole(role);
            }
        }

        User saved = repository.save(user);
        return ApiResponse.success(GetUserResponse.fromEntity(saved), "Account created");
    }

    @Override
    @Transactional
    public ApiResponse<Void> updateName(UUID id, UpdateNameRequest request) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotExistsException("User does not exist"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidUserCredentialsException("Wrong password");
        }
        user.setName(request.name().trim());
        repository.save(user);
        return ApiResponse.success("Name changed");
    }

    @Override
    @Transactional
    public ApiResponse<Void> disableUser(UUID id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotExistsException("User does not exist"));

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new UserAlreadyInactiveException("User is already inactive");
        }

        user.setStatus(UserStatus.INACTIVE);
        repository.save(user);

        return ApiResponse.success("User disabled successfully");
    }

    @Override
    @Transactional
    public ApiResponse<Void> enableUser(UUID id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotExistsException("User does not exist"));

        if (user.getStatus() == UserStatus.WORKING) {
            throw new UserAlreadyActiveException("User is already active");
        }

        user.setStatus(UserStatus.WORKING);
        repository.save(user);

        return ApiResponse.success("User enabled successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<GetUserResponse> getUser(UUID id) {
        User user = repository.findByIdWithRoles(id)
                .orElseThrow(() -> new UserNotExistsException("User does not exist"));

        GetUserResponse response = GetUserResponse.fromEntity(user);
        return ApiResponse.success(response, "User retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<GetUserResponse>> getAllUsers() {
        List<GetUserResponse> users = repository.findAllWithRoles().stream()
                .map(GetUserResponse::fromEntity)
                .toList();
        return ApiResponse.success(users, "Users retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Set<RoleResponse>> getRolesByUserId(UUID userId) {
        User user = repository.findByIdWithRoles(userId)
                .orElseThrow(() -> new UserNotExistsException("User does not exist"));

        Set<RoleResponse> roles = Collections.emptySet();
        if (user.getRoles() != null) {
            roles = user.getRoles().stream()
                    .map(RoleResponse::fromEntity)
                    .collect(Collectors.toSet());
        }

        return ApiResponse.success(roles, "Roles of user retrieved successfully");
    }

    @Override
    @Transactional
    public ApiResponse<GetUserResponse> assignRolesToUser(UUID id, AssignRolesRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body must not be null");
        }
        Set<UUID> roleIds = request.extractRoleIds();
        if (roleIds.isEmpty()) {
            throw new IllegalArgumentException("At least one role ID must be provided");
        }

        User user = repository.findByIdWithRoles(id)
                .orElseThrow(() -> new UserNotExistsException("User does not exist"));

        List<Role> roles = roleRepository.findAllById(roleIds);
        if (roles.size() != roleIds.size()) {
            Set<UUID> foundIds = roles.stream().map(Role::getId).collect(Collectors.toSet());
            Set<UUID> missingIds = new HashSet<>(roleIds);
            missingIds.removeAll(foundIds);
            throw new RoleNotExistsException("Roles not found with IDs: " + missingIds);
        }

        for (Role role : roles) {
            user.addRole(role);
        }

        User updated = repository.save(user);
        return ApiResponse.success(GetUserResponse.fromEntity(updated), "Roles assigned to user successfully");
    }

    @Override
    @Transactional
    public ApiResponse<GetUserResponse> assignRoleToUser(UUID userId, UUID roleId) {
        User user = repository.findByIdWithRoles(userId)
                .orElseThrow(() -> new UserNotExistsException("User does not exist"));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotExistsException("Role does not exist"));

        user.addRole(role);
        User updated = repository.save(user);
        return ApiResponse.success(GetUserResponse.fromEntity(updated), "Role assigned to user successfully");
    }

    @Override
    @Transactional
    public ApiResponse<GetUserResponse> removeRoleFromUser(UUID userId, UUID roleId) {
        User user = repository.findByIdWithRoles(userId)
                .orElseThrow(() -> new UserNotExistsException("User does not exist"));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotExistsException("Role does not exist"));

        user.removeRole(role);
        User updated = repository.save(user);
        return ApiResponse.success(GetUserResponse.fromEntity(updated), "Role removed from user successfully");
    }
}
