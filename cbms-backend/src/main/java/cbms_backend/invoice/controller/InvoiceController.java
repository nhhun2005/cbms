package cbms_backend.invoice.controller;

import cbms_backend.core.util.ApiResponse;
import cbms_backend.invoice.dto.CreateCustomPriceInvoiceRequest;
import cbms_backend.invoice.dto.CreateInvoiceRequest;
import cbms_backend.invoice.dto.InvoiceResponse;
import cbms_backend.invoice.dto.UpdateInvoiceRequest;
import cbms_backend.invoice.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    public ResponseEntity<ApiResponse<InvoiceResponse>> createInvoice(
            @Valid @RequestBody CreateInvoiceRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(invoiceService.createInvoice(request));
    }

    @PostMapping("/custom-price")
    public ResponseEntity<ApiResponse<InvoiceResponse>> createCustomPriceInvoice(
            @Valid @RequestBody CreateCustomPriceInvoiceRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(invoiceService.createCustomPriceInvoice(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> updateInvoice(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateInvoiceRequest request
    ) {
        return ResponseEntity.ok(invoiceService.updateInvoice(id, request));
    }

    @PutMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> updateInvoiceByOrderId(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateInvoiceRequest request
    ) {
        return ResponseEntity.ok(invoiceService.updateInvoiceByOrderId(orderId, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceById(@PathVariable UUID id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceByOrderId(@PathVariable UUID orderId) {
        return ResponseEntity.ok(invoiceService.getInvoiceByOrderId(orderId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoicesByCustomerId(@PathVariable UUID customerId) {
        return ResponseEntity.ok(invoiceService.getInvoicesByCustomerId(customerId));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getAllInvoices() {
        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }
}
