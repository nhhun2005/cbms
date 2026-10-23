package cbms_backend.invoice.controller;

import cbms_backend.core.exception.GlobalExceptionHandler;
import cbms_backend.core.exception.InvoiceAlreadyExistsException;
import cbms_backend.core.exception.InvoiceNotExistsException;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.invoice.dto.CreateCustomPriceInvoiceRequest;
import cbms_backend.invoice.dto.CreateInvoiceRequest;
import cbms_backend.invoice.dto.InvoiceResponse;
import cbms_backend.invoice.dto.UpdateInvoiceRequest;
import cbms_backend.invoice.service.InvoiceService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InvoiceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InvoiceService invoiceService;

    @InjectMocks
    private InvoiceController invoiceController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(invoiceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/invoices should return 201 when valid request")
    void createInvoice_shouldReturn201_whenValid() throws Exception {
        UUID invoiceId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        InvoiceResponse response = new InvoiceResponse(
                invoiceId,
                orderId,
                customerId,
                "Customer Name",
                "0912345678",
                1000000.0,
                0.10,
                10.0,
                990000.0,
                Instant.now()
        );

        when(invoiceService.createInvoice(any(CreateInvoiceRequest.class)))
                .thenReturn(ApiResponse.success(response, "Invoice created successfully"));

        mockMvc.perform(post("/api/v1/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "orderId": "%s",
                                    "discount": 10.0
                                }
                                """.formatted(orderId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Invoice created successfully"))
                .andExpect(jsonPath("$.data.subtotal").value(1000000.0))
                .andExpect(jsonPath("$.data.discount").value(10.0))
                .andExpect(jsonPath("$.data.total").value(990000.0));
    }

    @Test
    @DisplayName("POST /api/v1/invoices/custom-price should return 201 when valid request")
    void createCustomPriceInvoice_shouldReturn201_whenValid() throws Exception {
        UUID invoiceId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        InvoiceResponse response = new InvoiceResponse(
                invoiceId,
                orderId,
                customerId,
                "Customer Name",
                "0912345678",
                1000000.0,
                0.10,
                10.0,
                990000.0,
                Instant.now()
        );

        when(invoiceService.createCustomPriceInvoice(any(CreateCustomPriceInvoiceRequest.class)))
                .thenReturn(ApiResponse.success(response, "Invoice with custom price created successfully"));

        mockMvc.perform(post("/api/v1/invoices/custom-price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "orderId": "%s",
                                    "customPrice": 990000.0
                                }
                                """.formatted(orderId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Invoice with custom price created successfully"))
                .andExpect(jsonPath("$.data.total").value(990000.0))
                .andExpect(jsonPath("$.data.discount").value(10.0));
    }

    @Test
    @DisplayName("POST /api/v1/invoices should return 400 when orderId is null")
    void createInvoice_shouldReturn400_whenOrderIdNull() throws Exception {
        mockMvc.perform(post("/api/v1/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "discount": 10.0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Order ID must not be null"));
    }

    @Test
    @DisplayName("POST /api/v1/invoices should return 400 when discount is out of range 0-100")
    void createInvoice_shouldReturn400_whenDiscountOutOfRange() throws Exception {
        mockMvc.perform(post("/api/v1/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "orderId": "%s",
                                    "discount": 120.0
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Discount must be between 0 and 100"));
    }

    @Test
    @DisplayName("POST /api/v1/invoices/custom-price should return 400 when customPrice is negative")
    void createCustomPriceInvoice_shouldReturn400_whenCustomPriceNegative() throws Exception {
        mockMvc.perform(post("/api/v1/invoices/custom-price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "orderId": "%s",
                                    "customPrice": -500.0
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Custom price must be greater than or equal to 0"));
    }

    @Test
    @DisplayName("POST /api/v1/invoices should return 409 when invoice already exists")
    void createInvoice_shouldReturn409_whenAlreadyExists() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(invoiceService.createInvoice(any(CreateInvoiceRequest.class)))
                .thenThrow(new InvoiceAlreadyExistsException("Invoice already exists for this order"));

        mockMvc.perform(post("/api/v1/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "orderId": "%s"
                                }
                                """.formatted(orderId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Invoice already exists for this order"));
    }

    @Test
    @DisplayName("GET /api/v1/invoices/{id} should return 200 when found")
    void getInvoiceById_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        InvoiceResponse response = new InvoiceResponse(
                id,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Customer",
                "0912345678",
                500000.0,
                0.10,
                0.0,
                550000.0,
                Instant.now()
        );

        when(invoiceService.getInvoiceById(id))
                .thenReturn(ApiResponse.success(response, "Invoice retrieved successfully"));

        mockMvc.perform(get("/api/v1/invoices/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/invoices/{id} should return 404 when not found")
    void getInvoiceById_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        when(invoiceService.getInvoiceById(id))
                .thenThrow(new InvoiceNotExistsException("Invoice does not exist"));

        mockMvc.perform(get("/api/v1/invoices/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Invoice does not exist"));
    }

    @Test
    @DisplayName("GET /api/v1/invoices/order/{orderId} should return 200")
    void getInvoiceByOrderId_shouldReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        InvoiceResponse response = new InvoiceResponse(
                UUID.randomUUID(),
                orderId,
                UUID.randomUUID(),
                "Customer",
                "0912345678",
                500000.0,
                0.10,
                0.0,
                550000.0,
                Instant.now()
        );

        when(invoiceService.getInvoiceByOrderId(orderId))
                .thenReturn(ApiResponse.success(response, "Invoice retrieved successfully"));

        mockMvc.perform(get("/api/v1/invoices/order/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderId").value(orderId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/invoices/customer/{customerId} should return 200")
    void getInvoicesByCustomerId_shouldReturn200() throws Exception {
        UUID customerId = UUID.randomUUID();
        when(invoiceService.getInvoicesByCustomerId(customerId))
                .thenReturn(ApiResponse.success(List.of(), "Customer invoices retrieved successfully"));

        mockMvc.perform(get("/api/v1/invoices/customer/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/invoices should return 200")
    void getAllInvoices_shouldReturn200() throws Exception {
        when(invoiceService.getAllInvoices())
                .thenReturn(ApiResponse.success(List.of(), "Invoices retrieved successfully"));

        mockMvc.perform(get("/api/v1/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("PUT /api/v1/invoices/{id} should return 200 when updating with discount")
    void updateInvoice_shouldReturn200_withDiscount() throws Exception {
        UUID invoiceId = UUID.randomUUID();
        InvoiceResponse response = new InvoiceResponse(
                invoiceId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Customer",
                "0912345678",
                1000000.0,
                0.10,
                15.0,
                935000.0,
                Instant.now()
        );

        when(invoiceService.updateInvoice(eq(invoiceId), any(UpdateInvoiceRequest.class)))
                .thenReturn(ApiResponse.success(response, "Invoice updated successfully"));

        mockMvc.perform(put("/api/v1/invoices/{id}", invoiceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "discount": 15.0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Invoice updated successfully"))
                .andExpect(jsonPath("$.data.discount").value(15.0))
                .andExpect(jsonPath("$.data.total").value(935000.0));
    }

    @Test
    @DisplayName("PUT /api/v1/invoices/{id} should return 200 when updating with customPrice")
    void updateInvoice_shouldReturn200_withCustomPrice() throws Exception {
        UUID invoiceId = UUID.randomUUID();
        InvoiceResponse response = new InvoiceResponse(
                invoiceId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Customer",
                "0912345678",
                1000000.0,
                0.10,
                10.0,
                990000.0,
                Instant.now()
        );

        when(invoiceService.updateInvoice(eq(invoiceId), any(UpdateInvoiceRequest.class)))
                .thenReturn(ApiResponse.success(response, "Invoice updated successfully"));

        mockMvc.perform(put("/api/v1/invoices/{id}", invoiceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "customPrice": 990000.0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Invoice updated successfully"))
                .andExpect(jsonPath("$.data.total").value(990000.0));
    }

    @Test
    @DisplayName("PUT /api/v1/invoices/order/{orderId} should return 200")
    void updateInvoiceByOrderId_shouldReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        InvoiceResponse response = new InvoiceResponse(
                UUID.randomUUID(),
                orderId,
                UUID.randomUUID(),
                "Customer",
                "0912345678",
                1000000.0,
                0.10,
                0.0,
                1100000.0,
                Instant.now()
        );

        when(invoiceService.updateInvoiceByOrderId(eq(orderId), any(UpdateInvoiceRequest.class)))
                .thenReturn(ApiResponse.success(response, "Invoice updated successfully"));

        mockMvc.perform(put("/api/v1/invoices/order/{orderId}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "discount": 0.0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Invoice updated successfully"));
    }

    @Test
    @DisplayName("PUT /api/v1/invoices/{id} should return 400 when discount is invalid")
    void updateInvoice_shouldReturn400_whenDiscountInvalid() throws Exception {
        mockMvc.perform(put("/api/v1/invoices/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "discount": 150.0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Discount must be between 0 and 100"));
    }

    @Test
    @DisplayName("PUT /api/v1/invoices/{id} should return 404 when invoice not found")
    void updateInvoice_shouldReturn404_whenNotFound() throws Exception {
        UUID invoiceId = UUID.randomUUID();
        when(invoiceService.updateInvoice(eq(invoiceId), any(UpdateInvoiceRequest.class)))
                .thenThrow(new InvoiceNotExistsException("Invoice does not exist"));

        mockMvc.perform(put("/api/v1/invoices/{id}", invoiceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "discount": 10.0
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Invoice does not exist"));
    }
}
