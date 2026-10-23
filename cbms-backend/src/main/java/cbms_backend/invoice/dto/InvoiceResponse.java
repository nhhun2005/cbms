package cbms_backend.invoice.dto;

import cbms_backend.invoice.entity.Invoice;

import java.time.Instant;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        UUID orderId,
        UUID customerId,
        String customerName,
        String customerPhone,
        Double subtotal,
        Double taxRate,
        Double discount,
        Double total,
        Instant issuedAt
) {
    public static InvoiceResponse fromEntity(Invoice invoice) {
        if (invoice == null) {
            return null;
        }
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getOrder() != null ? invoice.getOrder().getId() : null,
                invoice.getCustomer() != null ? invoice.getCustomer().getId() : null,
                invoice.getCustomer() != null ? invoice.getCustomer().getName() : null,
                invoice.getCustomer() != null ? invoice.getCustomer().getPhone() : null,
                invoice.getSubtotal(),
                invoice.getTaxRate(),
                invoice.getDiscount(),
                invoice.getTotal(),
                invoice.getIssuedAt()
        );
    }
}
