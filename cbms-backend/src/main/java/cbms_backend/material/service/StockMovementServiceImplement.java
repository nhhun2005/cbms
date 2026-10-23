package cbms_backend.material.service;

import cbms_backend.core.exception.*;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.material.dto.create_stock_in.CreateStockInRequest;
import cbms_backend.material.dto.create_stock_out.CreateStockOutRequest;
import cbms_backend.material.dto.get_stock_movement.StockMovementResponse;
import cbms_backend.material.entity.Material;
import cbms_backend.material.entity.StockMovement;
import cbms_backend.material.entity.StockMovementType;
import cbms_backend.material.entity.Supplier;
import cbms_backend.material.repository.MaterialRepository;
import cbms_backend.material.repository.StockMovementRepository;
import cbms_backend.material.repository.SupplierRepository;
import cbms_backend.order.entity.Order;
import cbms_backend.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class StockMovementServiceImplement implements StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final MaterialRepository materialRepository;
    private final SupplierRepository supplierRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public ApiResponse<StockMovementResponse> recordStockIn(CreateStockInRequest request) {
        Material material = materialRepository.findById(request.materialId())
                .orElseThrow(() -> new MaterialNotExistsException("Material does not exist"));

        Supplier supplier = null;
        if (request.supplierId() != null) {
            supplier = supplierRepository.findById(request.supplierId())
                    .orElseThrow(() -> new SupplierNotExistsException("Supplier does not exist"));
        }

        // Tăng quantity của material theo quantity của StockMovement
        double currentQuantity = material.getQuantity() != null ? material.getQuantity() : 0.0;
        material.setQuantity(currentQuantity + request.quantity());
        materialRepository.save(material);

        // Tạo bản ghi StockMovement với type là IN
        StockMovement stockMovement = new StockMovement();
        stockMovement.setMaterial(material);
        stockMovement.setSupplier(supplier);
        stockMovement.setType(StockMovementType.IN);
        stockMovement.setQuantity(request.quantity());
        stockMovement.setCreatedAt(Instant.now());

        StockMovement saved = stockMovementRepository.save(stockMovement);
        return ApiResponse.success(StockMovementResponse.fromEntity(saved), "Stock in recorded successfully");
    }

    @Override
    @Transactional
    public ApiResponse<StockMovementResponse> recordStockOut(CreateStockOutRequest request) {
        Material material = materialRepository.findById(request.materialId())
                .orElseThrow(() -> new MaterialNotExistsException("Material does not exist"));

        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new OrderNotExistsException("Order does not exist"));

        double currentQuantity = material.getQuantity() != null ? material.getQuantity() : 0.0;
        if (currentQuantity < request.quantity()) {
            throw new InsufficientStockException(
                    "Insufficient material quantity in stock (available: " + currentQuantity +
                    ", requested: " + request.quantity() + ")"
            );
        }

        material.setQuantity(currentQuantity - request.quantity());
        materialRepository.save(material);

        StockMovement stockMovement = new StockMovement();
        stockMovement.setMaterial(material);
        stockMovement.setOrder(order);
        stockMovement.setType(StockMovementType.OUT);
        stockMovement.setQuantity(request.quantity());
        stockMovement.setCreatedAt(Instant.now());

        StockMovement saved = stockMovementRepository.save(stockMovement);
        return ApiResponse.success(StockMovementResponse.fromEntity(saved), "Stock out recorded successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<StockMovementResponse> getStockMovementById(UUID id) {
        StockMovement movement = stockMovementRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new StockMovementNotExistsException("Stock movement does not exist"));

        return ApiResponse.success(StockMovementResponse.fromEntity(movement), "Stock movement retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<StockMovementResponse>> getAllStockMovements() {
        return getStockMovementHistory(null, null, null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<StockMovementResponse>> getStockMovementHistory(
            StockMovementType type,
            UUID materialId,
            UUID supplierId,
            Instant from,
            Instant to
    ) {
        List<StockMovement> movements = stockMovementRepository.findHistory(type, materialId, supplierId);

        Stream<StockMovement> stream = movements.stream();
        if (from != null) {
            stream = stream.filter(m -> !m.getCreatedAt().isBefore(from));
        }
        if (to != null) {
            stream = stream.filter(m -> !m.getCreatedAt().isAfter(to));
        }

        List<StockMovementResponse> result = stream
                .map(StockMovementResponse::fromEntity)
                .toList();

        return ApiResponse.success(result, "Stock movements retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<StockMovementResponse>> getStockMovementsByMaterialId(UUID materialId) {
        if (!materialRepository.existsById(materialId)) {
            throw new MaterialNotExistsException("Material does not exist");
        }

        List<StockMovementResponse> movements = stockMovementRepository.findByMaterialIdWithDetails(materialId).stream()
                .map(StockMovementResponse::fromEntity)
                .toList();

        return ApiResponse.success(movements, "Stock movements retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<StockMovementResponse>> getStockMovementsBySupplierId(UUID supplierId) {
        if (!supplierRepository.existsById(supplierId)) {
            throw new SupplierNotExistsException("Supplier does not exist");
        }

        List<StockMovementResponse> movements = stockMovementRepository.findBySupplierIdWithDetails(supplierId).stream()
                .map(StockMovementResponse::fromEntity)
                .toList();

        return ApiResponse.success(movements, "Supplier stock movements retrieved successfully");
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteStockMovement(UUID id) {
        StockMovement movement = stockMovementRepository.findById(id)
                .orElseThrow(() -> new StockMovementNotExistsException("Stock movement does not exist"));

        if (movement.getType() == StockMovementType.OUT) {
            Material material = movement.getMaterial();
            if (material != null) {
                double currentQuantity = material.getQuantity() != null ? material.getQuantity() : 0.0;
                double movementQuantity = movement.getQuantity() != null ? movement.getQuantity() : 0.0;
                material.setQuantity(currentQuantity + movementQuantity);
                materialRepository.save(material);
            }
        }

        if (movement.getType() == StockMovementType.IN) {
            Material material = movement.getMaterial();
            if (material != null) {
                double currentQuantity = material.getQuantity() != null ? material.getQuantity() : 0.0;
                double movementQuantity = movement.getQuantity() != null ? movement.getQuantity() : 0.0;

                if (currentQuantity < movementQuantity) {
                    throw new CannotDeleteStockMovementException(
                            "Cannot delete stock-in movement because material quantity (" + currentQuantity +
                            ") is less than movement quantity (" + movementQuantity + ")"
                    );
                }

                material.setQuantity(currentQuantity - movementQuantity);
                materialRepository.save(material);
            }
        }

        stockMovementRepository.delete(movement);
        return ApiResponse.success("Stock movement deleted successfully");
    }
}
