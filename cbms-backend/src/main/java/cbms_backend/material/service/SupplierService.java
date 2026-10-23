package cbms_backend.material.service;

import cbms_backend.core.util.ApiResponse;
import cbms_backend.material.dto.create_supplier.CreateSupplierRequest;
import cbms_backend.material.dto.get_supplier.SupplierResponse;
import cbms_backend.material.dto.update_supplier.UpdateSupplierRequest;

import java.util.List;
import java.util.UUID;

public interface SupplierService {
    ApiResponse<SupplierResponse> createSupplier(CreateSupplierRequest request);
    ApiResponse<SupplierResponse> updateSupplier(UUID id, UpdateSupplierRequest request);
    ApiResponse<SupplierResponse> getSupplierById(UUID id);
    ApiResponse<List<SupplierResponse>> getAllSuppliers();
    ApiResponse<Void> deleteSupplier(UUID id);
}
