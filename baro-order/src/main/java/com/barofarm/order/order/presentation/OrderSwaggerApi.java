package com.barofarm.order.order.presentation;

import com.barofarm.order.common.response.CustomPage;
import com.barofarm.order.common.response.ResponseDto;
import com.barofarm.order.order.application.dto.response.OrderCancelInfo;
import com.barofarm.order.order.application.dto.response.OrderCreateInfo;
import com.barofarm.order.order.application.dto.response.OrderDetailInfo;
import com.barofarm.order.order.presentation.dto.OrderCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Order", description = "주문 관련 API")
@RequestMapping("${api.v1}/orders")
public interface OrderSwaggerApi {

    @Operation(summary = "주문 생성", description = "사용자의 주문을 생성한다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "주문 생성 성공",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "요청 값 검증 실패",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "상품을 찾을 수 없습니다. (PRODUCT_NOT_FOUND)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "409",
            description = "재고가 부족합니다. (OUT_OF_STOCK)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "502",
            description = "재고 서비스 호출에 실패했습니다. (INVENTORY_SERVICE_ERROR)",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping
    ResponseDto<OrderCreateInfo> createOrder(@Valid @RequestBody OrderCreateRequest request);

    @Operation(summary = "주문 상세 조회", description = "특정 주문 ID에 대한 상세 정보를 조회한다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "주문 상세 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderDetailInfo.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "주문을 찾을 수 없습니다. (ORDER_NOT_FOUND)",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/{orderId}")
    ResponseDto<OrderDetailInfo> findOrderDetail(@PathVariable UUID orderId);

    @Operation(summary = "주문 목록 조회", description = "사용자의 주문 내역을 페이지 단위로 조회한다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "주문 목록 조회 성공",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping
    ResponseDto<CustomPage<OrderDetailInfo>> findOrderList(Pageable pageable);

    @Operation(summary = "주문 취소", description = "사용자의 주문을 취소한다. 재고 복구 및 주문 상태 변경을 수행한다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "주문 취소 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderCancelInfo.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "주문을 찾을 수 없습니다. (ORDER_NOT_FOUND)"
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "재고 서비스 호출에 실패했습니다. (INVENTORY_SERVICE_ERROR)",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PutMapping("/{orderId}/cancel")
    ResponseDto<OrderCancelInfo> cancelOrder(@PathVariable UUID orderId);
}
