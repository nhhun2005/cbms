package cbms_backend.material.service;

import cbms_backend.core.util.ApiResponse;
import cbms_backend.material.dto.create_stock_in.CreateStockInRequest;
import cbms_backend.material.dto.get_stock_movement.StockMovementResponse;
import cbms_backend.material.dto.get_stock_movement.WarehouseStockSummaryResponse;
import cbms_backend.material.entity.StockMovementType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface StockMovementService {
    ApiResponse<StockMovementResponse> recordStockIn(CreateStockInRequest request);
    ApiResponse<StockMovementResponse> getStockMovementById(UUID id);
    ApiResponse<List<StockMovementResponse>> getAllStockMovements();
    ApiResponse<List<StockMovementResponse>> getStockMovementsByMaterialId(UUID materialId);
    ApiResponse<List<StockMovementResponse>> getStockMovementsBySupplierId(UUID supplierId);
    ApiResponse<List<StockMovementResponse>> getWarehouseHistory(
            StockMovementType type,
            UUID materialId,
            UUID supplierId,
            Instant from,
            Instant to
    );
    ApiResponse<WarehouseStockSummaryResponse> getWarehouseSummary();
    ApiResponse<Void> deleteStockMovement(UUID id);
}
