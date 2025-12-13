package com.barofarm.order.payment.application;

import com.barofarm.order.common.exception.CustomException;
import com.barofarm.order.common.response.ResponseDto;
import com.barofarm.order.deposit.application.DepositService;
import com.barofarm.order.order.application.OrderService;
import com.barofarm.order.payment.application.dto.request.TossPaymentRefundCommand;
import com.barofarm.order.payment.application.dto.request.TossPaymentConfirmCommand;
import com.barofarm.order.payment.application.dto.response.TossPaymentRefundInfo;
import com.barofarm.order.payment.application.dto.response.TossPaymentConfirmInfo;
import com.barofarm.order.payment.client.TossPaymentClient;
import com.barofarm.order.payment.client.dto.TossPaymentResponse;
import com.barofarm.order.payment.domain.Payment;
import com.barofarm.order.payment.domain.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

import static com.barofarm.order.payment.domain.Purpose.DEPOSIT_CHARGE;
import static com.barofarm.order.payment.domain.Purpose.ORDER_PAYMENT;
import static com.barofarm.order.payment.exception.PaymentErrorCode.PAYMENT_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final TossPaymentClient tossPaymentClient;
    private final OrderService orderService;
    private final DepositService depositService;

    @Transactional
    public ResponseDto<TossPaymentConfirmInfo> confirmPayment(TossPaymentConfirmCommand command) {
        TossPaymentResponse tossPayment = tossPaymentClient.confirm(command);

        UUID orderId = UUID.fromString(tossPayment.orderId());
        orderService.markOrderPaid(orderId);

        Payment payment = Payment.of(tossPayment, ORDER_PAYMENT);
        Payment saved = paymentRepository.save(payment);

        // TODO: 정산 서비스 호출 & 알람 서비스 호출
        return ResponseDto.ok(TossPaymentConfirmInfo.from(saved));
    }

    @Transactional
    public ResponseDto<TossPaymentRefundInfo> refundPayment(TossPaymentRefundCommand command) {
        TossPaymentResponse tossResponse = tossPaymentClient.refund(command);

        UUID orderId = UUID.fromString(tossResponse.orderId());
        orderService.cancelOrder(orderId);

        Payment payment = paymentRepository.findByPaymentKey(command.paymentKey())
                .orElseThrow(() -> new CustomException(PAYMENT_NOT_FOUND));
        payment.refund();
        return ResponseDto.ok(TossPaymentRefundInfo.from(tossResponse));
    }

    @Transactional
    public ResponseDto<TossPaymentConfirmInfo> confirmDeposit(TossPaymentConfirmCommand command) {
        TossPaymentResponse tossPayment = tossPaymentClient.confirm(command);

        UUID chargeId = UUID.fromString(tossPayment.orderId());
        depositService.markDepositCharge(chargeId);

        Payment payment = Payment.of(tossPayment, DEPOSIT_CHARGE);
        Payment saved = paymentRepository.save(payment);

        // TODO: 정산 서비스 호출 & 알람 서비스 호출
        return ResponseDto.ok(TossPaymentConfirmInfo.from(saved));
    }
}
