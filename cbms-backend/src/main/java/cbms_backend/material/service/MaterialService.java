package cbms_backend.material.service;

import cbms_backend.core.util.ApiResponse;
import cbms_backend.material.dto.create_material.CreateMaterialRequest;
import cbms_backend.material.dto.get_material.MaterialResponse;
import cbms_backend.material.dto.update_material.UpdateMaterialRequest;

import java.util.List;
import java.util.UUID;

public interface MaterialService {
    ApiResponse<MaterialResponse> createMaterial(CreateMaterialRequest request);
    ApiResponse<MaterialResponse> updateMaterial(UUID id, UpdateMaterialRequest request);
    ApiResponse<MaterialResponse> getMaterialById(UUID id);
    ApiResponse<List<MaterialResponse>> getAllMaterials();
    ApiResponse<Void> deleteMaterial(UUID id);
}
