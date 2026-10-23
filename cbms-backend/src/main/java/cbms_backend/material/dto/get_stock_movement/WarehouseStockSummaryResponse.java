package cbms_backend.material.dto.get_stock_movement;

import java.util.List;

public record WarehouseStockSummaryResponse(
        long totalMovements,
        double totalInQuantity,
        double totalOutQuantity,
        double totalAdjustmentQuantity,
        long distinctMaterialsCount,
        List<StockMovementResponse> recentMovements
) {
}
