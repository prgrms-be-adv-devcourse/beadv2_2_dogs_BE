package com.barofarm.support.delivery.application;

import static com.barofarm.support.delivery.exception.DeliveryErrorCode.DELIVERY_NOT_FOUND;

import com.barofarm.support.common.exception.CustomException;
import com.barofarm.support.delivery.application.dto.DeliveryCreateCommand;
import com.barofarm.support.delivery.application.dto.DeliveryDetailInfo;
import com.barofarm.support.delivery.application.dto.ShipDeliveryCommand;
import com.barofarm.support.delivery.domain.Delivery;
import com.barofarm.support.delivery.domain.DeliveryRepository;
import com.barofarm.support.delivery.exception.DeliveryErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;

    @Transactional
    public DeliveryDetailInfo createDelivery(DeliveryCreateCommand command) {
        validOrder(command);

        Delivery delivery = Delivery.create(
            command.orderId(),
            command.address()
        );

        Delivery saved = deliveryRepository.save(delivery);

        return DeliveryDetailInfo.from(saved);
    }

    @Transactional(readOnly = true)
    public DeliveryDetailInfo getByOrderId(UUID orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
            .orElseThrow(() -> new CustomException(DELIVERY_NOT_FOUND));

        return DeliveryDetailInfo.from(delivery);
    }

    @Transactional
    public DeliveryDetailInfo ship(ShipDeliveryCommand command) {
//       #유저 역할 확인
//        MemberRole role = MemberRole.from(command.sellerRole());
//        if (role != MemberRole.SELLER) {
//            throw new CustomException(DeliveryErrorCode.FORBIDDEN_ONLY_SELLER);
//        }

        // 2. Delivery 조회
        Delivery delivery = deliveryRepository.findById(command.id())
            .orElseThrow(() -> new CustomException(DELIVERY_NOT_FOUND));

        // 3. 주문 정보 조회 (Feign) + 판매자 소유 검증
//        OrderOwnerResponse orderOwner =
//            orderClient.getOrderOwner(delivery.getOrderId());
//
//        if (!orderOwner.sellerId().equals(command.sellerId())) {
//            throw new CustomException(DeliveryErrorCode.FORBIDDEN_NOT_ORDER_SELLER);
//        }

        // 5. 도메인 상태 변경
        delivery.ship(command.courier(), command.trackingNumber());

        return DeliveryDetailInfo.from(delivery);
    }

    @Transactional
    public void startDelivery(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new CustomException(DELIVERY_NOT_FOUND));

        delivery.startDelivery();
    }

    @Transactional
    public void completeDelivery(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new CustomException(DELIVERY_NOT_FOUND));

        delivery.completeDelivery();
    }

    private void validOrder(DeliveryCreateCommand command) {
        if (deliveryRepository.existsByOrderId(command.orderId())) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_ALREADY_EXISTS);
        }
    }
}
