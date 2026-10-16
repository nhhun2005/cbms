package cbms_backend.user.controller;

import cbms_backend.core.exception.GlobalExceptionHandler;
import cbms_backend.core.exception.PermissionAlreadyExistsException;
import cbms_backend.core.exception.PermissionNotExistsException;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.user.dto.permission.CreatePermissionRequest;
import cbms_backend.user.dto.permission.PermissionResponse;
import cbms_backend.user.service.PermissionService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PermissionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private PermissionController permissionController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(permissionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createPermission_shouldReturn201_whenValid() throws Exception {
        UUID id = UUID.randomUUID();
        PermissionResponse response = new PermissionResponse(id, "ROLE_ASSIGN");

        when(permissionService.createPermission(any(CreatePermissionRequest.class)))
                .thenReturn(ApiResponse.success(response, "Permission created successfully"));

        mockMvc.perform(post("/api/v1/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ROLE_ASSIGN\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Permission created successfully"))
                .andExpect(jsonPath("$.data.id").value(id.toString()))
                .andExpect(jsonPath("$.data.name").value("ROLE_ASSIGN"));
    }

    @Test
    void createPermission_shouldReturn409_whenDuplicate() throws Exception {
        when(permissionService.createPermission(any(CreatePermissionRequest.class)))
                .thenThrow(new PermissionAlreadyExistsException("Permission 'ROLE_ASSIGN' already exists"));

        mockMvc.perform(post("/api/v1/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ROLE_ASSIGN\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Permission 'ROLE_ASSIGN' already exists"));
    }

    @Test
    void getAllPermissions_shouldReturnList() throws Exception {
        UUID id = UUID.randomUUID();
        PermissionResponse response = new PermissionResponse(id, "ROLE_ASSIGN");

        when(permissionService.getAllPermissions())
                .thenReturn(ApiResponse.success(List.of(response), "Permissions retrieved successfully"));

        mockMvc.perform(get("/api/v1/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Permissions retrieved successfully"))
                .andExpect(jsonPath("$.data[0].id").value(id.toString()))
                .andExpect(jsonPath("$.data[0].name").value("ROLE_ASSIGN"));
    }

    @Test
    void getPermissionById_shouldReturn404_whenNotExists() throws Exception {
        UUID id = UUID.randomUUID();

        when(permissionService.getPermissionById(id))
                .thenThrow(new PermissionNotExistsException("Permission does not exist"));

        mockMvc.perform(get("/api/v1/permissions/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Permission does not exist"));
    }
}
