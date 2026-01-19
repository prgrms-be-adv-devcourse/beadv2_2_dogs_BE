package com.barofarm.support.deposit.application;

import static com.barofarm.support.deposit.exception.DepositErrorCode.DEPOSIT_ALREADY_EXISTS;

import com.barofarm.dto.ResponseDto;
import com.barofarm.exception.CustomException;
import com.barofarm.support.deposit.application.dto.request.DepositChargeCreateCommand;
import com.barofarm.support.deposit.application.dto.request.DepositPaymentCommand;
import com.barofarm.support.deposit.application.dto.request.DepositRefundCommand;
import com.barofarm.support.deposit.application.dto.response.DepositChargeCreateInfo;
import com.barofarm.support.deposit.application.dto.response.DepositCreateInfo;
import com.barofarm.support.deposit.application.dto.response.DepositInfo;
import com.barofarm.support.deposit.application.dto.response.DepositPaymentInfo;
import com.barofarm.support.deposit.application.dto.response.DepositRefundInfo;
import com.barofarm.support.deposit.domain.Deposit;
import com.barofarm.support.deposit.domain.DepositCharge;
import com.barofarm.support.deposit.domain.DepositChargeRepository;
import com.barofarm.support.deposit.domain.DepositOutboxEvent;
import com.barofarm.support.deposit.domain.DepositOutboxEventRepository;
import com.barofarm.support.deposit.domain.DepositRepository;
import com.barofarm.support.deposit.exception.DepositErrorCode;
import com.barofarm.support.deposit.infrastructure.kafka.dto.DepositPaymentRequestedEvent;
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
public class DepositService {

    private final DepositRepository depositRepository;
    private final DepositChargeRepository depositChargeRepository;
    private final DepositOutboxEventRepository depositOutboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ResponseDto<DepositCreateInfo> createDeposit(UUID userId) {
        boolean exists = depositRepository.findByUserId(userId).isPresent();
        if (exists) {
            throw new CustomException(DEPOSIT_ALREADY_EXISTS);
        }
        Deposit saved = depositRepository.save(Deposit.of(userId));
        return ResponseDto.ok(DepositCreateInfo.from(saved));
    }

    @Transactional
    public ResponseDto<DepositChargeCreateInfo> createCharge(UUID userId, DepositChargeCreateCommand command) {

        Deposit deposit = depositRepository.findByUserId(userId)
            .orElseThrow(() -> new CustomException(DepositErrorCode.DEPOSIT_NOT_FOUND));

        DepositCharge charge = DepositCharge.of(command.amount(), deposit);
        DepositCharge saved = depositChargeRepository.save(charge);
        return ResponseDto.ok(DepositChargeCreateInfo.from(saved));
    }

    @Transactional
    public void markDepositCharge(UUID userId, UUID chargeId) {

        DepositCharge charge = depositChargeRepository.findById(chargeId)
            .orElseThrow(() -> new CustomException(DepositErrorCode.DEPOSIT_CHARGE_NOT_FOUND));

        if (!charge.isPending()) {
            throw new CustomException(DepositErrorCode.DEPOSIT_CHARGE_INVALID_STATUS);
        }
        Deposit deposit = charge.getDeposit();
        if (!deposit.getUserId().equals(userId)) {
            throw new CustomException(DepositErrorCode.DEPOSIT_ACCESS_DENIED);
        }

        deposit.increase(charge.getAmount());
        charge.success();
    }

    @Transactional(readOnly = true)
    public ResponseDto<DepositInfo> findDeposit(UUID userId) {
        Deposit deposit = depositRepository.findByUserId(userId)
            .orElseThrow(() -> new CustomException(DepositErrorCode.DEPOSIT_NOT_FOUND));

        return ResponseDto.ok(DepositInfo.from(deposit));
    }

    @Transactional
    public ResponseDto<DepositPaymentInfo> payDeposit(UUID userId, DepositPaymentCommand command) {
        Deposit deposit = depositRepository.findByUserId(userId)
            .orElseThrow(() -> new CustomException(DepositErrorCode.DEPOSIT_NOT_FOUND));

        if (deposit.getAmount() < command.amount()) {
            throw new CustomException(DepositErrorCode.INSUFFICIENT_DEPOSIT_BALANCE);
        }
        deposit.decrease(command.amount());

        DepositPaymentRequestedEvent event = new DepositPaymentRequestedEvent(
            userId,
            command.orderId(),
            command.amount()
        );
        try {
            String payload = objectMapper.writeValueAsString(event);

            DepositOutboxEvent outboxEvent = DepositOutboxEvent.pending(
                "DEPOSIT",
                deposit.getId().toString(),
                "deposit-payment-requested",
                command.orderId().toString(),
                payload
            );
            depositOutboxEventRepository.save(outboxEvent);

            return ResponseDto.ok(
                DepositPaymentInfo.of(
                    command.orderId(),
                    command.amount(),
                    deposit.getAmount()
                )
            );
        } catch (JsonProcessingException e) {
            throw new CustomException(DepositErrorCode.OUTBOX_SERIALIZATION_FAILED);
        }
    }

    @Transactional
    public ResponseDto<DepositRefundInfo> refundDeposit(UUID userId, DepositRefundCommand command) {

        Deposit deposit = depositRepository.findByUserId(userId)
            .orElseThrow(() -> new CustomException(DepositErrorCode.DEPOSIT_NOT_FOUND));

        deposit.increase(command.amount());

        return ResponseDto.ok(
            DepositRefundInfo.of(command.orderId(), command.amount(), deposit.getAmount())
        );
    }
}
