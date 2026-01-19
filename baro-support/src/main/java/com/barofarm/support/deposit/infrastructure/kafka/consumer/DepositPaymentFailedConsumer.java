package com.barofarm.support.deposit.infrastructure.kafka.consumer;

import com.barofarm.exception.CustomException;
import com.barofarm.support.deposit.domain.Deposit;
import com.barofarm.support.deposit.domain.DepositRepository;
import com.barofarm.support.deposit.exception.DepositErrorCode;
import com.barofarm.support.deposit.infrastructure.kafka.dto.DepositPaymentFailedEvent;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DepositPaymentFailedConsumer {

    private final DepositRepository depositRepository;

    @KafkaListener(
        topics = "deposit-payment-failed",
        groupId = "support-service.deposit-payment-failed",
        properties = {
            "spring.json.value.default.type="
                + "com.barofarm.support.deposit.infrastructure.kafka.dto.DepositPaymentFailedEvent"
        }
    )
    @Transactional
    public void handle(DepositPaymentFailedEvent event) {
        UUID userId = event.userId();

        Deposit deposit = depositRepository.findByUserId(userId)
            .orElseThrow(() -> new CustomException(DepositErrorCode.DEPOSIT_NOT_FOUND));

        deposit.increase(event.amount());

        log.warn(
            "Deposit payment failed. userId={}, orderId={}, amount={}, reason={}",
            event.userId(),
            event.orderId(),
            event.amount(),
            event.reason()
        );
    }
}
