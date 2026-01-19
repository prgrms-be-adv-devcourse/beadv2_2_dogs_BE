package com.barofarm.support.deposit.infrastructure.kafka.consumer;

import com.barofarm.exception.CustomException;
import com.barofarm.support.deposit.domain.Deposit;
import com.barofarm.support.deposit.domain.DepositCharge;
import com.barofarm.support.deposit.domain.DepositChargeRepository;
import com.barofarm.support.deposit.domain.DepositOutboxEvent;
import com.barofarm.support.deposit.domain.DepositOutboxEventRepository;
import com.barofarm.support.deposit.exception.DepositErrorCode;
import com.barofarm.support.deposit.infrastructure.kafka.dto.DepositChargeConfirmFailedEvent;
import com.barofarm.support.deposit.infrastructure.kafka.dto.DepositChargeConfirmedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DepositChargeConfirmedConsumer {

    private final DepositChargeRepository depositChargeRepository;
    private final DepositOutboxEventRepository depositOutboxEventRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = "deposit-charge-confirmed",
        groupId = "support-service.deposit-charge-confirmed",
        properties = {
            "spring.json.value.default.type="
                + "com.barofarm.support.deposit.infrastructure.kafka.dto.DepositChargeConfirmedEvent"
        }
    )
    @Transactional
    public void handle(DepositChargeConfirmedEvent event) {
        UUID chargeId = event.chargeId();

        try {
            DepositCharge charge = depositChargeRepository.findById(chargeId)
                .orElse(null);

            if (charge == null) {
                String reason = "DepositCharge not found for confirmed event. chargeId=" + chargeId;
                createFailureOutbox(event, reason);
                log.warn(reason);
                return;
            }

            if (!charge.isPending()) {
                // 이미 처리된 상태이면 멱등 처리
                return;
            }

            Deposit deposit = charge.getDeposit();
            if (!deposit.getUserId().equals(event.userId())) {
                throw new CustomException(DepositErrorCode.DEPOSIT_ACCESS_DENIED);
            }

            deposit.increase(event.amount());
            charge.success();

            log.info(
                "Deposit charge confirmed. userId={}, chargeId={}, amount={}",
                event.userId(),
                chargeId,
                event.amount()
            );
        } catch (RuntimeException e) {
            createFailureOutbox(event, e.getMessage());
            throw e;
        }
    }

    private void createFailureOutbox(DepositChargeConfirmedEvent event, String reason) {
        try {
            DepositChargeConfirmFailedEvent failedEvent = new DepositChargeConfirmFailedEvent(
                event.userId(),
                event.chargeId(),
                event.amount(),
                reason
            );

            String payload = objectMapper.writeValueAsString(failedEvent);

            DepositOutboxEvent outboxEvent = DepositOutboxEvent.pending(
                "DEPOSIT",
                event.chargeId().toString(),
                "deposit-charge-confirm-failed",
                event.chargeId().toString(),
                payload
            );
            depositOutboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException ex) {
            throw new CustomException(DepositErrorCode.OUTBOX_SERIALIZATION_FAILED);
        }
    }
}
