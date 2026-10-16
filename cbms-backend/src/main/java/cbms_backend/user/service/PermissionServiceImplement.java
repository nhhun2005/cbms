package cbms_backend.user.service;

import cbms_backend.core.exception.PermissionAlreadyExistsException;
import cbms_backend.core.exception.PermissionInUseException;
import cbms_backend.core.exception.PermissionNotExistsException;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.user.dto.permission.CreatePermissionRequest;
import cbms_backend.user.dto.permission.PermissionResponse;
import cbms_backend.user.entity.Permission;
import cbms_backend.user.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PermissionServiceImplement implements PermissionService {
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public ApiResponse<PermissionResponse> createPermission(CreatePermissionRequest request) {
        String name = request.name().trim();
        if (permissionRepository.existsByName(name)) {
            throw new PermissionAlreadyExistsException("Permission '" + name + "' already exists");
        }

        Permission permission = new Permission();
        permission.setName(name);
        Permission saved = permissionRepository.save(permission);

        return ApiResponse.success(PermissionResponse.fromEntity(saved), "Permission created successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<PermissionResponse>> getAllPermissions() {
        List<PermissionResponse> permissions = permissionRepository.findAll().stream()
                .map(PermissionResponse::fromEntity)
                .toList();
        return ApiResponse.success(permissions, "Permissions retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PermissionResponse> getPermissionById(UUID id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new PermissionNotExistsException("Permission does not exist"));
        return ApiResponse.success(PermissionResponse.fromEntity(permission), "Permission retrieved successfully");
    }

    @Override
    @Transactional
    public ApiResponse<Void> deletePermission(UUID id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new PermissionNotExistsException("Permission does not exist"));

        if (permissionRepository.countRolesByPermissionId(id) > 0) {
            throw new PermissionInUseException("Cannot delete permission because it is currently assigned to one or more roles");
        }

        permissionRepository.delete(permission);

        return ApiResponse.success("Permission deleted successfully");
    }
}

