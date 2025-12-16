package com.barofarm.order.payment.application;

import static com.barofarm.order.payment.domain.Purpose.DEPOSIT_CHARGE;
import static com.barofarm.order.payment.domain.Purpose.ORDER_PAYMENT;
import static com.barofarm.order.payment.exception.PaymentErrorCode.PAYMENT_NOT_FOUND;

import com.barofarm.order.common.exception.CustomException;
import com.barofarm.order.common.response.ResponseDto;
import com.barofarm.order.deposit.application.DepositService;
import com.barofarm.order.order.application.OrderService;
import com.barofarm.order.order.application.dto.request.DeliveryInternalCreateRequest;
import com.barofarm.order.order.application.dto.response.OrderDeliveryInfo;
import com.barofarm.order.order.client.DeliveryClient;
import com.barofarm.order.payment.application.dto.request.TossPaymentConfirmCommand;
import com.barofarm.order.payment.application.dto.request.TossPaymentRefundCommand;
import com.barofarm.order.payment.application.dto.response.TossPaymentConfirmInfo;
import com.barofarm.order.payment.application.dto.response.TossPaymentRefundInfo;
import com.barofarm.order.payment.client.TossPaymentClient;
import com.barofarm.order.payment.client.dto.TossPaymentResponse;
import com.barofarm.order.payment.domain.Payment;
import com.barofarm.order.payment.domain.PaymentRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final TossPaymentClient tossPaymentClient;
    private final OrderService orderService;
    private final DepositService depositService;
    private final DeliveryClient deliveryClient;

    @Transactional
    public ResponseDto<TossPaymentConfirmInfo> confirmPayment(UUID userId, TossPaymentConfirmCommand command) {
        TossPaymentResponse tossPayment = tossPaymentClient.confirm(command);

        UUID orderId = UUID.fromString(tossPayment.orderId());
        orderService.markOrderPaid(userId, orderId);

        Payment payment = Payment.of(tossPayment, ORDER_PAYMENT);
        Payment saved = paymentRepository.save(payment);

        // delivery-service에서 특정 시간 이후 order의 상태를 변경하도록 하면 좋을듯.
        try {
            OrderDeliveryInfo deliveryInfo = orderService.getDeliveryInfo(orderId);
            DeliveryInternalCreateRequest request = DeliveryInternalCreateRequest.from(deliveryInfo);

            deliveryClient.createDelivery(request);

        } catch (Exception e) {
            // 배송 생성 실패: 주문은 PAID 유지(재시도/운영)
            log.error("배송 생성 실패. orderId={}", orderId, e);
        }
        return ResponseDto.ok(TossPaymentConfirmInfo.from(saved));
    }

    @Transactional
    public ResponseDto<TossPaymentRefundInfo> refundPayment(UUID userId, TossPaymentRefundCommand command) {
        TossPaymentResponse tossResponse = tossPaymentClient.refund(command);

        UUID orderId = UUID.fromString(tossResponse.orderId());
        orderService.cancelOrder(userId, orderId);

        Payment payment = paymentRepository.findByPaymentKey(command.paymentKey())
                .orElseThrow(() -> new CustomException(PAYMENT_NOT_FOUND));
        payment.refund();
        return ResponseDto.ok(TossPaymentRefundInfo.from(tossResponse));
    }

    @Transactional
    public ResponseDto<TossPaymentConfirmInfo> confirmDeposit(UUID userId, TossPaymentConfirmCommand command) {
        TossPaymentResponse tossPayment = tossPaymentClient.confirm(command);

        UUID chargeId = UUID.fromString(tossPayment.orderId());

        depositService.markDepositCharge(userId, chargeId);

        Payment payment = Payment.of(tossPayment, DEPOSIT_CHARGE);
        Payment saved = paymentRepository.save(payment);

        return ResponseDto.ok(TossPaymentConfirmInfo.from(saved));
    }
}
