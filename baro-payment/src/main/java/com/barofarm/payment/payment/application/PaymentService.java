package com.barofarm.payment.payment.application;

import static com.barofarm.payment.payment.domain.Purpose.DEPOSIT_CHARGE;
import static com.barofarm.payment.payment.domain.Purpose.ORDER_PAYMENT;

import com.barofarm.dto.ResponseDto;
import com.barofarm.exception.CustomException;
import com.barofarm.payment.payment.application.dto.request.TossPaymentConfirmCommand;
import com.barofarm.payment.payment.application.dto.response.TossPaymentConfirmInfo;
import com.barofarm.payment.payment.domain.Payment;
import com.barofarm.payment.payment.domain.PaymentOutboxEvent;
import com.barofarm.payment.payment.domain.PaymentOutboxEventRepository;
import com.barofarm.payment.payment.domain.PaymentRepository;
import com.barofarm.payment.payment.exception.PaymentErrorCode;
import com.barofarm.payment.payment.infrastructure.kafka.producer.dto.DepositChargeConfirmedEvent;
import com.barofarm.payment.payment.infrastructure.kafka.producer.dto.PaymentConfirmedEvent;
import com.barofarm.payment.payment.infrastructure.rest.TossPaymentClient;
import com.barofarm.payment.payment.infrastructure.rest.dto.TossPaymentResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final PaymentOutboxEventRepository paymentOutboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ResponseDto<TossPaymentConfirmInfo> confirmPayment(UUID userId, TossPaymentConfirmCommand command) {
        TossPaymentResponse tossPayment = tossPaymentClient.confirm(command);

        Payment payment = Payment.of(userId, tossPayment, ORDER_PAYMENT);
        Payment saved = paymentRepository.save(payment);

        PaymentConfirmedEvent event = new PaymentConfirmedEvent(
            saved.getOrderId(),
            saved.getAmount()
        );

        try {
            String payload = objectMapper.writeValueAsString(event);

            PaymentOutboxEvent outbox = PaymentOutboxEvent.pending(
                "PAYMENT",
                saved.getId().toString(),
                "payment-confirmed",
                saved.getOrderId().toString(),
                payload
            );
            paymentOutboxEventRepository.save(outbox);

        } catch (JsonProcessingException e) {
            throw new CustomException(PaymentErrorCode.OUTBOX_SERIALIZATION_FAILED);
        }
        return ResponseDto.ok(TossPaymentConfirmInfo.from(saved));
    }


    @Transactional
    public ResponseDto<TossPaymentConfirmInfo> confirmDeposit(UUID userId, TossPaymentConfirmCommand command) {
        TossPaymentResponse tossPayment = tossPaymentClient.confirm(command);

        UUID chargeId = UUID.fromString(tossPayment.orderId());

        Payment payment = Payment.of(userId, tossPayment, DEPOSIT_CHARGE);
        Payment saved = paymentRepository.save(payment);

        DepositChargeConfirmedEvent event = new DepositChargeConfirmedEvent(
            userId,
            chargeId,
            saved.getAmount()
        );

        try {
            String payload = objectMapper.writeValueAsString(event);

            PaymentOutboxEvent outbox = PaymentOutboxEvent.pending(
                "PAYMENT",
                saved.getId().toString(),
                "deposit-charge-confirmed",
                chargeId.toString(),
                payload
            );
            paymentOutboxEventRepository.save(outbox);
        } catch (JsonProcessingException e) {
            throw new CustomException(PaymentErrorCode.OUTBOX_SERIALIZATION_FAILED);
        }

        return ResponseDto.ok(TossPaymentConfirmInfo.from(saved));
    }
}
