package cbms_backend.user.service;

import cbms_backend.core.util.ApiResponse;
import cbms_backend.user.dto.permission.CreatePermissionRequest;
import cbms_backend.user.dto.permission.PermissionResponse;

import java.util.List;
import java.util.UUID;

public interface PermissionService {
    ApiResponse<PermissionResponse> createPermission(CreatePermissionRequest request);
    ApiResponse<List<PermissionResponse>> getAllPermissions();
    ApiResponse<PermissionResponse> getPermissionById(UUID id);
    ApiResponse<Void> deletePermission(UUID id);
}

