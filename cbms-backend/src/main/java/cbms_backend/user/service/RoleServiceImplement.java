package cbms_backend.user.service;

import cbms_backend.core.exception.PermissionNotExistsException;
import cbms_backend.core.exception.RoleAlreadyExistsException;
import cbms_backend.core.exception.RoleNotExistsException;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.user.dto.permission.PermissionResponse;
import cbms_backend.user.dto.role.AssignPermissionsRequest;
import cbms_backend.user.dto.role.CreateRoleRequest;
import cbms_backend.user.dto.role.RoleResponse;
import cbms_backend.user.entity.Permission;
import cbms_backend.user.entity.Role;
import cbms_backend.user.repository.PermissionRepository;
import cbms_backend.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImplement implements RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public ApiResponse<RoleResponse> createRole(CreateRoleRequest request) {
        String name = request.name().trim();
        if (roleRepository.existsByName(name)) {
            throw new RoleAlreadyExistsException("Role '" + name + "' already exists");
        }

        Role role = new Role();
        role.setName(name);

        if (request.permissionIds() != null && !request.permissionIds().isEmpty()) {
            List<Permission> permissions = permissionRepository.findAllById(request.permissionIds());
            if (permissions.size() != request.permissionIds().size()) {
                Set<UUID> foundIds = permissions.stream().map(Permission::getId).collect(Collectors.toSet());
                Set<UUID> missingIds = new HashSet<>(request.permissionIds());
                missingIds.removeAll(foundIds);
                throw new PermissionNotExistsException("Permissions not found with IDs: " + missingIds);
            }
            for (Permission p : permissions) {
                role.addPermission(p);
            }
        }

        Role saved = roleRepository.save(role);
        return ApiResponse.success(RoleResponse.fromEntity(saved), "Role created successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<RoleResponse>> getAllRoles() {
        List<RoleResponse> roles = roleRepository.findAllWithPermissions().stream()
                .map(RoleResponse::fromEntity)
                .toList();
        return ApiResponse.success(roles, "Roles retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<RoleResponse> getRoleById(UUID id) {
        Role role = roleRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new RoleNotExistsException("Role does not exist"));
        return ApiResponse.success(RoleResponse.fromEntity(role), "Role retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Set<PermissionResponse>> getPermissionsByRoleId(UUID roleId) {
        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new RoleNotExistsException("Role does not exist"));

        Set<PermissionResponse> permissions = Collections.emptySet();
        if (role.getPermissions() != null) {
            permissions = role.getPermissions().stream()
                    .map(PermissionResponse::fromEntity)
                    .collect(Collectors.toSet());
        }

        return ApiResponse.success(permissions, "Permissions of role retrieved successfully");
    }

    @Override
    @Transactional
    public ApiResponse<RoleResponse> assignPermissionsToRole(UUID roleId, AssignPermissionsRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body must not be null");
        }
        Set<UUID> permissionIds = request.extractPermissionIds();
        if (permissionIds.isEmpty()) {
            throw new IllegalArgumentException("At least one permission ID must be provided");
        }

        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new RoleNotExistsException("Role does not exist"));

        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        if (permissions.size() != permissionIds.size()) {
            Set<UUID> foundIds = permissions.stream().map(Permission::getId).collect(Collectors.toSet());
            Set<UUID> missingIds = new HashSet<>(permissionIds);
            missingIds.removeAll(foundIds);
            throw new PermissionNotExistsException("Permissions not found with IDs: " + missingIds);
        }

        for (Permission permission : permissions) {
            role.addPermission(permission);
        }

        Role updated = roleRepository.save(role);
        return ApiResponse.success(RoleResponse.fromEntity(updated), "Permissions assigned to role successfully");
    }

    @Override
    @Transactional
    public ApiResponse<RoleResponse> assignPermissionToRole(UUID roleId, UUID permissionId) {
        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new RoleNotExistsException("Role does not exist"));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new PermissionNotExistsException("Permission does not exist"));

        role.addPermission(permission);
        Role updated = roleRepository.save(role);
        return ApiResponse.success(RoleResponse.fromEntity(updated), "Permission assigned to role successfully");
    }

    @Override
    @Transactional
    public ApiResponse<RoleResponse> removePermissionFromRole(UUID roleId, UUID permissionId) {
        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new RoleNotExistsException("Role does not exist"));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new PermissionNotExistsException("Permission does not exist"));

        role.removePermission(permission);
        Role updated = roleRepository.save(role);
        return ApiResponse.success(RoleResponse.fromEntity(updated), "Permission removed from role successfully");
    }
}
