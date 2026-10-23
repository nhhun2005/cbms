package cbms_backend.material.dto.get_supplier;

import cbms_backend.material.entity.Supplier;

import java.util.UUID;

public record SupplierResponse(
        UUID id,
        String name,
        String contact
) {
    public static SupplierResponse fromEntity(Supplier supplier) {
        if (supplier == null) {
            return null;
        }
        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getContact()
        );
    }
}
