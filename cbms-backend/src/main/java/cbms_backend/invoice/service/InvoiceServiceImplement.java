package cbms_backend.invoice.service;

import cbms_backend.core.config.TaxConfig;
import cbms_backend.core.exception.InvoiceAlreadyExistsException;
import cbms_backend.core.exception.InvoiceNotExistsException;
import cbms_backend.core.exception.OrderNotExistsException;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.invoice.dto.CreateCustomPriceInvoiceRequest;
import cbms_backend.invoice.dto.CreateInvoiceRequest;
import cbms_backend.invoice.dto.InvoiceResponse;
import cbms_backend.invoice.dto.UpdateInvoiceRequest;
import cbms_backend.invoice.entity.Invoice;
import cbms_backend.invoice.repository.InvoiceRepository;
import cbms_backend.material.entity.StockMovement;
import cbms_backend.material.entity.StockMovementType;
import cbms_backend.material.repository.StockMovementRepository;
import cbms_backend.order.entity.Order;
import cbms_backend.order.entity.OrderStatus;
import cbms_backend.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImplement implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final StockMovementRepository stockMovementRepository;
    private final TaxConfig taxConfig;

    @Override
    @Transactional
    public ApiResponse<InvoiceResponse> createInvoice(CreateInvoiceRequest request) {
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new OrderNotExistsException("Order does not exist"));

        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new IllegalArgumentException("Cannot issue invoice for a canceled order");
        }

        if (invoiceRepository.existsByOrderId(request.orderId())) {
            throw new InvoiceAlreadyExistsException("Invoice already exists for order " + request.orderId());
        }

        double subtotal = round(calculateOrderSubtotal(request.orderId()));
        double taxRate = taxConfig.getTaxRate();
        double grossTotal = round(subtotal * (1.0 + taxRate));

        double discount = round(request.discount() != null ? request.discount() : 0.0);
        if (discount < 0.0 || discount > 100.0) {
            throw new IllegalArgumentException("Discount must be between 0 and 100 percent");
        }

        double total = round(grossTotal * (1.0 - (discount / 100.0)));

        Invoice invoice = new Invoice();
        invoice.setOrder(order);
        invoice.setCustomer(order.getCustomer());
        invoice.setSubtotal(subtotal);
        invoice.setTaxRate(taxRate);
        invoice.setDiscount(discount);
        invoice.setTotal(total);
        invoice.setIssuedAt(Instant.now());

        Invoice saved = invoiceRepository.save(invoice);
        return ApiResponse.success(InvoiceResponse.fromEntity(saved), "Invoice created successfully");
    }

    @Override
    @Transactional
    public ApiResponse<InvoiceResponse> createCustomPriceInvoice(CreateCustomPriceInvoiceRequest request) {
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new OrderNotExistsException("Order does not exist"));

        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new IllegalArgumentException("Cannot issue invoice for a canceled order");
        }

        if (invoiceRepository.existsByOrderId(request.orderId())) {
            throw new InvoiceAlreadyExistsException("Invoice already exists for order " + request.orderId());
        }

        double subtotal = round(calculateOrderSubtotal(request.orderId()));
        double taxRate = taxConfig.getTaxRate();
        double grossTotal = round(subtotal * (1.0 + taxRate));

        double customPrice = round(request.customPrice());
        if (customPrice < 0.0) {
            throw new IllegalArgumentException("Custom price cannot be negative");
        }
        if (customPrice > grossTotal) {
            throw new IllegalArgumentException("Custom price (" + customPrice + ") cannot exceed standard total (" + grossTotal + ")");
        }

        double discount = grossTotal == 0.0 ? 0.0 : round(((grossTotal - customPrice) / grossTotal) * 100.0);
        double total = customPrice;

        Invoice invoice = new Invoice();
        invoice.setOrder(order);
        invoice.setCustomer(order.getCustomer());
        invoice.setSubtotal(subtotal);
        invoice.setTaxRate(taxRate);
        invoice.setDiscount(discount);
        invoice.setTotal(total);
        invoice.setIssuedAt(Instant.now());

        Invoice saved = invoiceRepository.save(invoice);
        return ApiResponse.success(InvoiceResponse.fromEntity(saved), "Invoice with custom price created successfully");
    }

    @Override
    @Transactional
    public ApiResponse<InvoiceResponse> updateInvoice(UUID id, UpdateInvoiceRequest request) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotExistsException("Invoice does not exist"));

        return updateInvoiceEntity(invoice, request);
    }

    @Override
    @Transactional
    public ApiResponse<InvoiceResponse> updateInvoiceByOrderId(UUID orderId, UpdateInvoiceRequest request) {
        Invoice invoice = invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new InvoiceNotExistsException("Invoice does not exist for order " + orderId));

        return updateInvoiceEntity(invoice, request);
    }

    private ApiResponse<InvoiceResponse> updateInvoiceEntity(Invoice invoice, UpdateInvoiceRequest request) {
        Order order = invoice.getOrder();
        if (order != null && order.getStatus() == OrderStatus.CANCELED) {
            throw new IllegalArgumentException("Cannot update invoice for a canceled order");
        }

        if (request.discount() != null && request.customPrice() != null) {
            throw new IllegalArgumentException("Cannot specify both discount and custom price");
        }

        UUID orderId = order != null ? order.getId() : null;
        if (orderId == null) {
            throw new IllegalStateException("Invoice is not associated with an order");
        }

        double subtotal = round(calculateOrderSubtotal(orderId));
        double taxRate = taxConfig.getTaxRate();
        double grossTotal = round(subtotal * (1.0 + taxRate));

        double discount;
        double total;

        if (request.customPrice() != null) {
            double customPrice = round(request.customPrice());
            if (customPrice < 0.0) {
                throw new IllegalArgumentException("Custom price cannot be negative");
            }
            if (customPrice > grossTotal) {
                throw new IllegalArgumentException("Custom price (" + customPrice + ") cannot exceed standard total (" + grossTotal + ")");
            }
            discount = grossTotal == 0.0 ? 0.0 : round(((grossTotal - customPrice) / grossTotal) * 100.0);
            total = customPrice;
        } else {
            discount = round(request.discount() != null ? request.discount() : 0.0);
            if (discount < 0.0 || discount > 100.0) {
                throw new IllegalArgumentException("Discount must be between 0 and 100 percent");
            }
            total = round(grossTotal * (1.0 - (discount / 100.0)));
        }

        if (order.getCustomer() != null) {
            invoice.setCustomer(order.getCustomer());
        }
        invoice.setSubtotal(subtotal);
        invoice.setTaxRate(taxRate);
        invoice.setDiscount(discount);
        invoice.setTotal(total);
        invoice.setIssuedAt(Instant.now());

        Invoice saved = invoiceRepository.save(invoice);
        return ApiResponse.success(InvoiceResponse.fromEntity(saved), "Invoice updated successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<InvoiceResponse> getInvoiceById(UUID id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotExistsException("Invoice does not exist"));

        return ApiResponse.success(InvoiceResponse.fromEntity(invoice), "Invoice retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<InvoiceResponse> getInvoiceByOrderId(UUID orderId) {
        Invoice invoice = invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new InvoiceNotExistsException("Invoice does not exist for order " + orderId));

        return ApiResponse.success(InvoiceResponse.fromEntity(invoice), "Invoice retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<InvoiceResponse>> getInvoicesByCustomerId(UUID customerId) {
        List<InvoiceResponse> list = invoiceRepository.findByCustomerId(customerId).stream()
                .map(InvoiceResponse::fromEntity)
                .toList();

        return ApiResponse.success(list, "Customer invoices retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<InvoiceResponse>> getAllInvoices() {
        List<InvoiceResponse> list = invoiceRepository.findAllByOrderByIssuedAtDesc().stream()
                .map(InvoiceResponse::fromEntity)
                .toList();

        return ApiResponse.success(list, "Invoices retrieved successfully");
    }

    private double calculateOrderSubtotal(UUID orderId) {
        List<StockMovement> outMovements = stockMovementRepository.findByOrderIdAndTypeWithMaterial(
                orderId, StockMovementType.OUT
        );

        return outMovements.stream()
                .mapToDouble(sm -> {
                    double quantity = sm.getQuantity() != null ? sm.getQuantity() : 0.0;
                    double price = (sm.getMaterial() != null && sm.getMaterial().getPrice() != null)
                            ? sm.getMaterial().getPrice() : 0.0;
                    return quantity * price;
                })
                .sum();
    }

    private double round(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
