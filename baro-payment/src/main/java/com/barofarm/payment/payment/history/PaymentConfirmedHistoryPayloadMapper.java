package com.barofarm.payment.payment.history;

import com.barofarm.log.history.mapper.HistoryPayloadMapper;
import com.barofarm.log.history.model.HistoryEventType;
import com.barofarm.log.history.model.PaymentEventData;
import com.barofarm.payment.payment.domain.Payment;
import com.barofarm.payment.payment.domain.PaymentRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PaymentConfirmedHistoryPayloadMapper implements HistoryPayloadMapper {

    private final PaymentRepository paymentRepository;

    public PaymentConfirmedHistoryPayloadMapper(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public HistoryEventType supports() {
        return HistoryEventType.PAYMENT_CONFIRMED;
    }

    @Override
    public Object payload(Object[] args, Object returnValue) {
        UUID userId = args != null && args.length >= 2 && args[0] instanceof UUID u ? u : null;
        // confirmPayment 는 Payment 를 즉시 저장하므로, 응답 DTO 대신 가장 최근 ORDER_PAYMENT 결제를 찾는다.
        Payment payment = paymentRepository.findTopByUserIdAndPurposeOrderByCreatedAtDesc(
            userId, com.barofarm.payment.payment.domain.Purpose.ORDER_PAYMENT
        ).orElse(null);

        if (payment == null) {
            return null;
        }

        return PaymentEventData.builder()
            .paymentId(payment.getId())
            .orderId(payment.getOrderId())
            .userId(payment.getUserId())
            .amount(payment.getAmount())
            .purpose(payment.getPurpose().name())
            .build();
    }
}

