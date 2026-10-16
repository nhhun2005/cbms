package cbms_backend.user.service;

import cbms_backend.core.util.ApiResponse;
import cbms_backend.user.dto.permission.PermissionResponse;
import cbms_backend.user.dto.role.AssignPermissionsRequest;
import cbms_backend.user.dto.role.CreateRoleRequest;
import cbms_backend.user.dto.role.RoleResponse;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface RoleService {
    ApiResponse<RoleResponse> createRole(CreateRoleRequest request);
    ApiResponse<List<RoleResponse>> getAllRoles();
    ApiResponse<RoleResponse> getRoleById(UUID id);
    ApiResponse<Set<PermissionResponse>> getPermissionsByRoleId(UUID roleId);
    ApiResponse<RoleResponse> assignPermissionsToRole(UUID roleId, AssignPermissionsRequest request);
    ApiResponse<RoleResponse> assignPermissionToRole(UUID roleId, UUID permissionId);
    ApiResponse<RoleResponse> removePermissionFromRole(UUID roleId, UUID permissionId);
    ApiResponse<Void> deleteRole(UUID id);
}

