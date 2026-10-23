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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MaterialServiceImplement implements MaterialService {

    private final MaterialRepository materialRepository;
    private final StockMovementRepository stockMovementRepository;

    @Override
    @Transactional
    public ApiResponse<MaterialResponse> createMaterial(CreateMaterialRequest request) {
        String name = request.name().trim();
        if (materialRepository.existsByName(name)) {
            throw new MaterialAlreadyExistsException("Material with name '" + name + "' already exists");
        }

        Material material = new Material();
        material.setName(name);
        material.setUnit(request.unit().trim());
        material.setDescription(cleanString(request.description()));
        material.setQuantity(request.quantity() != null ? request.quantity() : 0.0);
        material.setImageUrl(cleanString(request.imageUrl()));

        Material saved = materialRepository.save(material);
        return ApiResponse.success(MaterialResponse.fromEntity(saved), "Material created successfully");
    }

    @Override
    @Transactional
    public ApiResponse<MaterialResponse> updateMaterial(UUID id, UpdateMaterialRequest request) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new MaterialNotExistsException("Material does not exist"));

        String name = request.name().trim();
        if (materialRepository.existsByNameAndIdNot(name, id)) {
            throw new MaterialAlreadyExistsException("Material with name '" + name + "' already exists");
        }

        material.setName(name);
        material.setUnit(request.unit().trim());
        material.setDescription(cleanString(request.description()));
        if (request.quantity() != null) {
            material.setQuantity(request.quantity());
        }
        material.setImageUrl(cleanString(request.imageUrl()));

        Material updated = materialRepository.save(material);
        return ApiResponse.success(MaterialResponse.fromEntity(updated), "Material updated successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<MaterialResponse> getMaterialById(UUID id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new MaterialNotExistsException("Material does not exist"));

        return ApiResponse.success(MaterialResponse.fromEntity(material), "Material retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<MaterialResponse>> getAllMaterials() {
        List<MaterialResponse> materials = materialRepository.findAll().stream()
                .map(MaterialResponse::fromEntity)
                .toList();

        return ApiResponse.success(materials, "Materials retrieved successfully");
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteMaterial(UUID id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new MaterialNotExistsException("Material does not exist"));

        if (stockMovementRepository.existsByMaterialId(id)) {
            throw new MaterialInUseException("Cannot delete material because it is referenced in stock movements");
        }

        materialRepository.delete(material);
        return ApiResponse.success("Material deleted successfully");
    }

    private String cleanString(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
