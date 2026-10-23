package cbms_backend.order.controller;

import cbms_backend.core.util.ApiResponse;
import cbms_backend.order.dto.curtain.CreateCurtainRequest;
import cbms_backend.order.dto.curtain.UpdateCurtainRequest;
import cbms_backend.order.dto.get_order.CurtainResponse;
import cbms_backend.order.service.CurtainService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/curtains")
@RequiredArgsConstructor
public class CurtainController {

    private final CurtainService curtainService;

    @PostMapping
    public ResponseEntity<ApiResponse<CurtainResponse>> addCurtain(
            @Valid @RequestBody CreateCurtainRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(curtainService.addCurtain(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CurtainResponse>> updateCurtain(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCurtainRequest request
    ) {
        return ResponseEntity.ok(curtainService.updateCurtain(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCurtain(@PathVariable UUID id) {
        return ResponseEntity.ok(curtainService.deleteCurtain(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CurtainResponse>> getCurtainById(@PathVariable UUID id) {
        return ResponseEntity.ok(curtainService.getCurtainById(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<List<CurtainResponse>>> getCurtainsByOrderId(@PathVariable UUID orderId) {
        return ResponseEntity.ok(curtainService.getCurtainsByOrderId(orderId));
    }
}
