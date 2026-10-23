package cbms_backend.material.controller;

import cbms_backend.core.exception.GlobalExceptionHandler;
import cbms_backend.core.exception.SupplierAlreadyExistsException;
import cbms_backend.core.exception.SupplierInUseException;
import cbms_backend.core.exception.SupplierNotExistsException;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.material.dto.create_supplier.CreateSupplierRequest;
import cbms_backend.material.dto.get_supplier.SupplierResponse;
import cbms_backend.material.dto.update_supplier.UpdateSupplierRequest;
import cbms_backend.material.service.SupplierService;
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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SupplierControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SupplierService supplierService;

    @InjectMocks
    private SupplierController supplierController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(supplierController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/suppliers should return 201 when contact is a valid VN phone number")
    void createSupplier_shouldReturn201_whenPhoneContact() throws Exception {
        UUID id = UUID.randomUUID();
        SupplierResponse response = new SupplierResponse(id, "Công ty Rèm An Phát", "0987654321");

        when(supplierService.createSupplier(any(CreateSupplierRequest.class)))
                .thenReturn(ApiResponse.success(response, "Supplier created successfully"));

        mockMvc.perform(post("/api/v1/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Công ty Rèm An Phát",
                                    "contact": "0987654321"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Supplier created successfully"))
                .andExpect(jsonPath("$.data.id").value(id.toString()))
                .andExpect(jsonPath("$.data.name").value("Công ty Rèm An Phát"))
                .andExpect(jsonPath("$.data.contact").value("0987654321"));
    }

    @Test
    @DisplayName("POST /api/v1/suppliers should return 201 when contact is a 10-digit phone starting with 0 (e.g. 0354023611)")
    void createSupplier_shouldReturn201_whenNormalizedPhoneContact() throws Exception {
        UUID id = UUID.randomUUID();
        SupplierResponse response = new SupplierResponse(id, "Công ty Rèm An Phát", "0354023611");

        when(supplierService.createSupplier(any(CreateSupplierRequest.class)))
                .thenReturn(ApiResponse.success(response, "Supplier created successfully"));

        mockMvc.perform(post("/api/v1/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Công ty Rèm An Phát",
                                    "contact": "0354023611"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.contact").value("0354023611"));
    }

    @Test
    @DisplayName("POST /api/v1/suppliers should return 400 when contact uses +84 prefix instead of 0...")
    void createSupplier_shouldReturn400_whenPhoneStartsWithPlus84() throws Exception {
        mockMvc.perform(post("/api/v1/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Công ty Rèm An Phát",
                                    "contact": "+84354023611"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Contact must be a valid email or Vietnamese phone number"));
    }

    @Test
    @DisplayName("POST /api/v1/suppliers should return 400 when contact uses 84 prefix without leading 0")
    void createSupplier_shouldReturn400_whenPhoneStartsWith84WithoutZero() throws Exception {
        mockMvc.perform(post("/api/v1/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Công ty Rèm An Phát",
                                    "contact": "84354023611"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Contact must be a valid email or Vietnamese phone number"));
    }

    @Test
    @DisplayName("POST /api/v1/suppliers should return 201 when contact is a valid email")
    void createSupplier_shouldReturn201_whenEmailContact() throws Exception {
        UUID id = UUID.randomUUID();
        SupplierResponse response = new SupplierResponse(id, "Công ty Rèm Toàn Cầu", "supplier@domain.com.vn");

        when(supplierService.createSupplier(any(CreateSupplierRequest.class)))
                .thenReturn(ApiResponse.success(response, "Supplier created successfully"));

        mockMvc.perform(post("/api/v1/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Công ty Rèm Toàn Cầu",
                                    "contact": "supplier@domain.com.vn"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Supplier created successfully"))
                .andExpect(jsonPath("$.data.name").value("Công ty Rèm Toàn Cầu"))
                .andExpect(jsonPath("$.data.contact").value("supplier@domain.com.vn"));
    }

    @Test
    @DisplayName("POST /api/v1/suppliers should return 400 when name is blank")
    void createSupplier_shouldReturn400_whenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "   ",
                                    "contact": "0987654321"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Supplier name must not be blank"));
    }

    @Test
    @DisplayName("POST /api/v1/suppliers should return 400 when contact is blank")
    void createSupplier_shouldReturn400_whenContactIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Supplier X",
                                    "contact": "   "
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/suppliers should return 400 when contact is invalid format (neither phone nor email)")
    void createSupplier_shouldReturn400_whenContactIsInvalidFormat() throws Exception {
        mockMvc.perform(post("/api/v1/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Supplier X",
                                    "contact": "invalid-contact-format"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Contact must be a valid email or Vietnamese phone number"));
    }

    @Test
    @DisplayName("POST /api/v1/suppliers should return 400 when contact is an invalid short phone number")
    void createSupplier_shouldReturn400_whenPhoneIsTooShort() throws Exception {
        mockMvc.perform(post("/api/v1/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Supplier X",
                                    "contact": "0912345"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Contact must be a valid email or Vietnamese phone number"));
    }

    @Test
    @DisplayName("POST /api/v1/suppliers should return 409 when supplier name already exists")
    void createSupplier_shouldReturn409_whenDuplicateName() throws Exception {
        when(supplierService.createSupplier(any(CreateSupplierRequest.class)))
                .thenThrow(new SupplierAlreadyExistsException("Supplier with name 'Đã tồn tại' already exists"));

        mockMvc.perform(post("/api/v1/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Đã tồn tại",
                                    "contact": "0912345678"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Supplier with name 'Đã tồn tại' already exists"));
    }

    @Test
    @DisplayName("GET /api/v1/suppliers should return 200 with list of suppliers")
    void getAllSuppliers_shouldReturn200() throws Exception {
        List<SupplierResponse> list = List.of(
                new SupplierResponse(UUID.randomUUID(), "Supplier 1", "0912345678"),
                new SupplierResponse(UUID.randomUUID(), "Supplier 2", "s2@mail.com")
        );

        when(supplierService.getAllSuppliers())
                .thenReturn(ApiResponse.success(list, "Suppliers retrieved successfully"));

        mockMvc.perform(get("/api/v1/suppliers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Suppliers retrieved successfully"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("Supplier 1"))
                .andExpect(jsonPath("$.data[1].name").value("Supplier 2"));
    }

    @Test
    @DisplayName("GET /api/v1/suppliers/{id} should return 200 when supplier found")
    void getSupplierById_shouldReturn200_whenFound() throws Exception {
        UUID id = UUID.randomUUID();
        SupplierResponse response = new SupplierResponse(id, "Công ty Vải Hà Đông", "0987654321");

        when(supplierService.getSupplierById(id))
                .thenReturn(ApiResponse.success(response, "Supplier retrieved successfully"));

        mockMvc.perform(get("/api/v1/suppliers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id.toString()))
                .andExpect(jsonPath("$.data.name").value("Công ty Vải Hà Đông"))
                .andExpect(jsonPath("$.data.contact").value("0987654321"));
    }

    @Test
    @DisplayName("GET /api/v1/suppliers/{id} should return 404 when supplier not found")
    void getSupplierById_shouldReturn404_whenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(supplierService.getSupplierById(id))
                .thenThrow(new SupplierNotExistsException("Supplier does not exist"));

        mockMvc.perform(get("/api/v1/suppliers/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Supplier does not exist"));
    }

    @Test
    @DisplayName("PUT /api/v1/suppliers/{id} should return 200 when updated successfully")
    void updateSupplier_shouldReturn200_whenValid() throws Exception {
        UUID id = UUID.randomUUID();
        SupplierResponse response = new SupplierResponse(id, "Tên Mới", "new@supplier.com");

        when(supplierService.updateSupplier(eq(id), any(UpdateSupplierRequest.class)))
                .thenReturn(ApiResponse.success(response, "Supplier updated successfully"));

        mockMvc.perform(put("/api/v1/suppliers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Tên Mới",
                                    "contact": "new@supplier.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Supplier updated successfully"))
                .andExpect(jsonPath("$.data.name").value("Tên Mới"))
                .andExpect(jsonPath("$.data.contact").value("new@supplier.com"));
    }

    @Test
    @DisplayName("PUT /api/v1/suppliers/{id} should return 400 when contact is invalid")
    void updateSupplier_shouldReturn400_whenInvalidContact() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/suppliers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Tên Mới",
                                    "contact": "not_an_email_or_phone"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Contact must be a valid email or Vietnamese phone number"));
    }

    @Test
    @DisplayName("PUT /api/v1/suppliers/{id} should return 404 when supplier not found")
    void updateSupplier_shouldReturn404_whenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(supplierService.updateSupplier(eq(id), any(UpdateSupplierRequest.class)))
                .thenThrow(new SupplierNotExistsException("Supplier does not exist"));

        mockMvc.perform(put("/api/v1/suppliers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Tên Mới",
                                    "contact": "0912345678"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Supplier does not exist"));
    }

    @Test
    @DisplayName("PUT /api/v1/suppliers/{id} should return 409 when updated name already exists")
    void updateSupplier_shouldReturn409_whenDuplicateName() throws Exception {
        UUID id = UUID.randomUUID();
        when(supplierService.updateSupplier(eq(id), any(UpdateSupplierRequest.class)))
                .thenThrow(new SupplierAlreadyExistsException("Supplier with name 'Tên Trùng' already exists"));

        mockMvc.perform(put("/api/v1/suppliers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Tên Trùng",
                                    "contact": "0912345678"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Supplier with name 'Tên Trùng' already exists"));
    }

    @Test
    @DisplayName("DELETE /api/v1/suppliers/{id} should return 200 when deleted successfully")
    void deleteSupplier_shouldReturn200_whenSuccess() throws Exception {
        UUID id = UUID.randomUUID();
        when(supplierService.deleteSupplier(id))
                .thenReturn(ApiResponse.success("Supplier deleted successfully"));

        mockMvc.perform(delete("/api/v1/suppliers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Supplier deleted successfully"));
    }

    @Test
    @DisplayName("DELETE /api/v1/suppliers/{id} should return 404 when supplier not found")
    void deleteSupplier_shouldReturn404_whenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(supplierService.deleteSupplier(id))
                .thenThrow(new SupplierNotExistsException("Supplier does not exist"));

        mockMvc.perform(delete("/api/v1/suppliers/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Supplier does not exist"));
    }

    @Test
    @DisplayName("DELETE /api/v1/suppliers/{id} should return 409 when supplier is in use by stock movement")
    void deleteSupplier_shouldReturn409_whenInUse() throws Exception {
        UUID id = UUID.randomUUID();
        when(supplierService.deleteSupplier(id))
                .thenThrow(new SupplierInUseException("Cannot delete supplier because it is referenced in stock movements"));

        mockMvc.perform(delete("/api/v1/suppliers/{id}", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Cannot delete supplier because it is referenced in stock movements"));
    }
}
