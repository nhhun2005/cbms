package cbms_backend.material.controller;

import cbms_backend.core.util.ApiResponse;
import cbms_backend.material.dto.create_stock_in.CreateStockInRequest;
import cbms_backend.material.dto.get_stock_movement.StockMovementResponse;
import cbms_backend.material.dto.get_stock_movement.WarehouseStockSummaryResponse;
import cbms_backend.material.entity.StockMovementType;
import cbms_backend.material.service.StockMovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stock-movements")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @PostMapping(value = {"", "/in"})
    public ResponseEntity<ApiResponse<StockMovementResponse>> recordStockIn(
            @Valid @RequestBody CreateStockInRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(stockMovementService.recordStockIn(request));
    }

    @GetMapping(value = {"", "/history", "/warehouse/history"})
    public ResponseEntity<ApiResponse<List<StockMovementResponse>>> getWarehouseHistory(
            @RequestParam(required = false) StockMovementType type,
            @RequestParam(required = false) UUID materialId,
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return ResponseEntity.ok(stockMovementService.getWarehouseHistory(type, materialId, supplierId, from, to));
    }

    @GetMapping("/warehouse/summary")
    public ResponseEntity<ApiResponse<WarehouseStockSummaryResponse>> getWarehouseSummary() {
        return ResponseEntity.ok(stockMovementService.getWarehouseSummary());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StockMovementResponse>> getStockMovementById(@PathVariable UUID id) {
        return ResponseEntity.ok(stockMovementService.getStockMovementById(id));
    }

    @GetMapping("/material/{materialId}")
    public ResponseEntity<ApiResponse<List<StockMovementResponse>>> getStockMovementsByMaterialId(
            @PathVariable UUID materialId
    ) {
        return ResponseEntity.ok(stockMovementService.getStockMovementsByMaterialId(materialId));
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<ApiResponse<List<StockMovementResponse>>> getStockMovementsBySupplierId(
            @PathVariable UUID supplierId
    ) {
        return ResponseEntity.ok(stockMovementService.getStockMovementsBySupplierId(supplierId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStockMovement(@PathVariable UUID id) {
        return ResponseEntity.ok(stockMovementService.deleteStockMovement(id));
    }
}
