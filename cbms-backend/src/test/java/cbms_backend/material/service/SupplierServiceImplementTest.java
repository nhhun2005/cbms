package cbms_backend.material.service;

import cbms_backend.core.exception.SupplierAlreadyExistsException;
import cbms_backend.core.exception.SupplierInUseException;
import cbms_backend.core.exception.SupplierNotExistsException;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.material.dto.create_supplier.CreateSupplierRequest;
import cbms_backend.material.dto.get_supplier.SupplierResponse;
import cbms_backend.material.dto.update_supplier.UpdateSupplierRequest;
import cbms_backend.material.entity.Supplier;
import cbms_backend.material.repository.StockMovementRepository;
import cbms_backend.material.repository.SupplierRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceImplementTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private SupplierServiceImplement supplierService;

    @Test
    @DisplayName("createSupplier should succeed when contact is a valid Vietnamese phone number")
    void createSupplier_shouldSucceed_withPhoneContact() {
        CreateSupplierRequest request = new CreateSupplierRequest("Công ty Rèm An Phát", "0987654321");

        when(supplierRepository.existsByName("Công ty Rèm An Phát")).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier s = invocation.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        ApiResponse<SupplierResponse> response = supplierService.createSupplier(request);

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals("Công ty Rèm An Phát", response.getData().name());
        assertEquals("0987654321", response.getData().contact());
        assertEquals("Supplier created successfully", response.getMessage());

        verify(supplierRepository).existsByName("Công ty Rèm An Phát");
        verify(supplierRepository).save(any(Supplier.class));
    }

    @Test
    @DisplayName("createSupplier should succeed when contact is a valid email")
    void createSupplier_shouldSucceed_withEmailContact() {
        CreateSupplierRequest request = new CreateSupplierRequest("Công ty Rèm Toàn Cầu", "contact@toancau.com.vn");

        when(supplierRepository.existsByName("Công ty Rèm Toàn Cầu")).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier s = invocation.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        ApiResponse<SupplierResponse> response = supplierService.createSupplier(request);

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals("Công ty Rèm Toàn Cầu", response.getData().name());
        assertEquals("contact@toancau.com.vn", response.getData().contact());
        assertEquals("Supplier created successfully", response.getMessage());
    }

    @Test
    @DisplayName("createSupplier should throw SupplierAlreadyExistsException when supplier name already exists")
    void createSupplier_shouldThrow_whenNameAlreadyExists() {
        CreateSupplierRequest request = new CreateSupplierRequest("Công ty Đã Tồn Tại", "0912345678");

        when(supplierRepository.existsByName("Công ty Đã Tồn Tại")).thenReturn(true);

        SupplierAlreadyExistsException exception = assertThrows(
                SupplierAlreadyExistsException.class,
                () -> supplierService.createSupplier(request)
        );

        assertTrue(exception.getMessage().contains("Công ty Đã Tồn Tại"));
        verify(supplierRepository, never()).save(any(Supplier.class));
    }

    @Test
    @DisplayName("updateSupplier should succeed when name and contact are valid")
    void updateSupplier_shouldSucceed() {
        UUID id = UUID.randomUUID();
        Supplier existing = new Supplier(id, "Tên Cũ", "old@supplier.com");
        UpdateSupplierRequest request = new UpdateSupplierRequest("Tên Mới", "0988776655");

        when(supplierRepository.findById(id)).thenReturn(Optional.of(existing));
        when(supplierRepository.existsByNameAndIdNot("Tên Mới", id)).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApiResponse<SupplierResponse> response = supplierService.updateSupplier(id, request);

        assertNotNull(response);
        assertEquals("Tên Mới", response.getData().name());
        assertEquals("0988776655", response.getData().contact());
        assertEquals("Supplier updated successfully", response.getMessage());
    }

    @Test
    @DisplayName("updateSupplier should throw SupplierNotExistsException when id not found")
    void updateSupplier_shouldThrow_whenNotFound() {
        UUID id = UUID.randomUUID();
        UpdateSupplierRequest request = new UpdateSupplierRequest("Tên Mới", "0988776655");

        when(supplierRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(SupplierNotExistsException.class, () -> supplierService.updateSupplier(id, request));
        verify(supplierRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateSupplier should throw SupplierAlreadyExistsException when updated name already exists for other supplier")
    void updateSupplier_shouldThrow_whenDuplicateName() {
        UUID id = UUID.randomUUID();
        Supplier existing = new Supplier(id, "Tên Cũ", "0988776655");
        UpdateSupplierRequest request = new UpdateSupplierRequest("Tên Trùng", "0988776655");

        when(supplierRepository.findById(id)).thenReturn(Optional.of(existing));
        when(supplierRepository.existsByNameAndIdNot("Tên Trùng", id)).thenReturn(true);

        assertThrows(SupplierAlreadyExistsException.class, () -> supplierService.updateSupplier(id, request));
        verify(supplierRepository, never()).save(any());
    }

    @Test
    @DisplayName("getSupplierById should return supplier when found")
    void getSupplierById_shouldReturnSupplier() {
        UUID id = UUID.randomUUID();
        Supplier supplier = new Supplier(id, "Nhà cung cấp lụa Hà Đông", "0912345678");

        when(supplierRepository.findById(id)).thenReturn(Optional.of(supplier));

        ApiResponse<SupplierResponse> response = supplierService.getSupplierById(id);

        assertNotNull(response);
        assertEquals(id, response.getData().id());
        assertEquals("Nhà cung cấp lụa Hà Đông", response.getData().name());
        assertEquals("0912345678", response.getData().contact());
        assertEquals("Supplier retrieved successfully", response.getMessage());
    }

    @Test
    @DisplayName("getSupplierById should throw SupplierNotExistsException when not found")
    void getSupplierById_shouldThrow_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(supplierRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(SupplierNotExistsException.class, () -> supplierService.getSupplierById(id));
    }

    @Test
    @DisplayName("getAllSuppliers should return list of suppliers")
    void getAllSuppliers_shouldReturnList() {
        Supplier s1 = new Supplier(UUID.randomUUID(), "Supplier 1", "0912345678");
        Supplier s2 = new Supplier(UUID.randomUUID(), "Supplier 2", "contact@supplier2.com");

        when(supplierRepository.findAll()).thenReturn(List.of(s1, s2));

        ApiResponse<List<SupplierResponse>> response = supplierService.getAllSuppliers();

        assertNotNull(response);
        assertEquals(2, response.getData().size());
        assertEquals("Supplier 1", response.getData().get(0).name());
        assertEquals("Supplier 2", response.getData().get(1).name());
        assertEquals("Suppliers retrieved successfully", response.getMessage());
    }

    @Test
    @DisplayName("getAllSuppliers should return empty list when no suppliers")
    void getAllSuppliers_shouldReturnEmptyList() {
        when(supplierRepository.findAll()).thenReturn(Collections.emptyList());

        ApiResponse<List<SupplierResponse>> response = supplierService.getAllSuppliers();

        assertNotNull(response);
        assertTrue(response.getData().isEmpty());
    }

    @Test
    @DisplayName("deleteSupplier should succeed when supplier exists and not in use")
    void deleteSupplier_shouldSucceed() {
        UUID id = UUID.randomUUID();
        Supplier supplier = new Supplier(id, "Supplier cần xóa", "0912345678");

        when(supplierRepository.findById(id)).thenReturn(Optional.of(supplier));
        when(stockMovementRepository.existsBySupplierId(id)).thenReturn(false);

        ApiResponse<Void> response = supplierService.deleteSupplier(id);

        assertNotNull(response);
        assertEquals("Supplier deleted successfully", response.getMessage());
        verify(supplierRepository).delete(supplier);
    }

    @Test
    @DisplayName("deleteSupplier should throw SupplierNotExistsException when not found")
    void deleteSupplier_shouldThrow_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(supplierRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(SupplierNotExistsException.class, () -> supplierService.deleteSupplier(id));
        verify(supplierRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteSupplier should throw SupplierInUseException when supplier is in use by stock movement")
    void deleteSupplier_shouldThrow_whenInUse() {
        UUID id = UUID.randomUUID();
        Supplier supplier = new Supplier(id, "Supplier đang dùng", "0912345678");

        when(supplierRepository.findById(id)).thenReturn(Optional.of(supplier));
        when(stockMovementRepository.existsBySupplierId(id)).thenReturn(true);

        SupplierInUseException exception = assertThrows(
                SupplierInUseException.class,
                () -> supplierService.deleteSupplier(id)
        );

        assertTrue(exception.getMessage().contains("Cannot delete supplier because it is referenced in stock movements"));
        verify(supplierRepository, never()).delete(any());
    }
}
