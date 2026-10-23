package cbms_backend.order.service;

import cbms_backend.core.exception.CannotDeleteLastCurtainException;
import cbms_backend.core.exception.CannotModifyCurtainException;
import cbms_backend.core.exception.CurtainNotExistsException;
import cbms_backend.core.exception.OrderNotExistsException;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.order.dto.curtain.CreateCurtainRequest;
import cbms_backend.order.dto.curtain.UpdateCurtainRequest;
import cbms_backend.order.dto.get_order.CurtainResponse;
import cbms_backend.order.entity.Curtain;
import cbms_backend.order.entity.Order;
import cbms_backend.order.entity.OrderStatus;
import cbms_backend.order.repository.CurtainRepository;
import cbms_backend.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CurtainServiceImplement implements CurtainService {

    private final CurtainRepository curtainRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public ApiResponse<CurtainResponse> addCurtain(CreateCurtainRequest request) {
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new OrderNotExistsException("Order does not exist"));

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new CannotModifyCurtainException("Cannot add curtain to a completed order");
        }

        Curtain curtain = new Curtain();
        curtain.setOrder(order);
        curtain.setWidth(request.width());
        curtain.setHeight(request.height());

        Curtain saved = curtainRepository.save(curtain);
        return ApiResponse.success(CurtainResponse.fromEntity(saved), "Curtain added successfully");
    }

    @Override
    @Transactional
    public ApiResponse<CurtainResponse> updateCurtain(UUID id, UpdateCurtainRequest request) {
        Curtain curtain = curtainRepository.findById(id)
                .orElseThrow(() -> new CurtainNotExistsException("Curtain does not exist"));

        if (curtain.getOrder().getStatus() == OrderStatus.COMPLETED) {
            throw new CannotModifyCurtainException("Cannot update curtain in a completed order");
        }

        curtain.setWidth(request.width());
        curtain.setHeight(request.height());

        Curtain saved = curtainRepository.save(curtain);
        return ApiResponse.success(CurtainResponse.fromEntity(saved), "Curtain updated successfully");
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteCurtain(UUID id) {
        Curtain curtain = curtainRepository.findById(id)
                .orElseThrow(() -> new CurtainNotExistsException("Curtain does not exist"));

        Order order = curtain.getOrder();
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new CannotModifyCurtainException("Cannot delete curtain from a completed order");
        }

        long count = curtainRepository.countByOrderId(order.getId());
        if (count <= 1) {
            throw new CannotDeleteLastCurtainException("Cannot delete the last curtain of an order. An order must have at least one curtain");
        }

        curtainRepository.delete(curtain);
        return ApiResponse.success("Curtain deleted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<CurtainResponse> getCurtainById(UUID id) {
        Curtain curtain = curtainRepository.findById(id)
                .orElseThrow(() -> new CurtainNotExistsException("Curtain does not exist"));

        return ApiResponse.success(CurtainResponse.fromEntity(curtain), "Curtain retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<CurtainResponse>> getCurtainsByOrderId(UUID orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new OrderNotExistsException("Order does not exist");
        }

        List<CurtainResponse> list = curtainRepository.findByOrderId(orderId).stream()
                .map(CurtainResponse::fromEntity)
                .toList();

        return ApiResponse.success(list, "Curtains retrieved successfully");
    }
}
