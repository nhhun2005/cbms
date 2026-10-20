package cbms_backend.user.controller;

import cbms_backend.core.exception.GlobalExceptionHandler;
import cbms_backend.core.exception.UserAlreadyExistsException;
import cbms_backend.core.exception.UserNotExistsException;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.user.dto.create_account.CreateAccountRequest;
import cbms_backend.user.dto.get_user.GetUserResponse;
import cbms_backend.user.dto.role.AssignRolesRequest;
import cbms_backend.user.dto.role.RoleResponse;
import cbms_backend.user.dto.update_user.UpdateNameRequest;
import cbms_backend.user.entity.UserStatus;
import cbms_backend.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createUser_shouldReturn201_whenValid() throws Exception {
        UUID id = UUID.randomUUID();
        GetUserResponse response = new GetUserResponse(id, "Huan", "0354023611", UserStatus.WORKING, Set.of("ADMIN"));

        when(userService.createAccount(any(CreateAccountRequest.class)))
                .thenReturn(ApiResponse.success(response, "Account created"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Huan\", \"phone\": \"0354023611\", \"password\": \"123456\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Account created"))
                .andExpect(jsonPath("$.data.id").value(id.toString()))
                .andExpect(jsonPath("$.data.name").value("Huan"));
    }

    @Test
    void createUser_shouldReturn409_whenPhoneAlreadyExists() throws Exception {
        when(userService.createAccount(any(CreateAccountRequest.class)))
                .thenThrow(new UserAlreadyExistsException("Phone number already exists"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Huan\", \"phone\": \"0354023611\", \"password\": \"123456\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Phone number already exists"));
    }

    @Test
    void getUser_shouldReturnUser() throws Exception {
        UUID id = UUID.randomUUID();
        GetUserResponse response = new GetUserResponse(id, "Huan", "0354023611", UserStatus.WORKING, Set.of("ADMIN"));

        when(userService.getUser(id)).thenReturn(ApiResponse.success(response, "User retrieved successfully"));

        mockMvc.perform(get("/api/v1/users/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User retrieved successfully"))
                .andExpect(jsonPath("$.data.name").value("Huan"));
    }

    @Test
    void getUser_shouldReturn404_whenNotExists() throws Exception {
        UUID id = UUID.randomUUID();

        when(userService.getUser(id)).thenThrow(new UserNotExistsException("User does not exist"));

        mockMvc.perform(get("/api/v1/users/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User does not exist"));
    }

    @Test
    void getAllUsers_shouldReturnList() throws Exception {
        UUID id = UUID.randomUUID();
        GetUserResponse response = new GetUserResponse(id, "Huan", "0354023611", UserStatus.WORKING, Set.of());

        when(userService.getAllUsers()).thenReturn(ApiResponse.success(List.of(response), "Users retrieved successfully"));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Huan"));
    }

    @Test
    void assignRoles_bulk_shouldReturnUpdatedUser() throws Exception {
        UUID id = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        GetUserResponse response = new GetUserResponse(id, "Huan", "0354023611", UserStatus.WORKING, Set.of("MANAGER"));

        when(userService.assignRolesToUser(eq(id), any(AssignRolesRequest.class)))
                .thenReturn(ApiResponse.success(response, "Roles assigned to user successfully"));

        mockMvc.perform(post("/api/v1/users/" + id + "/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleIds\": [\"" + roleId + "\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roles[0]").value("MANAGER"));
    }

    @Test
    void assignRole_singlePath_shouldReturnUpdatedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        GetUserResponse response = new GetUserResponse(userId, "Huan", "0354023611", UserStatus.WORKING, Set.of("TESTER"));

        when(userService.assignRoleToUser(userId, roleId))
                .thenReturn(ApiResponse.success(response, "Role assigned to user successfully"));

        mockMvc.perform(post("/api/v1/users/" + userId + "/roles/" + roleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roles[0]").value("TESTER"));
    }

    @Test
    void removeRole_shouldReturnSuccess() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        GetUserResponse response = new GetUserResponse(userId, "Huan", "0354023611", UserStatus.WORKING, Set.of());

        when(userService.removeRoleFromUser(userId, roleId))
                .thenReturn(ApiResponse.success(response, "Role removed from user successfully"));

        mockMvc.perform(delete("/api/v1/users/" + userId + "/roles/" + roleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Role removed from user successfully"));
    }

    @Test
    void disableUser_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();

        when(userService.disableUser(id)).thenReturn(ApiResponse.success("User disabled successfully"));

        mockMvc.perform(patch("/api/v1/users/" + id + "/disable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User disabled successfully"));
    }

    @Test
    void enableUser_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();

        when(userService.enableUser(id)).thenReturn(ApiResponse.success("User enabled successfully"));

        mockMvc.perform(patch("/api/v1/users/" + id + "/enable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User enabled successfully"));
    }

    @Test
    void deleteUser_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();

        when(userService.deleteUser(id)).thenReturn(ApiResponse.success("User deleted successfully"));

        mockMvc.perform(delete("/api/v1/users/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully"));
    }

    @Test
    void deleteUser_shouldReturn404_whenUserNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        when(userService.deleteUser(id)).thenThrow(new UserNotExistsException("User does not exist"));

        mockMvc.perform(delete("/api/v1/users/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User does not exist"));
    }
}

