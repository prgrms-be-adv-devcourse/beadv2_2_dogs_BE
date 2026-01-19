package com.barofarm.order.order.presentation;

import com.barofarm.dto.CustomPage;
import com.barofarm.dto.ResponseDto;
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
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Orders", description = "주문 관련 API")
@RequestMapping("${api.v1}/orders")
public interface OrderSwaggerApi {

    @Operation(summary = "주문 생성", description = "주문을 생성합니다.")
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
            description = "재고 서비스 호출 실패. (INVENTORY_SERVICE_ERROR)",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping
    ResponseDto<OrderCreateInfo> placeOrder(
        @Valid @RequestBody OrderCreateRequest request
    );

    @Operation(summary = "주문 상세 조회", description = "주문 ID로 주문 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "주문 상세 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OrderDetailInfo.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "주문을 찾을 수 없습니다. (ORDER_NOT_FOUND)",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/{orderId}")
    ResponseDto<OrderDetailInfo> findOrderDetail(
        @PathVariable("orderId") UUID orderId
    );

    @Operation(summary = "주문 목록 조회", description = "주문 이력을 페이지네이션하여 조회합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "주문 목록 조회 성공",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping
    ResponseDto<CustomPage<OrderDetailInfo>> findOrderList(Pageable pageable);

    @Operation(summary = "주문 취소", description = "주문을 취소하고 재고 복구 및 상태 변경을 수행합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "주문 취소 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OrderCancelInfo.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "주문을 찾을 수 없습니다. (ORDER_NOT_FOUND)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "502",
            description = "재고 서비스 호출 실패. (INVENTORY_SERVICE_ERROR)",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/{orderId}/cancel")
    ResponseDto<OrderCancelInfo> cancelOrder(
        @PathVariable("orderId") UUID orderId
    );
}
