package com.barofarm.order.payment.application;

import com.barofarm.order.common.response.ResponseDto;
import com.barofarm.order.order.application.OrderService;
import com.barofarm.order.order.domain.Order;
import com.barofarm.order.payment.application.dto.request.TossPaymentConfirmCommand;
import com.barofarm.order.payment.application.dto.response.TossPaymentConfirmInfo;
import com.barofarm.order.payment.client.TossPaymentClient;
import com.barofarm.order.payment.client.dto.TossPaymentResponse;
import com.barofarm.order.payment.domain.Payment;
import com.barofarm.order.payment.domain.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final TossPaymentClient tossPaymentClient;
    private final OrderService orderService;


    public ResponseDto<TossPaymentConfirmInfo> confirmPayment(TossPaymentConfirmCommand command) {
        TossPaymentResponse tossPayment = tossPaymentClient.confirm(command);
        UUID orderId = UUID.fromString(tossPayment.orderId());
        Payment payment = Payment.of(
            tossPayment.paymentKey(),
            tossPayment.orderId(),
            tossPayment.totalAmount()
        );
        LocalDateTime approvedAt = tossPayment.approvedAt() != null ? tossPayment.approvedAt().toLocalDateTime() : null;
        LocalDateTime requestedAt = tossPayment.requestedAt() != null ? tossPayment.requestedAt().toLocalDateTime() : null;
        payment.markConfirmed(tossPayment.method(), approvedAt, requestedAt);

        Payment saved = paymentRepository.save(payment);

        Order order = orderService.findEntity(orderId);
        orderService.markPaid(order);

        // TODO: 정산 서비스 호출 & 알람 서비스 호출
        return ResponseDto.ok(TossPaymentConfirmInfo.from(payment));
    }
}
