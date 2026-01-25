package com.barofarm.buyer.inventory.infrastructure.kafka.consumer;

import com.barofarm.buyer.inventory.application.InventoryFacadeService;
import com.barofarm.buyer.inventory.domain.InventoryOutboxEvent;
import com.barofarm.buyer.inventory.domain.InventoryOutboxEventRepository;
import com.barofarm.buyer.inventory.exception.InventoryErrorCode;
import com.barofarm.buyer.inventory.infrastructure.kafka.consumer.dto.PaymentConfirmedEvent;
import com.barofarm.buyer.inventory.infrastructure.kafka.producer.dto.InventoryConfirmedEvent;
import com.barofarm.buyer.inventory.infrastructure.kafka.producer.dto.InventoryConfirmedFailEvent;
import com.barofarm.exception.CustomException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentConfirmedConsumer {

    private final InventoryFacadeService inventoryFacadeService;
    private final ObjectMapper objectMapper;
    private final InventoryOutboxEventRepository inventoryOutboxEventRepository;

    @KafkaListener(
        topics = "payment-confirmed",
        groupId = "inventory-service.payment-confirmed",
        properties = {
            "spring.json.value.default.type="
                + "com.barofarm.buyer.inventory.infrastructure.kafka.consumer.dto.PaymentConfirmedEvent"
        }
    )
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Transactional
    public void handle(PaymentConfirmedEvent event) {
        UUID orderId = event.orderId();
        try {
            inventoryFacadeService.confirmForPaymentSaga(orderId);

            String payload =
                objectMapper.writeValueAsString(InventoryConfirmedEvent.from(event));

            inventoryOutboxEventRepository.save(
                InventoryOutboxEvent.pending(
                    "INVENTORY",
                    orderId.toString(),
                    "inventory-confirmed",
                    orderId.toString(),
                    payload
                )
            );
        } catch (CustomException customException) {
            try {
                String payload =
                    objectMapper.writeValueAsString(InventoryConfirmedFailEvent.from(event));
                inventoryOutboxEventRepository.save(
                    InventoryOutboxEvent.pending(
                        "INVENTORY",
                        orderId.toString(),
                        "inventory-confirmed-fail",
                        orderId.toString(),
                        payload
                    )
                );
            } catch (JsonProcessingException jsonProcessingException) {
                throw new CustomException(InventoryErrorCode.OUTBOX_SERIALIZATION_FAILED);
            }
        } catch (JsonProcessingException jsonProcessingException) {
            throw new CustomException(InventoryErrorCode.OUTBOX_SERIALIZATION_FAILED);
        }
    }
}
