package cbms_backend.user.controller;

import cbms_backend.core.util.ApiResponse;
import cbms_backend.user.dto.create_account.CreateAccountRequest;
import cbms_backend.user.dto.update_user.UpdateNameRequest;
import cbms_backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createUser(
            @Valid @RequestBody CreateAccountRequest request
            ){

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.createAccount(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateName(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateNameRequest request
            ){

        return ResponseEntity.ok(service.updateName(id, request));
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<ApiResponse<Void>> disableUser(@PathVariable UUID id) {
        return ResponseEntity.ok(service.disableUser(id));
    }

}
