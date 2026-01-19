package com.barofarm.seller.farm.infrastructure.kafka;

import com.barofarm.seller.farm.event.FarmEvent;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.backoff.BackOffExecution;
import org.springframework.util.backoff.ExponentialBackOff;

@Slf4j
@Component
@RequiredArgsConstructor
public class FarmEventProducer {

    private final KafkaTemplate<String, FarmEvent> kafkaTemplate;
    private final KafkaTemplate<String, Object> producerDlqKafkaTemplate;

    private static final String TOPIC = "farm-events";
    private static final String PRODUCER_DLQ_TOPIC = "farm-events.producer.DLQ";

    /**
     * Farm 이벤트를 Kafka 토픽으로 전송
     * - 지수 백오프를 사용한 재시도 (초기 1초, 배수 2.0)
     * - 최대 1번 재시도 (maxElapsedTime으로 제한)
     * - 재시도 실패 시 DLQ(Dead Letter Queue) 토픽으로 메시지 전송
     */
    public void send(FarmEvent event) {
        FarmEvent.FarmEventData data = event.getData();
        // 파티션 키로 farmId를 사용하여 동일 farm의 이벤트 순서 보장
        String partitionKey = data.getFarmId().toString();

        log.info(
            "📤 [PRODUCER] Sending farm event to topic '{}' - Type: {}, Farm ID: {}, Seller ID: {}, Partition Key: {}",
            TOPIC, event.getType(), data.getFarmId(), data.getSellerId(), partitionKey);

        // 지수 백오프 설정: 초기 간격 1초, 배수 2.0
        ExponentialBackOff backOff = new ExponentialBackOff();
        backOff.setInitialInterval(1000L); // 초기 간격: 1초
        backOff.setMultiplier(2.0); // 배수: 2.0 (1초 -> 2초 -> 4초 -> ...)
        backOff.setMaxInterval(10000L); // 최대 간격: 10초
        // 최대 경과 시간을 초기 간격보다 크고 두 번째 재시도 전에 끝나도록 설정 (최대 1번 재시도)
        backOff.setMaxElapsedTime(1500L); // 최대 경과 시간: 1.5초 (1번 재시도만 허용)

        sendWithRetry(event, partitionKey, backOff.start());
    }

    /**
     * 지수 백오프를 사용한 재시도 로직
     */
    private void sendWithRetry(FarmEvent event, String partitionKey, BackOffExecution backOffExecution) {
        CompletableFuture<SendResult<String, FarmEvent>> future =
            kafkaTemplate.send(TOPIC, partitionKey, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info(
                    "✅ [PRODUCER] Successfully sent farm event to topic '{}' - Type: {}, Farm ID: {}, "
                        + "Partition: {}, Offset: {}, Partition Key: {}",
                    TOPIC, event.getType(), event.getData().getFarmId(),
                    result.getRecordMetadata().partition(), result.getRecordMetadata().offset(), partitionKey);
            } else {
                // 재시도 가능 여부 확인
                long nextBackOff = backOffExecution.nextBackOff();
                if (nextBackOff != BackOffExecution.STOP) {
                    // 재시도 가능: 지수 백오프 대기 후 재시도
                    log.warn(
                        "⚠️ [PRODUCER] Failed to send farm event, retrying after {}ms - "
                            + "Topic: {}, Type: {}, Farm ID: {}, Partition Key: {}, Error: {}",
                        nextBackOff, TOPIC, event.getType(), event.getData().getFarmId(),
                        partitionKey, ex.getMessage());

                    try {
                        Thread.sleep(nextBackOff);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("❌ [PRODUCER] Retry sleep interrupted", ie);
                        sendToDlq(event, partitionKey, ex);
                        return;
                    }

                    // 재시도
                    sendWithRetry(event, partitionKey, backOffExecution);
                } else {
                    // 재시도 불가: DLQ로 전송
                    log.error(
                        "❌ [PRODUCER] Failed to send farm event after retries - "
                            + "Topic: {}, Type: {}, Farm ID: {}, Partition Key: {}, Error: {}",
                        TOPIC, event.getType(), event.getData().getFarmId(), partitionKey, ex.getMessage(), ex);
                    sendToDlq(event, partitionKey, ex);
                }
            }
        });
    }

    /**
     * DLQ로 메시지 전송
     */
    private void sendToDlq(FarmEvent event, String partitionKey, Throwable originalException) {
        try {
            producerDlqKafkaTemplate.send(PRODUCER_DLQ_TOPIC, partitionKey, event)
                .whenComplete((dlqResult, dlqEx) -> {
                    if (dlqEx == null) {
                        log.error(
                            "💀 [PRODUCER_DLQ] Failed message sent to Producer DLQ - "
                                + "Original Topic: {}, DLQ Topic: {}, Type: {}, Farm ID: {}, "
                                + "Partition: {}, Offset: {}",
                            TOPIC, PRODUCER_DLQ_TOPIC, event.getType(), event.getData().getFarmId(),
                            dlqResult.getRecordMetadata().partition(),
                            dlqResult.getRecordMetadata().offset());
                    } else {
                        log.error(
                            "💀 [PRODUCER_DLQ] CRITICAL: Failed to send to Producer DLQ - "
                                + "Original Topic: {}, DLQ Topic: {}, Type: {}, Farm ID: {}, Error: {}",
                            TOPIC, PRODUCER_DLQ_TOPIC, event.getType(), event.getData().getFarmId(),
                            dlqEx.getMessage(), dlqEx);
                    }
                });
        } catch (Exception dlqException) {
            log.error(
                "💀 [PRODUCER_DLQ] CRITICAL: Exception while sending to Producer DLQ - "
                    + "Original Topic: {}, DLQ Topic: {}, Type: {}, Farm ID: {}, Error: {}",
                TOPIC, PRODUCER_DLQ_TOPIC, event.getType(), event.getData().getFarmId(),
                dlqException.getMessage(), dlqException);
        }
    }
}
