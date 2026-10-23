package cbms_backend.material.controller;

import cbms_backend.core.exception.GlobalExceptionHandler;
import cbms_backend.core.exception.MaterialAlreadyExistsException;
import cbms_backend.core.exception.MaterialInUseException;
import cbms_backend.core.exception.MaterialNotExistsException;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.material.dto.create_material.CreateMaterialRequest;
import cbms_backend.material.dto.get_material.MaterialResponse;
import cbms_backend.material.dto.update_material.UpdateMaterialRequest;
import cbms_backend.material.service.MaterialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MaterialControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MaterialService materialService;

    @InjectMocks
    private MaterialController materialController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(materialController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/materials should return 201 when valid request with Cloudinary imageUrl")
    void createMaterial_shouldReturn201_whenValid() throws Exception {
        UUID id = UUID.randomUUID();
        MaterialResponse response = new MaterialResponse(
                id,
                "Vải gấm hoa văn",
                "Mô tả chi tiết",
                "mét",
                50.0,
                "https://res.cloudinary.com/demo/image/upload/sample.jpg",
                Instant.now()
        );

        when(materialService.createMaterial(any(CreateMaterialRequest.class)))
                .thenReturn(ApiResponse.success(response, "Material created successfully"));

        mockMvc.perform(post("/api/v1/materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Vải gấm hoa văn",
                                    "description": "Mô tả chi tiết",
                                    "unit": "mét",
                                    "quantity": 50.0,
                                    "imageUrl": "https://res.cloudinary.com/demo/image/upload/sample.jpg"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Material created successfully"))
                .andExpect(jsonPath("$.data.id").value(id.toString()))
                .andExpect(jsonPath("$.data.name").value("Vải gấm hoa văn"))
                .andExpect(jsonPath("$.data.unit").value("mét"))
                .andExpect(jsonPath("$.data.quantity").value(50.0))
                .andExpect(jsonPath("$.data.imageUrl").value("https://res.cloudinary.com/demo/image/upload/sample.jpg"));
    }

    @Test
    @DisplayName("POST /api/v1/materials should return 400 when name is blank")
    void createMaterial_shouldReturn400_whenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "   ",
                                    "unit": "mét"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/materials should return 400 when unit is blank")
    void createMaterial_shouldReturn400_whenUnitIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Vải nhung",
                                    "unit": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/materials should return 400 when quantity is negative")
    void createMaterial_shouldReturn400_whenQuantityNegative() throws Exception {
        mockMvc.perform(post("/api/v1/materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Vải nhung",
                                    "unit": "mét",
                                    "quantity": -5.0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/materials should return 409 when name already exists")
    void createMaterial_shouldReturn409_whenNameExists() throws Exception {
        when(materialService.createMaterial(any(CreateMaterialRequest.class)))
                .thenThrow(new MaterialAlreadyExistsException("Material with name 'Vải nhung' already exists"));

        mockMvc.perform(post("/api/v1/materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Vải nhung",
                                    "unit": "mét"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Material with name 'Vải nhung' already exists"));
    }

    @Test
    @DisplayName("GET /api/v1/materials should return 200 with list of materials")
    void getAllMaterials_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        MaterialResponse m = new MaterialResponse(
                id,
                "Vải voan",
                null,
                "mét",
                10.0,
                null,
                Instant.now()
        );

        when(materialService.getAllMaterials())
                .thenReturn(ApiResponse.success(List.of(m), "Materials retrieved successfully"));

        mockMvc.perform(get("/api/v1/materials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Materials retrieved successfully"))
                .andExpect(jsonPath("$.data[0].id").value(id.toString()))
                .andExpect(jsonPath("$.data[0].name").value("Vải voan"));
    }

    @Test
    @DisplayName("GET /api/v1/materials/{id} should return 200 when material exists")
    void getMaterialById_shouldReturn200_whenFound() throws Exception {
        UUID id = UUID.randomUUID();
        MaterialResponse m = new MaterialResponse(
                id,
                "Vải gấm",
                "Chống nắng",
                "mét",
                25.0,
                "https://res.cloudinary.com/demo/image.png",
                Instant.now()
        );

        when(materialService.getMaterialById(id))
                .thenReturn(ApiResponse.success(m, "Material retrieved successfully"));

        mockMvc.perform(get("/api/v1/materials/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Material retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(id.toString()))
                .andExpect(jsonPath("$.data.name").value("Vải gấm"));
    }

    @Test
    @DisplayName("GET /api/v1/materials/{id} should return 404 when material not found")
    void getMaterialById_shouldReturn404_whenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(materialService.getMaterialById(id))
                .thenThrow(new MaterialNotExistsException("Material does not exist"));

        mockMvc.perform(get("/api/v1/materials/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Material does not exist"));
    }

    @Test
    @DisplayName("PUT /api/v1/materials/{id} should return 200 when update is successful")
    void updateMaterial_shouldReturn200_whenValid() throws Exception {
        UUID id = UUID.randomUUID();
        MaterialResponse m = new MaterialResponse(
                id,
                "Vải bố cập nhật",
                "Mô tả mới",
                "cuộn",
                80.0,
                "https://res.cloudinary.com/demo/new-image.jpg",
                Instant.now()
        );

        when(materialService.updateMaterial(eq(id), any(UpdateMaterialRequest.class)))
                .thenReturn(ApiResponse.success(m, "Material updated successfully"));

        mockMvc.perform(put("/api/v1/materials/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Vải bố cập nhật",
                                    "description": "Mô tả mới",
                                    "unit": "cuộn",
                                    "quantity": 80.0,
                                    "imageUrl": "https://res.cloudinary.com/demo/new-image.jpg"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Material updated successfully"))
                .andExpect(jsonPath("$.data.name").value("Vải bố cập nhật"))
                .andExpect(jsonPath("$.data.imageUrl").value("https://res.cloudinary.com/demo/new-image.jpg"));
    }

    @Test
    @DisplayName("PUT /api/v1/materials/{id} should return 404 when material not found")
    void updateMaterial_shouldReturn404_whenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(materialService.updateMaterial(eq(id), any(UpdateMaterialRequest.class)))
                .thenThrow(new MaterialNotExistsException("Material does not exist"));

        mockMvc.perform(put("/api/v1/materials/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Vải bố",
                                    "unit": "mét"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Material does not exist"));
    }

    @Test
    @DisplayName("PUT /api/v1/materials/{id} should return 409 when name is duplicate")
    void updateMaterial_shouldReturn409_whenDuplicateName() throws Exception {
        UUID id = UUID.randomUUID();
        when(materialService.updateMaterial(eq(id), any(UpdateMaterialRequest.class)))
                .thenThrow(new MaterialAlreadyExistsException("Material with name 'Vải bố' already exists"));

        mockMvc.perform(put("/api/v1/materials/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Vải bố",
                                    "unit": "mét"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Material with name 'Vải bố' already exists"));
    }

    @Test
    @DisplayName("DELETE /api/v1/materials/{id} should return 200 when deleted successfully")
    void deleteMaterial_shouldReturn200_whenSuccess() throws Exception {
        UUID id = UUID.randomUUID();
        when(materialService.deleteMaterial(id))
                .thenReturn(ApiResponse.success("Material deleted successfully"));

        mockMvc.perform(delete("/api/v1/materials/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Material deleted successfully"));
    }

    @Test
    @DisplayName("DELETE /api/v1/materials/{id} should return 404 when material not found")
    void deleteMaterial_shouldReturn404_whenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(materialService.deleteMaterial(id))
                .thenThrow(new MaterialNotExistsException("Material does not exist"));

        mockMvc.perform(delete("/api/v1/materials/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Material does not exist"));
    }

    @Test
    @DisplayName("DELETE /api/v1/materials/{id} should return 409 when material is in use")
    void deleteMaterial_shouldReturn409_whenInUse() throws Exception {
        UUID id = UUID.randomUUID();
        when(materialService.deleteMaterial(id))
                .thenThrow(new MaterialInUseException("Cannot delete material because it is referenced in stock movements"));

        mockMvc.perform(delete("/api/v1/materials/{id}", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Cannot delete material because it is referenced in stock movements"));
    }
}
