package cbms_backend.material.controller;

import cbms_backend.core.util.ApiResponse;
import cbms_backend.material.dto.create_material.CreateMaterialRequest;
import cbms_backend.material.dto.get_material.MaterialResponse;
import cbms_backend.material.dto.update_material.UpdateMaterialRequest;
import cbms_backend.material.service.MaterialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    @PostMapping
    public ResponseEntity<ApiResponse<MaterialResponse>> createMaterial(
            @Valid @RequestBody CreateMaterialRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(materialService.createMaterial(request));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MaterialResponse>>> getAllMaterials() {
        return ResponseEntity.ok(materialService.getAllMaterials());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MaterialResponse>> getMaterialById(@PathVariable UUID id) {
        return ResponseEntity.ok(materialService.getMaterialById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MaterialResponse>> updateMaterial(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMaterialRequest request
    ) {
        return ResponseEntity.ok(materialService.updateMaterial(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMaterial(@PathVariable UUID id) {
        return ResponseEntity.ok(materialService.deleteMaterial(id));
    }
}
