package cbms_backend.order.dto.get_order;

import cbms_backend.order.entity.Curtain;

import java.util.UUID;

public record CurtainResponse(
        UUID id,
        UUID orderId,
        Double width,
        Double height
) {
    public static CurtainResponse fromEntity(Curtain curtain) {
        if (curtain == null) {
            return null;
        }
        return new CurtainResponse(
                curtain.getId(),
                curtain.getOrder() != null ? curtain.getOrder().getId() : null,
                curtain.getWidth(),
                curtain.getHeight()
        );
    }
}
