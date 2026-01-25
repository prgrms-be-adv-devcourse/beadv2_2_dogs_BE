package com.barofarm.buyer.inventory.presentation;

import com.barofarm.buyer.inventory.presentation.dto.InventoryInfo;
import com.barofarm.dto.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Inventory", description = "재고 관련 API")
@RequestMapping("${api.v1}/inventories")
public interface InventorySwaggerApi {

    @Operation(
        summary = "상품 재고 조회",
        description = "상품 ID로 재고 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "재고 조회 성공",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "요청 검증 실패. (INVALID_REQUEST)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "해당 상품의 재고 정보가 존재하지 않습니다. (INVENTORY_NOT_FOUND)",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping(params = "productId")
    ResponseDto<List<InventoryInfo>> getInventoriesByProductId(
        @Parameter(
            description = "조회할 상품 ID",
            required = true,
            example = "550e8400-e29b-41d4-a716-446655440000"
        )
        @RequestParam @NotNull UUID productId
    );
}
