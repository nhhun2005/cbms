package cbms_backend.order.service;

import cbms_backend.core.util.ApiResponse;
import cbms_backend.order.dto.curtain.CreateCurtainRequest;
import cbms_backend.order.dto.curtain.UpdateCurtainRequest;
import cbms_backend.order.dto.get_order.CurtainResponse;

import java.util.List;
import java.util.UUID;

public interface CurtainService {
    ApiResponse<CurtainResponse> addCurtain(CreateCurtainRequest request);
    ApiResponse<CurtainResponse> updateCurtain(UUID id, UpdateCurtainRequest request);
    ApiResponse<Void> deleteCurtain(UUID id);
    ApiResponse<CurtainResponse> getCurtainById(UUID id);
    ApiResponse<List<CurtainResponse>> getCurtainsByOrderId(UUID orderId);
}
