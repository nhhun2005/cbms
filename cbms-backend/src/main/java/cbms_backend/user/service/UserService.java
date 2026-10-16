package cbms_backend.user.service;

import cbms_backend.core.util.ApiResponse;
import cbms_backend.user.dto.create_account.CreateAccountRequest;
import cbms_backend.user.dto.get_user.GetUserResponse;
import cbms_backend.user.dto.role.AssignRolesRequest;
import cbms_backend.user.dto.role.RoleResponse;
import cbms_backend.user.dto.update_user.UpdateNameRequest;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface UserService {
    ApiResponse<GetUserResponse> createAccount(CreateAccountRequest request);
    ApiResponse<Void> updateName(UUID id, UpdateNameRequest request);
    ApiResponse<Void> disableUser(UUID id);
    ApiResponse<Void> enableUser(UUID id);
    ApiResponse<GetUserResponse> getUser(UUID id);
    ApiResponse<List<GetUserResponse>> getAllUsers();
    ApiResponse<Set<RoleResponse>> getRolesByUserId(UUID userId);
    ApiResponse<GetUserResponse> assignRolesToUser(UUID id, AssignRolesRequest request);
    ApiResponse<GetUserResponse> assignRoleToUser(UUID userId, UUID roleId);
    ApiResponse<GetUserResponse> removeRoleFromUser(UUID userId, UUID roleId);
}


