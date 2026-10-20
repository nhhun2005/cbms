package cbms_backend.user.controller;

import cbms_backend.core.util.ApiResponse;
import cbms_backend.user.dto.create_account.CreateAccountRequest;
import cbms_backend.user.dto.get_user.GetUserResponse;
import cbms_backend.user.dto.role.AssignRolesRequest;
import cbms_backend.user.dto.role.RoleResponse;
import cbms_backend.user.dto.update_user.UpdateNameRequest;
import cbms_backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @PostMapping
    public ResponseEntity<ApiResponse<GetUserResponse>> createUser(
            @Valid @RequestBody CreateAccountRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.createAccount(request));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GetUserResponse>>> getAllUsers() {
        return ResponseEntity.ok(service.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GetUserResponse>> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getUser(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateName(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateNameRequest request
    ) {
        return ResponseEntity.ok(service.updateName(id, request));
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<ApiResponse<Void>> disableUser(@PathVariable UUID id) {
        return ResponseEntity.ok(service.disableUser(id));
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<ApiResponse<Void>> enableUser(@PathVariable UUID id) {
        return ResponseEntity.ok(service.enableUser(id));
    }


    @GetMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<Set<RoleResponse>>> getRolesByUserId(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getRolesByUserId(id));
    }

    @PostMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<GetUserResponse>> assignRoles(
            @PathVariable UUID id,
            @RequestBody AssignRolesRequest request
    ) {
        return ResponseEntity.ok(service.assignRolesToUser(id, request));
    }

    @PostMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<ApiResponse<GetUserResponse>> assignRole(
            @PathVariable UUID userId,
            @PathVariable UUID roleId
    ) {
        return ResponseEntity.ok(service.assignRoleToUser(userId, roleId));
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<ApiResponse<GetUserResponse>> removeRole(
            @PathVariable UUID userId,
            @PathVariable UUID roleId
    ) {
        return ResponseEntity.ok(service.removeRoleFromUser(userId, roleId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        return ResponseEntity.ok(service.deleteUser(id));
    }
}
