package com.barofarm.order.payment.presentation;

import com.barofarm.order.common.response.ResponseDto;
import com.barofarm.order.payment.application.PaymentService;
import com.barofarm.order.payment.application.dto.response.TossPaymentConfirmInfo;
import com.barofarm.order.payment.application.dto.response.TossPaymentRefundInfo;
import com.barofarm.order.payment.presentation.dto.TossPaymentConfirmRequest;
import com.barofarm.order.payment.presentation.dto.TossPaymentRefundRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.v1}/payments")
@RequiredArgsConstructor
// TODO: 나중에 인증/인가 붙이면 @RequestHeader("userId") UUID sellerId 사용
public class PaymentController implements PaymentSwaggerApi{

    private final PaymentService paymentService;

    @PostMapping("/toss/confirm")
    public ResponseDto<TossPaymentConfirmInfo> confirmPayment(@RequestBody TossPaymentConfirmRequest confirmRequest){
        return paymentService.confirmPayment(confirmRequest.toCommand());
    }

    @PostMapping("/toss/refund")
    public ResponseDto<TossPaymentRefundInfo> refundPayment(@RequestBody TossPaymentRefundRequest refundRequest){
        return paymentService.refundPayment(refundRequest.toCommand());
    }
}
