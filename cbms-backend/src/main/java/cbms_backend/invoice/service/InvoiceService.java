package cbms_backend.invoice.service;

import cbms_backend.core.util.ApiResponse;
import cbms_backend.invoice.dto.CreateCustomPriceInvoiceRequest;
import cbms_backend.invoice.dto.CreateInvoiceRequest;
import cbms_backend.invoice.dto.InvoiceResponse;
import cbms_backend.invoice.dto.UpdateInvoiceRequest;

import java.util.List;
import java.util.UUID;

public interface InvoiceService {
    ApiResponse<InvoiceResponse> createInvoice(CreateInvoiceRequest request);
    ApiResponse<InvoiceResponse> createCustomPriceInvoice(CreateCustomPriceInvoiceRequest request);
    ApiResponse<InvoiceResponse> updateInvoice(UUID id, UpdateInvoiceRequest request);
    ApiResponse<InvoiceResponse> updateInvoiceByOrderId(UUID orderId, UpdateInvoiceRequest request);
    ApiResponse<InvoiceResponse> getInvoiceById(UUID id);
    ApiResponse<InvoiceResponse> getInvoiceByOrderId(UUID orderId);
    ApiResponse<List<InvoiceResponse>> getInvoicesByCustomerId(UUID customerId);
    ApiResponse<List<InvoiceResponse>> getAllInvoices();
}
