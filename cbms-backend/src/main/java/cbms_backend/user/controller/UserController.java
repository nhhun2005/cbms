package cbms_backend.user.controller;

import cbms_backend.core.util.ApiResponse;
import cbms_backend.user.dto.create_account.CreateAccountRequest;
import cbms_backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {
    UserService service;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createUser(
            @RequestBody CreateAccountRequest request
            ){

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.createAccount(request));
    }
}
