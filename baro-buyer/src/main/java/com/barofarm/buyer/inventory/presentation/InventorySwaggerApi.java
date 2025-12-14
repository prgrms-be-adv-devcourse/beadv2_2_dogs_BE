package com.barofarm.buyer.inventory.presentation;

import com.barofarm.buyer.common.response.ResponseDto;
import com.barofarm.buyer.inventory.presentation.dto.InventoryDecreaseRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Inventory", description = "재고 관련 API")
@RequestMapping("${api.v1}/inventories")
public interface InventorySwaggerApi {

    @Operation(summary = "재고 차감", description = "상품 재고를 차감한다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "재고 차감 성공",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "요청 값 검증 실패 (INVALID_REQUEST)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "재고 정보를 찾을 수 없음 (INVENTORY_NOT_FOUND)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "409",
            description = "재고 부족 (INSUFFICIENT_STOCK)",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/decrease")
    ResponseDto<Void> decreaseStock(@Valid @RequestBody InventoryDecreaseRequest request);
}
