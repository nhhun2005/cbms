package cbms_backend.material.service;

import cbms_backend.core.exception.MaterialAlreadyExistsException;
import cbms_backend.core.exception.MaterialInUseException;
import cbms_backend.core.exception.MaterialNotExistsException;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.material.dto.create_material.CreateMaterialRequest;
import cbms_backend.material.dto.get_material.MaterialResponse;
import cbms_backend.material.dto.update_material.UpdateMaterialRequest;
import cbms_backend.material.entity.Material;
import cbms_backend.material.repository.MaterialRepository;
import cbms_backend.material.repository.StockMovementRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialServiceImplementTest {

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private MaterialServiceImplement materialService;

    @Test
    @DisplayName("createMaterial should succeed with full fields including Cloudinary imageUrl")
    void createMaterial_shouldSucceed_withFullFields() {
        CreateMaterialRequest request = new CreateMaterialRequest(
                "Vải gấm cao cấp",
                "Chất liệu cao cấp chống nắng",
                "mét",
                150.5,
                "https://res.cloudinary.com/demo/image/upload/v123456/curtains/material1.jpg"
        );

        when(materialRepository.existsByName("Vải gấm cao cấp")).thenReturn(false);
        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> {
            Material m = invocation.getArgument(0);
            m.setId(UUID.randomUUID());
            m.setUpdatedAt(Instant.now());
            return m;
        });

        ApiResponse<MaterialResponse> response = materialService.createMaterial(request);

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals("Vải gấm cao cấp", response.getData().name());
        assertEquals("Chất liệu cao cấp chống nắng", response.getData().description());
        assertEquals("mét", response.getData().unit());
        assertEquals(150.5, response.getData().quantity());
        assertEquals(100000.0, response.getData().price());
        assertEquals("https://res.cloudinary.com/demo/image/upload/v123456/curtains/material1.jpg", response.getData().imageUrl());
        assertEquals("Material created successfully", response.getMessage());

        verify(materialRepository).existsByName("Vải gấm cao cấp");
        verify(materialRepository).save(any(Material.class));
    }

    @Test
    @DisplayName("createMaterial should default quantity to 0.0 and null imageUrl when not provided")
    void createMaterial_shouldDefaultQuantityAndNullImageUrl() {
        CreateMaterialRequest request = new CreateMaterialRequest(
                "Khoen rèm inox",
                "cái"
        );

        when(materialRepository.existsByName("Khoen rèm inox")).thenReturn(false);
        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> {
            Material m = invocation.getArgument(0);
            m.setId(UUID.randomUUID());
            m.setUpdatedAt(Instant.now());
            return m;
        });

        ApiResponse<MaterialResponse> response = materialService.createMaterial(request);

        assertNotNull(response.getData());
        assertEquals(0.0, response.getData().quantity());
        assertEquals(100000.0, response.getData().price());
        assertNull(response.getData().imageUrl());
        assertNull(response.getData().description());
    }

    @Test
    @DisplayName("createMaterial should save custom price when provided")
    void createMaterial_shouldSaveCustomPrice_whenProvided() {
        CreateMaterialRequest request = new CreateMaterialRequest(
                "Vải gấm hoàng gia",
                "Mô tả",
                "mét",
                50.0,
                250000.0,
                null
        );

        when(materialRepository.existsByName("Vải gấm hoàng gia")).thenReturn(false);
        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> {
            Material m = invocation.getArgument(0);
            m.setId(UUID.randomUUID());
            m.setUpdatedAt(Instant.now());
            return m;
        });

        ApiResponse<MaterialResponse> response = materialService.createMaterial(request);

        assertNotNull(response.getData());
        assertEquals(250000.0, response.getData().price());
    }

    @Test
    @DisplayName("createMaterial should throw MaterialAlreadyExistsException when name exists")
    void createMaterial_shouldThrowException_whenNameExists() {
        CreateMaterialRequest request = new CreateMaterialRequest(
                "Vải gấm cao cấp",
                "mét"
        );

        when(materialRepository.existsByName("Vải gấm cao cấp")).thenReturn(true);

        assertThrows(MaterialAlreadyExistsException.class, () -> materialService.createMaterial(request));
        verify(materialRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateMaterial should succeed and update all fields")
    void updateMaterial_shouldSucceed() {
        UUID id = UUID.randomUUID();
        Material existing = new Material();
        existing.setId(id);
        existing.setName("Vải voan trắng");
        existing.setUnit("mét");
        existing.setQuantity(50.0);
        existing.setImageUrl("https://res.cloudinary.com/demo/old.jpg");
        existing.setUpdatedAt(Instant.now());

        UpdateMaterialRequest request = new UpdateMaterialRequest(
                "Vải voan cao cấp",
                "Voan thêu họa tiết",
                "cuộn",
                100.0,
                "https://res.cloudinary.com/demo/new.jpg"
        );

        when(materialRepository.findById(id)).thenReturn(Optional.of(existing));
        when(materialRepository.existsByNameAndIdNot("Vải voan cao cấp", id)).thenReturn(false);
        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApiResponse<MaterialResponse> response = materialService.updateMaterial(id, request);

        assertNotNull(response.getData());
        assertEquals("Vải voan cao cấp", response.getData().name());
        assertEquals("Voan thêu họa tiết", response.getData().description());
        assertEquals("cuộn", response.getData().unit());
        assertEquals(100.0, response.getData().quantity());
        assertEquals("https://res.cloudinary.com/demo/new.jpg", response.getData().imageUrl());
        assertEquals("Material updated successfully", response.getMessage());
    }

    @Test
    @DisplayName("updateMaterial should throw MaterialNotExistsException when not found")
    void updateMaterial_shouldThrowException_whenNotFound() {
        UUID id = UUID.randomUUID();
        UpdateMaterialRequest request = new UpdateMaterialRequest("Tên mới", null, "mét", 10.0, null);

        when(materialRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(MaterialNotExistsException.class, () -> materialService.updateMaterial(id, request));
    }

    @Test
    @DisplayName("updateMaterial should throw MaterialAlreadyExistsException when new name taken")
    void updateMaterial_shouldThrowException_whenNameTaken() {
        UUID id = UUID.randomUUID();
        Material existing = new Material();
        existing.setId(id);
        existing.setName("Vải cũ");

        UpdateMaterialRequest request = new UpdateMaterialRequest("Vải trùng", null, "mét", 10.0, null);

        when(materialRepository.findById(id)).thenReturn(Optional.of(existing));
        when(materialRepository.existsByNameAndIdNot("Vải trùng", id)).thenReturn(true);

        assertThrows(MaterialAlreadyExistsException.class, () -> materialService.updateMaterial(id, request));
        verify(materialRepository, never()).save(any());
    }

    @Test
    @DisplayName("getMaterialById should return material when found")
    void getMaterialById_shouldReturnMaterial() {
        UUID id = UUID.randomUUID();
        Material material = new Material();
        material.setId(id);
        material.setName("Thanh treo nhôm");
        material.setUnit("cây");
        material.setQuantity(20.0);
        material.setImageUrl("https://res.cloudinary.com/demo/bar.jpg");
        material.setUpdatedAt(Instant.now());

        when(materialRepository.findById(id)).thenReturn(Optional.of(material));

        ApiResponse<MaterialResponse> response = materialService.getMaterialById(id);

        assertNotNull(response.getData());
        assertEquals("Thanh treo nhôm", response.getData().name());
        assertEquals("https://res.cloudinary.com/demo/bar.jpg", response.getData().imageUrl());
    }

    @Test
    @DisplayName("getMaterialById should throw MaterialNotExistsException when not found")
    void getMaterialById_shouldThrowException_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(materialRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(MaterialNotExistsException.class, () -> materialService.getMaterialById(id));
    }

    @Test
    @DisplayName("getAllMaterials should return all materials")
    void getAllMaterials_shouldReturnList() {
        Material m1 = new Material();
        m1.setId(UUID.randomUUID());
        m1.setName("M1");
        m1.setUnit("mét");

        Material m2 = new Material();
        m2.setId(UUID.randomUUID());
        m2.setName("M2");
        m2.setUnit("cái");

        when(materialRepository.findAll()).thenReturn(List.of(m1, m2));

        ApiResponse<List<MaterialResponse>> response = materialService.getAllMaterials();

        assertNotNull(response.getData());
        assertEquals(2, response.getData().size());
    }

    @Test
    @DisplayName("getAllMaterials should return empty list when no materials exist")
    void getAllMaterials_shouldReturnEmptyList() {
        when(materialRepository.findAll()).thenReturn(Collections.emptyList());

        ApiResponse<List<MaterialResponse>> response = materialService.getAllMaterials();

        assertNotNull(response.getData());
        assertTrue(response.getData().isEmpty());
    }

    @Test
    @DisplayName("deleteMaterial should succeed when material exists and not in use")
    void deleteMaterial_shouldSucceed() {
        UUID id = UUID.randomUUID();
        Material material = new Material();
        material.setId(id);

        when(materialRepository.findById(id)).thenReturn(Optional.of(material));
        when(stockMovementRepository.existsByMaterialId(id)).thenReturn(false);

        ApiResponse<Void> response = materialService.deleteMaterial(id);

        assertNotNull(response);
        assertEquals("Material deleted successfully", response.getMessage());
        verify(materialRepository).delete(material);
    }

    @Test
    @DisplayName("deleteMaterial should throw MaterialNotExistsException when not found")
    void deleteMaterial_shouldThrowException_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(materialRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(MaterialNotExistsException.class, () -> materialService.deleteMaterial(id));
        verify(materialRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteMaterial should throw MaterialInUseException when referenced by stock movements")
    void deleteMaterial_shouldThrowException_whenReferenced() {
        UUID id = UUID.randomUUID();
        Material material = new Material();
        material.setId(id);

        when(materialRepository.findById(id)).thenReturn(Optional.of(material));
        when(stockMovementRepository.existsByMaterialId(id)).thenReturn(true);

        assertThrows(MaterialInUseException.class, () -> materialService.deleteMaterial(id));
        verify(materialRepository, never()).delete(any());
    }
}
