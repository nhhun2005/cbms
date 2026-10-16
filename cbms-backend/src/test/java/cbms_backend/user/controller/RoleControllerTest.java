package cbms_backend.user.controller;

import cbms_backend.core.exception.GlobalExceptionHandler;
import cbms_backend.core.exception.RoleAlreadyExistsException;
import cbms_backend.core.exception.RoleNotExistsException;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.user.dto.permission.PermissionResponse;
import cbms_backend.user.dto.role.AssignPermissionsRequest;
import cbms_backend.user.dto.role.CreateRoleRequest;
import cbms_backend.user.dto.role.RoleResponse;
import cbms_backend.user.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleController roleController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(roleController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createRole_shouldReturn201_whenValid() throws Exception {
        UUID id = UUID.randomUUID();
        RoleResponse response = new RoleResponse(id, "ADMIN", null);

        when(roleService.createRole(any(CreateRoleRequest.class)))
                .thenReturn(ApiResponse.success(response, "Role created successfully"));

        mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ADMIN\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Role created successfully"))
                .andExpect(jsonPath("$.data.id").value(id.toString()))
                .andExpect(jsonPath("$.data.name").value("ADMIN"));
    }

    @Test
    void getPermissionsByRoleId_shouldReturnPermissions() throws Exception {
        UUID roleId = UUID.randomUUID();
        UUID permId = UUID.randomUUID();
        PermissionResponse p = new PermissionResponse(permId, "READ_DOCS");

        when(roleService.getPermissionsByRoleId(roleId))
                .thenReturn(ApiResponse.success(Set.of(p), "Permissions of role retrieved successfully"));

        mockMvc.perform(get("/api/v1/roles/" + roleId + "/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Permissions of role retrieved successfully"))
                .andExpect(jsonPath("$.data[0].id").value(permId.toString()))
                .andExpect(jsonPath("$.data[0].name").value("READ_DOCS"));
    }

    @Test
    void assignPermissionsToRole_viaBody_shouldReturnUpdatedRole() throws Exception {
        UUID roleId = UUID.randomUUID();
        UUID permId = UUID.randomUUID();
        PermissionResponse p = new PermissionResponse(permId, "WRITE_DOCS");
        RoleResponse response = new RoleResponse(roleId, "ADMIN", Set.of(p));

        when(roleService.assignPermissionsToRole(eq(roleId), any(AssignPermissionsRequest.class)))
                .thenReturn(ApiResponse.success(response, "Permissions assigned to role successfully"));

        mockMvc.perform(post("/api/v1/roles/" + roleId + "/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"permissionIds\": [\"" + permId + "\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Permissions assigned to role successfully"))
                .andExpect(jsonPath("$.data.permissions[0].name").value("WRITE_DOCS"));
    }

    @Test
    void assignPermissionToRole_viaPath_shouldReturnUpdatedRole() throws Exception {
        UUID roleId = UUID.randomUUID();
        UUID permId = UUID.randomUUID();
        PermissionResponse p = new PermissionResponse(permId, "EXECUTE");
        RoleResponse response = new RoleResponse(roleId, "ADMIN", Set.of(p));

        when(roleService.assignPermissionToRole(roleId, permId))
                .thenReturn(ApiResponse.success(response, "Permission assigned to role successfully"));

        mockMvc.perform(post("/api/v1/roles/" + roleId + "/permissions/" + permId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Permission assigned to role successfully"))
                .andExpect(jsonPath("$.data.permissions[0].name").value("EXECUTE"));
    }

    @Test
    void removePermissionFromRole_shouldReturnSuccess() throws Exception {
        UUID roleId = UUID.randomUUID();
        UUID permId = UUID.randomUUID();
        RoleResponse response = new RoleResponse(roleId, "ADMIN", Set.of());

        when(roleService.removePermissionFromRole(roleId, permId))
                .thenReturn(ApiResponse.success(response, "Permission removed from role successfully"));

        mockMvc.perform(delete("/api/v1/roles/" + roleId + "/permissions/" + permId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Permission removed from role successfully"));
    }

    @Test
    void getPermissionsByRoleId_shouldReturn404_whenRoleNotExists() throws Exception {
        UUID roleId = UUID.randomUUID();

        when(roleService.getPermissionsByRoleId(roleId))
                .thenThrow(new RoleNotExistsException("Role does not exist"));

        mockMvc.perform(get("/api/v1/roles/" + roleId + "/permissions"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Role does not exist"));
    }

    @Test
    void deleteRole_shouldReturn200_whenSuccessful() throws Exception {
        UUID roleId = UUID.randomUUID();

        when(roleService.deleteRole(roleId)).thenReturn(ApiResponse.success("Role deleted successfully"));

        mockMvc.perform(delete("/api/v1/roles/" + roleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Role deleted successfully"));
    }

    @Test
    void deleteRole_shouldReturn409_whenInUse() throws Exception {
        UUID roleId = UUID.randomUUID();

        when(roleService.deleteRole(roleId))
                .thenThrow(new cbms_backend.core.exception.RoleInUseException("Cannot delete role because it is currently assigned to one or more users"));

        mockMvc.perform(delete("/api/v1/roles/" + roleId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Cannot delete role because it is currently assigned to one or more users"));
    }
}

