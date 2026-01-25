package com.barofarm.support.notification_delivery.infrastructure.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;

/**
 * Kafka Consumer 설정
 *
 * 목표:
 * - enable-auto-commit=false
 * - 메시지 처리 성공 시만 ack.acknowledge()
 * - 실패 시 ErrorHandler가 재시도 후 DLQ
 *
 * 주의:
 * - spring.kafka.* 속성으로도 대부분 설정 가능하지만
 * - "AckMode", "ErrorHandler" 같은 핵심 운영 옵션은 Java Config로 명시하는 게 좋다.
 */
@Configuration
@Profile("!local & !mock & !local-mail")
public class NotificationKafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, String> consumerFactory(KafkaProperties props) {
        Map<String, Object> config = new HashMap<>(props.buildConsumerProperties());

        // 문자열 JSON을 받을 것이므로 StringDeserializer
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // auto commit 금지(정확한 처리 보장)
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
        ConsumerFactory<String, String> consumerFactory,
        CommonErrorHandler errorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        // 수동 Ack 모드: 성공 시에만 커밋됨
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        // 알림 전용 에러 핸들러(재시도/DLQ): KafkaErrorHandlerConfig.defaultErrorHandler
        factory.setCommonErrorHandler(errorHandler);

        // concurrency는 파티션 수에 맞춰 조절
        // factory.setConcurrency(2);

        return factory;
    }
}
