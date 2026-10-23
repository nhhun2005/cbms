package cbms_backend.order.controller;

import cbms_backend.core.exception.CannotDeleteLastCurtainException;
import cbms_backend.core.exception.CannotModifyCurtainException;
import cbms_backend.core.exception.CurtainNotExistsException;
import cbms_backend.core.exception.GlobalExceptionHandler;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.order.dto.curtain.CreateCurtainRequest;
import cbms_backend.order.dto.curtain.UpdateCurtainRequest;
import cbms_backend.order.dto.get_order.CurtainResponse;
import cbms_backend.order.service.CurtainService;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CurtainControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CurtainService curtainService;

    @InjectMocks
    private CurtainController curtainController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(curtainController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/curtains should return 201 when valid")
    void addCurtain_shouldReturn201() throws Exception {
        UUID curtainId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        CurtainResponse response = new CurtainResponse(curtainId, orderId, 2.0, 3.0);
        when(curtainService.addCurtain(any(CreateCurtainRequest.class)))
                .thenReturn(ApiResponse.success(response, "Curtain added successfully"));

        mockMvc.perform(post("/api/v1/curtains")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "orderId": "%s",
                                    "width": 2.0,
                                    "height": 3.0
                                }
                                """.formatted(orderId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.width").value(2.0))
                .andExpect(jsonPath("$.data.height").value(3.0));
    }

    @Test
    @DisplayName("POST /api/v1/curtains should return 400 when order is completed")
    void addCurtain_shouldReturn400_whenCompleted() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(curtainService.addCurtain(any(CreateCurtainRequest.class)))
                .thenThrow(new CannotModifyCurtainException("Cannot add curtain to a completed order"));

        mockMvc.perform(post("/api/v1/curtains")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "orderId": "%s",
                                    "width": 2.0,
                                    "height": 3.0
                                }
                                """.formatted(orderId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot add curtain to a completed order"));
    }

    @Test
    @DisplayName("PUT /api/v1/curtains/{id} should return 200 when valid")
    void updateCurtain_shouldReturn200() throws Exception {
        UUID curtainId = UUID.randomUUID();
        CurtainResponse response = new CurtainResponse(curtainId, UUID.randomUUID(), 3.5, 4.0);

        when(curtainService.updateCurtain(eq(curtainId), any(UpdateCurtainRequest.class)))
                .thenReturn(ApiResponse.success(response, "Curtain updated successfully"));

        mockMvc.perform(put("/api/v1/curtains/{id}", curtainId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "width": 3.5,
                                    "height": 4.0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.width").value(3.5));
    }

    @Test
    @DisplayName("DELETE /api/v1/curtains/{id} should return 200 when deleted")
    void deleteCurtain_shouldReturn200() throws Exception {
        UUID curtainId = UUID.randomUUID();
        when(curtainService.deleteCurtain(curtainId))
                .thenReturn(ApiResponse.success("Curtain deleted successfully"));

        mockMvc.perform(delete("/api/v1/curtains/{id}", curtainId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Curtain deleted successfully"));
    }

    @Test
    @DisplayName("DELETE /api/v1/curtains/{id} should return 400 when deleting last curtain")
    void deleteCurtain_shouldReturn400_whenLastCurtain() throws Exception {
        UUID curtainId = UUID.randomUUID();
        when(curtainService.deleteCurtain(curtainId))
                .thenThrow(new CannotDeleteLastCurtainException("Cannot delete the last curtain of an order. An order must have at least one curtain"));

        mockMvc.perform(delete("/api/v1/curtains/{id}", curtainId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot delete the last curtain of an order. An order must have at least one curtain"));
    }

    @Test
    @DisplayName("GET /api/v1/curtains/{id} should return 200 when curtain exists")
    void getCurtainById_shouldReturn200() throws Exception {
        UUID curtainId = UUID.randomUUID();
        CurtainResponse response = new CurtainResponse(curtainId, UUID.randomUUID(), 2.0, 3.0);

        when(curtainService.getCurtainById(curtainId))
                .thenReturn(ApiResponse.success(response, "Curtain retrieved successfully"));

        mockMvc.perform(get("/api/v1/curtains/{id}", curtainId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(curtainId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/curtains/{id} should return 404 when not found")
    void getCurtainById_shouldReturn404() throws Exception {
        UUID curtainId = UUID.randomUUID();
        when(curtainService.getCurtainById(curtainId))
                .thenThrow(new CurtainNotExistsException("Curtain does not exist"));

        mockMvc.perform(get("/api/v1/curtains/{id}", curtainId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Curtain does not exist"));
    }

    @Test
    @DisplayName("GET /api/v1/curtains/order/{orderId} should return 200")
    void getCurtainsByOrderId_shouldReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(curtainService.getCurtainsByOrderId(orderId))
                .thenReturn(ApiResponse.success(List.of(), "Curtains retrieved successfully"));

        mockMvc.perform(get("/api/v1/curtains/order/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
