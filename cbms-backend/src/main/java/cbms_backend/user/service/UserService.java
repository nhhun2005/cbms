package cbms_backend.user.service;

import cbms_backend.core.util.ApiResponse;
import cbms_backend.user.dto.create_account.CreateAccountRequest;
import cbms_backend.user.dto.update_user.UpdateNameRequest;

import java.util.UUID;

public interface UserService {
    ApiResponse<Void> createAccount(CreateAccountRequest request);
    ApiResponse<Void> updateName(UUID id, UpdateNameRequest request);
    ApiResponse<Void> disableUser(UUID id);
}
