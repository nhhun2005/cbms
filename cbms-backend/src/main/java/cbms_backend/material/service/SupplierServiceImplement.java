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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupplierServiceImplement implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final StockMovementRepository stockMovementRepository;

    @Override
    @Transactional
    public ApiResponse<SupplierResponse> createSupplier(CreateSupplierRequest request) {
        String name = request.name().trim();
        if (supplierRepository.existsByName(name)) {
            throw new SupplierAlreadyExistsException("Supplier with name '" + name + "' already exists");
        }

        Supplier supplier = new Supplier();
        supplier.setName(name);
        supplier.setContact(request.contact().trim());

        Supplier saved = supplierRepository.save(supplier);
        return ApiResponse.success(SupplierResponse.fromEntity(saved), "Supplier created successfully");
    }

    @Override
    @Transactional
    public ApiResponse<SupplierResponse> updateSupplier(UUID id, UpdateSupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotExistsException("Supplier does not exist"));

        String name = request.name().trim();
        if (supplierRepository.existsByNameAndIdNot(name, id)) {
            throw new SupplierAlreadyExistsException("Supplier with name '" + name + "' already exists");
        }

        supplier.setName(name);
        supplier.setContact(request.contact().trim());

        Supplier updated = supplierRepository.save(supplier);
        return ApiResponse.success(SupplierResponse.fromEntity(updated), "Supplier updated successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<SupplierResponse> getSupplierById(UUID id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotExistsException("Supplier does not exist"));

        return ApiResponse.success(SupplierResponse.fromEntity(supplier), "Supplier retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<SupplierResponse>> getAllSuppliers() {
        List<SupplierResponse> suppliers = supplierRepository.findAll().stream()
                .map(SupplierResponse::fromEntity)
                .toList();

        return ApiResponse.success(suppliers, "Suppliers retrieved successfully");
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteSupplier(UUID id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotExistsException("Supplier does not exist"));

        if (stockMovementRepository.existsBySupplierId(id)) {
            throw new SupplierInUseException("Cannot delete supplier because it is referenced in stock movements");
        }

        supplierRepository.delete(supplier);
        return ApiResponse.success("Supplier deleted successfully");
    }
}
