package com.barofarm.order.deposit.application;

import static com.barofarm.order.deposit.exception.DepositErrorCode.DEPOSIT_ALREADY_EXISTS;

import com.barofarm.order.common.exception.CustomException;
import com.barofarm.order.common.response.ResponseDto;
import com.barofarm.order.deposit.application.dto.request.DepositChargeCreateCommand;
import com.barofarm.order.deposit.application.dto.request.DepositPaymentCommand;
import com.barofarm.order.deposit.application.dto.request.DepositRefundCommand;
import com.barofarm.order.deposit.application.dto.response.DepositChargeCreateInfo;
import com.barofarm.order.deposit.application.dto.response.DepositCreateInfo;
import com.barofarm.order.deposit.application.dto.response.DepositInfo;
import com.barofarm.order.deposit.application.dto.response.DepositPaymentInfo;
import com.barofarm.order.deposit.application.dto.response.DepositRefundInfo;
import com.barofarm.order.deposit.domain.Deposit;
import com.barofarm.order.deposit.domain.DepositCharge;
import com.barofarm.order.deposit.domain.DepositChargeRepository;
import com.barofarm.order.deposit.domain.DepositRepository;
import com.barofarm.order.deposit.exception.DepositErrorCode;
import com.barofarm.order.order.application.OrderService;
import com.barofarm.order.order.application.dto.request.DeliveryInternalCreateRequest;
import com.barofarm.order.order.application.dto.response.OrderDeliveryInfo;
import com.barofarm.order.order.client.DeliveryClient;
import com.barofarm.order.payment.domain.Payment;
import com.barofarm.order.payment.domain.PaymentRepository;
import com.barofarm.order.payment.domain.PaymentStatus;
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
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final DeliveryClient deliveryClient;

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

        // 잔액 부족 체크
        if (deposit.getAmount() < command.amount()) {
            throw new CustomException(DepositErrorCode.INSUFFICIENT_DEPOSIT_BALANCE);
        }

        deposit.decrease(command.amount());

        orderService.markOrderPaid(userId, command.orderId());

        Payment payment = Payment.of(command.orderId(), command.amount());
        paymentRepository.save(payment);

        // delivery-service에서 특정 시간 이후 order의 상태를 변경하도록 하면 좋을듯.
        try {
            OrderDeliveryInfo deliveryInfo = orderService.getDeliveryInfo(command.orderId());
            DeliveryInternalCreateRequest request = DeliveryInternalCreateRequest.from(deliveryInfo);

            deliveryClient.createDelivery(request);

        } catch (Exception e) {
            // 배송 생성 실패: 주문은 PAID 유지(재시도/운영)
            log.error("배송 생성 실패. orderId={}", command.orderId(), e);
        }

        return ResponseDto.ok(
            DepositPaymentInfo.of(
                command.orderId(),
                command.amount(),
                deposit.getAmount()
            )
        );
    }

    @Transactional
    public ResponseDto<DepositRefundInfo> refundDeposit(UUID userId, DepositRefundCommand command) {

        Deposit deposit = depositRepository.findByUserId(userId)
            .orElseThrow(() -> new CustomException(DepositErrorCode.DEPOSIT_NOT_FOUND));

        String paymentKey = "DEPOSIT:" + command.orderId();
        Payment payment = paymentRepository.findByPaymentKey(paymentKey)
            .orElseThrow(() -> new CustomException(DepositErrorCode.DEPOSIT_PAYMENT_NOT_FOUND));

        // 멱등: 이미 환불 처리된 경우 그대로 응답(또는 return)
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            return ResponseDto.ok(
                DepositRefundInfo.of(command.orderId(), 0L, deposit.getAmount())
            );
        }

        // 예치금 복구
        deposit.increase(command.amount());

        orderService.cancelOrder(userId, command.orderId());

        // 결제 상태 환불 처리
        payment.refund();

        return ResponseDto.ok(
            DepositRefundInfo.of(command.orderId(), command.amount(), deposit.getAmount())
        );
    }
}
