package cbms_backend.user.service;

import cbms_backend.core.util.ApiResponse;
import cbms_backend.user.dto.create_account.CreateAccountRequest;

public interface UserService {
    ApiResponse<Void> createAccount(CreateAccountRequest request);
}
