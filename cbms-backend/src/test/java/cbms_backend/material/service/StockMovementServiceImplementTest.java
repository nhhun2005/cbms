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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class StockMovementServiceImplementTest {

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private StockMovementServiceImplement stockMovementService;

    @Test
    @DisplayName("recordStockIn should increase material quantity and persist StockMovement with type IN")
    void recordStockIn_shouldSucceed_andIncreaseMaterialQuantity() {
        UUID materialId = UUID.randomUUID();
        UUID supplierId = UUID.randomUUID();

        Material material = new Material();
        material.setId(materialId);
        material.setName("Vải Voan");
        material.setQuantity(20.0);

        Supplier supplier = new Supplier(supplierId, "Công ty Vải Đẹp", "0987654321");

        CreateStockInRequest request = new CreateStockInRequest(materialId, supplierId, 30.5);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> {
            StockMovement sm = invocation.getArgument(0);
            sm.setId(UUID.randomUUID());
            return sm;
        });

        ApiResponse<StockMovementResponse> response = stockMovementService.recordStockIn(request);

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals("Stock in recorded successfully", response.getMessage());

        // Verify material quantity increased: 20.0 + 30.5 = 50.5
        assertEquals(50.5, material.getQuantity());
        verify(materialRepository).save(material);

        // Verify StockMovement captured
        ArgumentCaptor<StockMovement> movementCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(movementCaptor.capture());
        StockMovement savedMovement = movementCaptor.getValue();

        assertEquals(material, savedMovement.getMaterial());
        assertEquals(supplier, savedMovement.getSupplier());
        assertEquals(StockMovementType.IN, savedMovement.getType());
        assertEquals(30.5, savedMovement.getQuantity());

        // Verify response payload
        assertEquals(StockMovementType.IN, response.getData().type());
        assertEquals(30.5, response.getData().quantity());
        assertEquals("Vải Voan", response.getData().materialName());
        assertEquals("Công ty Vải Đẹp", response.getData().supplierName());
        assertEquals(50.5, response.getData().currentMaterialQuantity());
    }

    @Test
    @DisplayName("recordStockIn should succeed when supplier is null")
    void recordStockIn_shouldSucceed_whenSupplierIsNull() {
        UUID materialId = UUID.randomUUID();

        Material material = new Material();
        material.setId(materialId);
        material.setName("Vải Bố");
        material.setQuantity(10.0);

        CreateStockInRequest request = new CreateStockInRequest(materialId, 15.0);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> {
            StockMovement sm = invocation.getArgument(0);
            sm.setId(UUID.randomUUID());
            return sm;
        });

        ApiResponse<StockMovementResponse> response = stockMovementService.recordStockIn(request);

        assertNotNull(response);
        assertEquals(25.0, material.getQuantity());
        assertNull(response.getData().supplierId());
        assertNull(response.getData().supplierName());
        assertEquals(StockMovementType.IN, response.getData().type());
        verify(supplierRepository, never()).findById(any());
    }

    @Test
    @DisplayName("recordStockIn should throw MaterialNotExistsException when material not found")
    void recordStockIn_shouldThrowMaterialNotExistsException() {
        UUID materialId = UUID.randomUUID();
        CreateStockInRequest request = new CreateStockInRequest(materialId, 10.0);

        when(materialRepository.findById(materialId)).thenReturn(Optional.empty());

        assertThrows(MaterialNotExistsException.class, () -> stockMovementService.recordStockIn(request));
        verify(materialRepository, never()).save(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    @DisplayName("recordStockIn should throw SupplierNotExistsException when supplierId not found")
    void recordStockIn_shouldThrowSupplierNotExistsException() {
        UUID materialId = UUID.randomUUID();
        UUID supplierId = UUID.randomUUID();

        Material material = new Material();
        material.setId(materialId);

        CreateStockInRequest request = new CreateStockInRequest(materialId, supplierId, 10.0);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.empty());

        assertThrows(SupplierNotExistsException.class, () -> stockMovementService.recordStockIn(request));
        verify(materialRepository, never()).save(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    @DisplayName("getStockMovementById should return movement when found")
    void getStockMovementById_shouldReturn_whenFound() {
        UUID id = UUID.randomUUID();
        Material material = new Material();
        material.setId(UUID.randomUUID());
        material.setName("Vải Lụa");

        StockMovement movement = new StockMovement();
        movement.setId(id);
        movement.setMaterial(material);
        movement.setType(StockMovementType.IN);
        movement.setQuantity(40.0);
        movement.setCreatedAt(Instant.now());

        when(stockMovementRepository.findByIdWithDetails(id)).thenReturn(Optional.of(movement));

        ApiResponse<StockMovementResponse> response = stockMovementService.getStockMovementById(id);

        assertNotNull(response);
        assertEquals(id, response.getData().id());
        assertEquals("Vải Lụa", response.getData().materialName());
        assertEquals(StockMovementType.IN, response.getData().type());
        assertEquals(40.0, response.getData().quantity());
    }

    @Test
    @DisplayName("getStockMovementById should throw StockMovementNotExistsException when not found")
    void getStockMovementById_shouldThrow_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(stockMovementRepository.findByIdWithDetails(id)).thenReturn(Optional.empty());

        assertThrows(StockMovementNotExistsException.class, () -> stockMovementService.getStockMovementById(id));
    }

    @Test
    @DisplayName("getAllStockMovements should return list of movements")
    void getAllStockMovements_shouldReturnList() {
        Material material = new Material();
        material.setId(UUID.randomUUID());
        material.setName("Vải Lụa");

        StockMovement m1 = new StockMovement();
        m1.setId(UUID.randomUUID());
        m1.setMaterial(material);
        m1.setType(StockMovementType.IN);
        m1.setQuantity(10.0);

        StockMovement m2 = new StockMovement();
        m2.setId(UUID.randomUUID());
        m2.setMaterial(material);
        m2.setType(StockMovementType.IN);
        m2.setQuantity(20.0);

        when(stockMovementRepository.findWarehouseHistory(null, null, null)).thenReturn(List.of(m1, m2));

        ApiResponse<List<StockMovementResponse>> response = stockMovementService.getAllStockMovements();

        assertNotNull(response);
        assertEquals(2, response.getData().size());
    }

    @Test
    @DisplayName("getWarehouseHistory should filter movements by type, materialId, supplierId, and date range")
    void getWarehouseHistory_shouldFilterCorrectly() {
        UUID materialId = UUID.randomUUID();
        UUID supplierId = UUID.randomUUID();
        Material material = new Material();
        material.setId(materialId);
        material.setName("Vải Đũi");

        Supplier supplier = new Supplier(supplierId, "Công ty Vải Đũi", "0912345678");

        Instant now = Instant.now();
        Instant past = now.minusSeconds(3600 * 24); // 1 day ago
        Instant future = now.plusSeconds(3600 * 24);

        StockMovement inRange = new StockMovement();
        inRange.setId(UUID.randomUUID());
        inRange.setMaterial(material);
        inRange.setSupplier(supplier);
        inRange.setType(StockMovementType.IN);
        inRange.setQuantity(50.0);
        inRange.setCreatedAt(now);

        StockMovement tooOld = new StockMovement();
        tooOld.setId(UUID.randomUUID());
        tooOld.setMaterial(material);
        tooOld.setSupplier(supplier);
        tooOld.setType(StockMovementType.IN);
        tooOld.setQuantity(30.0);
        tooOld.setCreatedAt(past.minusSeconds(3600));

        when(stockMovementRepository.findWarehouseHistory(StockMovementType.IN, materialId, supplierId))
                .thenReturn(List.of(inRange, tooOld));

        ApiResponse<List<StockMovementResponse>> response = stockMovementService.getWarehouseHistory(
                StockMovementType.IN,
                materialId,
                supplierId,
                past,
                future
        );

        assertNotNull(response);
        assertEquals(1, response.getData().size());
        assertEquals(inRange.getId(), response.getData().get(0).id());
        assertEquals(50.0, response.getData().get(0).quantity());
    }

    @Test
    @DisplayName("getWarehouseSummary should compute totals for IN, OUT, ADJUSTMENT, and distinct materials")
    void getWarehouseSummary_shouldComputeTotals() {
        Material m1 = new Material();
        m1.setId(UUID.randomUUID());
        Material m2 = new Material();
        m2.setId(UUID.randomUUID());

        StockMovement smIn = new StockMovement();
        smIn.setId(UUID.randomUUID());
        smIn.setMaterial(m1);
        smIn.setType(StockMovementType.IN);
        smIn.setQuantity(100.0);

        StockMovement smOut = new StockMovement();
        smOut.setId(UUID.randomUUID());
        smOut.setMaterial(m1);
        smOut.setType(StockMovementType.OUT);
        smOut.setQuantity(25.0);

        StockMovement smAdj = new StockMovement();
        smAdj.setId(UUID.randomUUID());
        smAdj.setMaterial(m2);
        smAdj.setType(StockMovementType.ADJUSTMENT);
        smAdj.setQuantity(5.0);

        when(stockMovementRepository.findAllWithDetails()).thenReturn(List.of(smIn, smOut, smAdj));

        var response = stockMovementService.getWarehouseSummary();

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(3, response.getData().totalMovements());
        assertEquals(100.0, response.getData().totalInQuantity());
        assertEquals(25.0, response.getData().totalOutQuantity());
        assertEquals(5.0, response.getData().totalAdjustmentQuantity());
        assertEquals(2, response.getData().distinctMaterialsCount());
        assertEquals(3, response.getData().recentMovements().size());
    }

    @Test
    @DisplayName("getStockMovementsByMaterialId should return movements when material exists")
    void getStockMovementsByMaterialId_shouldReturn_whenExists() {
        UUID materialId = UUID.randomUUID();
        Material material = new Material();
        material.setId(materialId);
        material.setName("Vải Lụa");

        StockMovement m1 = new StockMovement();
        m1.setId(UUID.randomUUID());
        m1.setMaterial(material);
        m1.setType(StockMovementType.IN);
        m1.setQuantity(15.0);

        when(materialRepository.existsById(materialId)).thenReturn(true);
        when(stockMovementRepository.findByMaterialIdWithDetails(materialId)).thenReturn(List.of(m1));

        ApiResponse<List<StockMovementResponse>> response = stockMovementService.getStockMovementsByMaterialId(materialId);

        assertNotNull(response);
        assertEquals(1, response.getData().size());
        assertEquals("Vải Lụa", response.getData().get(0).materialName());
    }

    @Test
    @DisplayName("getStockMovementsByMaterialId should throw MaterialNotExistsException when material not found")
    void getStockMovementsByMaterialId_shouldThrow_whenMaterialNotFound() {
        UUID materialId = UUID.randomUUID();
        when(materialRepository.existsById(materialId)).thenReturn(false);

        assertThrows(MaterialNotExistsException.class, () -> stockMovementService.getStockMovementsByMaterialId(materialId));
    }

    @Test
    @DisplayName("getStockMovementsBySupplierId should return movements when supplier exists")
    void getStockMovementsBySupplierId_shouldReturn_whenSupplierExists() {
        UUID supplierId = UUID.randomUUID();
        Supplier supplier = new Supplier(supplierId, "Công ty Rèm Thêu", "0987654321");
        Material material = new Material();
        material.setId(UUID.randomUUID());
        material.setName("Vải Voan");

        StockMovement m1 = new StockMovement();
        m1.setId(UUID.randomUUID());
        m1.setMaterial(material);
        m1.setSupplier(supplier);
        m1.setType(StockMovementType.IN);
        m1.setQuantity(60.0);

        when(supplierRepository.existsById(supplierId)).thenReturn(true);
        when(stockMovementRepository.findBySupplierIdWithDetails(supplierId)).thenReturn(List.of(m1));

        ApiResponse<List<StockMovementResponse>> response = stockMovementService.getStockMovementsBySupplierId(supplierId);

        assertNotNull(response);
        assertEquals(1, response.getData().size());
        assertEquals("Công ty Rèm Thêu", response.getData().get(0).supplierName());
        assertEquals("Vải Voan", response.getData().get(0).materialName());
    }

    @Test
    @DisplayName("getStockMovementsBySupplierId should throw SupplierNotExistsException when supplier not found")
    void getStockMovementsBySupplierId_shouldThrow_whenSupplierNotFound() {
        UUID supplierId = UUID.randomUUID();
        when(supplierRepository.existsById(supplierId)).thenReturn(false);

        assertThrows(SupplierNotExistsException.class, () -> stockMovementService.getStockMovementsBySupplierId(supplierId));
    }

    @Test
    @DisplayName("deleteStockMovement should deduct material quantity and delete movement when type is IN")
    void deleteStockMovement_shouldSucceed_whenTypeIsIN() {
        UUID id = UUID.randomUUID();
        Material material = new Material();
        material.setId(UUID.randomUUID());
        material.setName("Vải Dạ");
        material.setQuantity(100.0);

        StockMovement movement = new StockMovement();
        movement.setId(id);
        movement.setMaterial(material);
        movement.setType(StockMovementType.IN);
        movement.setQuantity(30.0);

        when(stockMovementRepository.findById(id)).thenReturn(Optional.of(movement));

        ApiResponse<Void> response = stockMovementService.deleteStockMovement(id);

        assertNotNull(response);
        assertEquals("Stock movement deleted successfully", response.getMessage());

        // Verify quantity was reduced by 30: 100 - 30 = 70
        assertEquals(70.0, material.getQuantity());
        verify(materialRepository).save(material);
        verify(stockMovementRepository).delete(movement);
    }

    @Test
    @DisplayName("deleteStockMovement should restore material quantity when type is OUT")
    void deleteStockMovement_shouldRestoreQuantity_whenTypeIsOUT() {
        UUID id = UUID.randomUUID();
        Material material = new Material();
        material.setId(UUID.randomUUID());
        material.setQuantity(40.0);

        StockMovement movement = new StockMovement();
        movement.setId(id);
        movement.setMaterial(material);
        movement.setType(StockMovementType.OUT);
        movement.setQuantity(15.0);

        when(stockMovementRepository.findById(id)).thenReturn(Optional.of(movement));

        ApiResponse<Void> response = stockMovementService.deleteStockMovement(id);

        assertNotNull(response);
        assertEquals("Stock movement deleted successfully", response.getMessage());
        assertEquals(55.0, material.getQuantity());
        verify(materialRepository).save(material);
        verify(stockMovementRepository).delete(movement);
    }

    @Test
    @DisplayName("recordStockOut should decrease material quantity and create OUT movement")
    void recordStockOut_shouldSucceed_andDecreaseMaterialQuantity() {
        UUID materialId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Material material = new Material();
        material.setId(materialId);
        material.setQuantity(50.0);

        Order order = new Order();
        order.setId(orderId);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> {
            StockMovement sm = invocation.getArgument(0);
            sm.setId(UUID.randomUUID());
            return sm;
        });

        CreateStockOutRequest request = new CreateStockOutRequest(materialId, orderId, 20.0);
        ApiResponse<StockMovementResponse> response = stockMovementService.recordStockOut(request);

        assertNotNull(response);
        assertEquals("Stock out recorded successfully", response.getMessage());
        assertEquals(30.0, material.getQuantity());
        verify(materialRepository).save(material);
        verify(stockMovementRepository).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("recordStockOut should throw InsufficientStockException when available is less than requested")
    void recordStockOut_shouldThrow_whenInsufficientStock() {
        UUID materialId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Material material = new Material();
        material.setId(materialId);
        material.setQuantity(10.0);

        Order order = new Order();
        order.setId(orderId);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        CreateStockOutRequest request = new CreateStockOutRequest(materialId, orderId, 25.0);
        assertThrows(InsufficientStockException.class, () -> stockMovementService.recordStockOut(request));
        verify(materialRepository, never()).save(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    @DisplayName("recordStockOut should throw MaterialNotExistsException when material not found")
    void recordStockOut_shouldThrow_whenMaterialNotFound() {
        UUID materialId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(materialRepository.findById(materialId)).thenReturn(Optional.empty());

        CreateStockOutRequest request = new CreateStockOutRequest(materialId, orderId, 10.0);
        assertThrows(MaterialNotExistsException.class, () -> stockMovementService.recordStockOut(request));
    }

    @Test
    @DisplayName("recordStockOut should throw OrderNotExistsException when order not found")
    void recordStockOut_shouldThrow_whenOrderNotFound() {
        UUID materialId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Material material = new Material();
        material.setId(materialId);
        material.setQuantity(100.0);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        CreateStockOutRequest request = new CreateStockOutRequest(materialId, orderId, 10.0);
        assertThrows(OrderNotExistsException.class, () -> stockMovementService.recordStockOut(request));
    }

    @Test
    @DisplayName("deleteStockMovement should throw CannotDeleteStockMovementException when material quantity would become negative")
    void deleteStockMovement_shouldThrow_whenQuantityWouldBecomeNegative() {
        UUID id = UUID.randomUUID();
        Material material = new Material();
        material.setId(UUID.randomUUID());
        material.setQuantity(10.0);

        StockMovement movement = new StockMovement();
        movement.setId(id);
        movement.setMaterial(material);
        movement.setType(StockMovementType.IN);
        movement.setQuantity(50.0);

        when(stockMovementRepository.findById(id)).thenReturn(Optional.of(movement));

        CannotDeleteStockMovementException ex = assertThrows(
                CannotDeleteStockMovementException.class,
                () -> stockMovementService.deleteStockMovement(id)
        );

        assertTrue(ex.getMessage().contains("less than movement quantity"));
        verify(materialRepository, never()).save(any());
        verify(stockMovementRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteStockMovement should throw StockMovementNotExistsException when not found")
    void deleteStockMovement_shouldThrow_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(stockMovementRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(StockMovementNotExistsException.class, () -> stockMovementService.deleteStockMovement(id));
        verify(stockMovementRepository, never()).delete(any());
    }
}
