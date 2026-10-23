package cbms_backend.material.controller;

import cbms_backend.core.exception.*;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.material.dto.create_stock_in.CreateStockInRequest;
import cbms_backend.material.dto.create_stock_out.CreateStockOutRequest;
import cbms_backend.material.dto.get_stock_movement.StockMovementResponse;
import cbms_backend.material.entity.StockMovementType;
import cbms_backend.material.service.StockMovementService;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StockMovementControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StockMovementService stockMovementService;

    @InjectMocks
    private StockMovementController stockMovementController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(stockMovementController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/stock-movements should return 201 when valid stock-in request")
    void recordStockIn_shouldReturn201_whenValid() throws Exception {
        UUID id = UUID.randomUUID();
        UUID materialId = UUID.randomUUID();
        UUID supplierId = UUID.randomUUID();

        StockMovementResponse response = new StockMovementResponse(
                id,
                materialId,
                "Vải Gấm",
                supplierId,
                "Nhà cung cấp A",
                null,
                StockMovementType.IN,
                50.0,
                150.0,
                Instant.now()
        );

        when(stockMovementService.recordStockIn(any(CreateStockInRequest.class)))
                .thenReturn(ApiResponse.success(response, "Stock in recorded successfully"));

        mockMvc.perform(post("/api/v1/stock-movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "materialId": "%s",
                                    "supplierId": "%s",
                                    "quantity": 50.0
                                }
                                """.formatted(materialId, supplierId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Stock in recorded successfully"))
                .andExpect(jsonPath("$.data.id").value(id.toString()))
                .andExpect(jsonPath("$.data.materialId").value(materialId.toString()))
                .andExpect(jsonPath("$.data.materialName").value("Vải Gấm"))
                .andExpect(jsonPath("$.data.supplierId").value(supplierId.toString()))
                .andExpect(jsonPath("$.data.supplierName").value("Nhà cung cấp A"))
                .andExpect(jsonPath("$.data.type").value("IN"))
                .andExpect(jsonPath("$.data.quantity").value(50.0))
                .andExpect(jsonPath("$.data.currentMaterialQuantity").value(150.0));
    }

    @Test
    @DisplayName("POST /api/v1/stock-movements/in should also return 201 for /in subpath")
    void recordStockIn_shouldReturn201_onInSubpath() throws Exception {
        UUID id = UUID.randomUUID();
        UUID materialId = UUID.randomUUID();

        StockMovementResponse response = new StockMovementResponse(
                id,
                materialId,
                "Vải Voan",
                null,
                null,
                null,
                StockMovementType.IN,
                20.0,
                70.0,
                Instant.now()
        );

        when(stockMovementService.recordStockIn(any(CreateStockInRequest.class)))
                .thenReturn(ApiResponse.success(response, "Stock in recorded successfully"));

        mockMvc.perform(post("/api/v1/stock-movements/in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "materialId": "%s",
                                    "quantity": 20.0
                                }
                                """.formatted(materialId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.type").value("IN"))
                .andExpect(jsonPath("$.data.quantity").value(20.0));
    }

    @Test
    @DisplayName("POST /api/v1/stock-movements should return 400 when materialId is null")
    void recordStockIn_shouldReturn400_whenMaterialIdNull() throws Exception {
        mockMvc.perform(post("/api/v1/stock-movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "quantity": 50.0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Material ID must not be null"));
    }

    @Test
    @DisplayName("POST /api/v1/stock-movements should return 400 when quantity is zero or negative")
    void recordStockIn_shouldReturn400_whenQuantityInvalid() throws Exception {
        UUID materialId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/stock-movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "materialId": "%s",
                                    "quantity": 0.0
                                }
                                """.formatted(materialId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Quantity must be greater than 0"));

        mockMvc.perform(post("/api/v1/stock-movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "materialId": "%s",
                                    "quantity": -5.0
                                }
                                """.formatted(materialId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Quantity must be greater than 0"));
    }

    @Test
    @DisplayName("POST /api/v1/stock-movements should return 404 when material does not exist")
    void recordStockIn_shouldReturn404_whenMaterialNotExists() throws Exception {
        UUID materialId = UUID.randomUUID();

        when(stockMovementService.recordStockIn(any(CreateStockInRequest.class)))
                .thenThrow(new MaterialNotExistsException("Material does not exist"));

        mockMvc.perform(post("/api/v1/stock-movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "materialId": "%s",
                                    "quantity": 10.0
                                }
                                """.formatted(materialId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Material does not exist"));
    }

    @Test
    @DisplayName("POST /api/v1/stock-movements should return 404 when supplier does not exist")
    void recordStockIn_shouldReturn404_whenSupplierNotExists() throws Exception {
        UUID materialId = UUID.randomUUID();
        UUID supplierId = UUID.randomUUID();

        when(stockMovementService.recordStockIn(any(CreateStockInRequest.class)))
                .thenThrow(new SupplierNotExistsException("Supplier does not exist"));

        mockMvc.perform(post("/api/v1/stock-movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "materialId": "%s",
                                    "supplierId": "%s",
                                    "quantity": 10.0
                                }
                                """.formatted(materialId, supplierId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Supplier does not exist"));
    }

    @Test
    @DisplayName("GET /api/v1/stock-movements should return 200 with list of movements")
    void getStockMovementHistory_shouldReturn200() throws Exception {
        StockMovementResponse response = new StockMovementResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Vải Lụa",
                null,
                null,
                null,
                StockMovementType.IN,
                15.0,
                45.0,
                Instant.now()
        );

        when(stockMovementService.getStockMovementHistory(null, null, null, null, null))
                .thenReturn(ApiResponse.success(List.of(response), "Stock movements retrieved successfully"));

        mockMvc.perform(get("/api/v1/stock-movements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Stock movements retrieved successfully"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].materialName").value("Vải Lụa"));
    }

    @Test
    @DisplayName("GET /api/v1/stock-movements with filter params should return 200 with filtered movements")
    void getStockMovementHistory_shouldReturn200_withFilters() throws Exception {
        UUID materialId = UUID.randomUUID();
        UUID supplierId = UUID.randomUUID();
        StockMovementResponse response = new StockMovementResponse(
                UUID.randomUUID(),
                materialId,
                "Vải Voan",
                supplierId,
                "Nhà cung cấp An Phát",
                null,
                StockMovementType.IN,
                100.0,
                200.0,
                Instant.now()
        );

        when(stockMovementService.getStockMovementHistory(eq(StockMovementType.IN), eq(materialId), eq(supplierId), any(), any()))
                .thenReturn(ApiResponse.success(List.of(response), "Stock movements retrieved successfully"));

        mockMvc.perform(get("/api/v1/stock-movements")
                        .param("type", "IN")
                        .param("materialId", materialId.toString())
                        .param("supplierId", supplierId.toString())
                        .param("from", "2026-10-01T00:00:00Z")
                        .param("to", "2026-10-31T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].type").value("IN"))
                .andExpect(jsonPath("$.data[0].supplierName").value("Nhà cung cấp An Phát"));
    }

    @Test
    @DisplayName("GET /api/v1/stock-movements/history should also return 200")
    void getStockMovementHistory_shouldReturn200_onHistoryPath() throws Exception {
        when(stockMovementService.getStockMovementHistory(null, null, null, null, null))
                .thenReturn(ApiResponse.success(List.of(), "Stock movements retrieved successfully"));

        mockMvc.perform(get("/api/v1/stock-movements/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Stock movements retrieved successfully"));
    }

    @Test
    @DisplayName("GET /api/v1/stock-movements/{id} should return 200 when found")
    void getStockMovementById_shouldReturn200_whenFound() throws Exception {
        UUID id = UUID.randomUUID();
        StockMovementResponse response = new StockMovementResponse(
                id,
                UUID.randomUUID(),
                "Vải Lụa",
                null,
                null,
                null,
                StockMovementType.IN,
                15.0,
                45.0,
                Instant.now()
        );

        when(stockMovementService.getStockMovementById(id))
                .thenReturn(ApiResponse.success(response, "Stock movement retrieved successfully"));

        mockMvc.perform(get("/api/v1/stock-movements/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id.toString()))
                .andExpect(jsonPath("$.data.materialName").value("Vải Lụa"));
    }

    @Test
    @DisplayName("GET /api/v1/stock-movements/{id} should return 404 when not found")
    void getStockMovementById_shouldReturn404_whenNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        when(stockMovementService.getStockMovementById(id))
                .thenThrow(new StockMovementNotExistsException("Stock movement does not exist"));

        mockMvc.perform(get("/api/v1/stock-movements/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Stock movement does not exist"));
    }

    @Test
    @DisplayName("GET /api/v1/stock-movements/material/{materialId} should return 200 when found")
    void getStockMovementsByMaterialId_shouldReturn200() throws Exception {
        UUID materialId = UUID.randomUUID();
        StockMovementResponse response = new StockMovementResponse(
                UUID.randomUUID(),
                materialId,
                "Vải Lụa",
                null,
                null,
                null,
                StockMovementType.IN,
                15.0,
                45.0,
                Instant.now()
        );

        when(stockMovementService.getStockMovementsByMaterialId(materialId))
                .thenReturn(ApiResponse.success(List.of(response), "Stock movements retrieved successfully"));

        mockMvc.perform(get("/api/v1/stock-movements/material/{materialId}", materialId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].materialId").value(materialId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/stock-movements/material/{materialId} should return 404 when material not found")
    void getStockMovementsByMaterialId_shouldReturn404_whenMaterialNotExists() throws Exception {
        UUID materialId = UUID.randomUUID();

        when(stockMovementService.getStockMovementsByMaterialId(materialId))
                .thenThrow(new MaterialNotExistsException("Material does not exist"));

        mockMvc.perform(get("/api/v1/stock-movements/material/{materialId}", materialId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Material does not exist"));
    }

    @Test
    @DisplayName("GET /api/v1/stock-movements/supplier/{supplierId} should return 200 when found")
    void getStockMovementsBySupplierId_shouldReturn200() throws Exception {
        UUID supplierId = UUID.randomUUID();
        StockMovementResponse response = new StockMovementResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Vải Bố",
                supplierId,
                "Nhà cung cấp Sợi Việt",
                null,
                StockMovementType.IN,
                80.0,
                180.0,
                Instant.now()
        );

        when(stockMovementService.getStockMovementsBySupplierId(supplierId))
                .thenReturn(ApiResponse.success(List.of(response), "Supplier stock movements retrieved successfully"));

        mockMvc.perform(get("/api/v1/stock-movements/supplier/{supplierId}", supplierId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].supplierId").value(supplierId.toString()))
                .andExpect(jsonPath("$.data[0].supplierName").value("Nhà cung cấp Sợi Việt"))
                .andExpect(jsonPath("$.data[0].quantity").value(80.0));
    }

    @Test
    @DisplayName("GET /api/v1/stock-movements/supplier/{supplierId} should return 404 when supplier not found")
    void getStockMovementsBySupplierId_shouldReturn404_whenSupplierNotFound() throws Exception {
        UUID supplierId = UUID.randomUUID();

        when(stockMovementService.getStockMovementsBySupplierId(supplierId))
                .thenThrow(new SupplierNotExistsException("Supplier does not exist"));

        mockMvc.perform(get("/api/v1/stock-movements/supplier/{supplierId}", supplierId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Supplier does not exist"));
    }

    @Test
    @DisplayName("DELETE /api/v1/stock-movements/{id} should return 200 when deleted successfully")
    void deleteStockMovement_shouldReturn200_whenSuccess() throws Exception {
        UUID id = UUID.randomUUID();

        when(stockMovementService.deleteStockMovement(id))
                .thenReturn(ApiResponse.success("Stock movement deleted successfully"));

        mockMvc.perform(delete("/api/v1/stock-movements/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Stock movement deleted successfully"));
    }

    @Test
    @DisplayName("DELETE /api/v1/stock-movements/{id} should return 404 when stock movement not found")
    void deleteStockMovement_shouldReturn404_whenNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        when(stockMovementService.deleteStockMovement(id))
                .thenThrow(new StockMovementNotExistsException("Stock movement does not exist"));

        mockMvc.perform(delete("/api/v1/stock-movements/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Stock movement does not exist"));
    }

    @Test
    @DisplayName("DELETE /api/v1/stock-movements/{id} should return 400 when deleting is not permitted")
    void deleteStockMovement_shouldReturn400_whenCannotDelete() throws Exception {
        UUID id = UUID.randomUUID();

        when(stockMovementService.deleteStockMovement(id))
                .thenThrow(new CannotDeleteStockMovementException("Deleting stock-out movements is not supported yet until Order workflow is implemented"));

        mockMvc.perform(delete("/api/v1/stock-movements/{id}", id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Deleting stock-out movements is not supported yet until Order workflow is implemented"));
    }

    @Test
    @DisplayName("POST /api/v1/stock-movements/out should return 201 when valid")
    void recordStockOut_shouldReturn201_whenValid() throws Exception {
        UUID materialId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        StockMovementResponse response = new StockMovementResponse(
                UUID.randomUUID(),
                materialId,
                "Vải Gấm",
                null,
                null,
                orderId,
                StockMovementType.OUT,
                15.0,
                35.0,
                Instant.now()
        );

        when(stockMovementService.recordStockOut(any(CreateStockOutRequest.class)))
                .thenReturn(ApiResponse.success(response, "Stock out recorded successfully"));

        mockMvc.perform(post("/api/v1/stock-movements/out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "materialId": "%s",
                                    "orderId": "%s",
                                    "quantity": 15.0
                                }
                                """.formatted(materialId, orderId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.type").value("OUT"))
                .andExpect(jsonPath("$.data.quantity").value(15.0))
                .andExpect(jsonPath("$.data.orderId").value(orderId.toString()));
    }

    @Test
    @DisplayName("POST /api/v1/stock-movements/out should return 400 when insufficient stock")
    void recordStockOut_shouldReturn400_whenInsufficientStock() throws Exception {
        UUID materialId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(stockMovementService.recordStockOut(any(CreateStockOutRequest.class)))
                .thenThrow(new InsufficientStockException("Insufficient material quantity in stock"));

        mockMvc.perform(post("/api/v1/stock-movements/out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "materialId": "%s",
                                    "orderId": "%s",
                                    "quantity": 100.0
                                }
                                """.formatted(materialId, orderId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient material quantity in stock"));
    }
}
