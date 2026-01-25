package com.barofarm.ai.config;

import com.barofarm.ai.event.model.CartLogEvent;
import com.barofarm.ai.event.model.OrderLogEvent;
import com.barofarm.ai.event.model.ReviewEvent;
import com.barofarm.ai.search.infrastructure.event.ExperienceEvent;
import com.barofarm.ai.search.infrastructure.event.ProductEvent;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

/**
 * [Kafka Consumer 설정]
 * ProductEvent, ExperienceEvent, CartEvent, OrderEvent를 각각 처리하기 위한 별도의 ConsumerFactory 설정
 * TODO: 재시도 로직 or DLQ 설정
 */
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    // === Product Event Consumer (기존) ===

    @Bean
    public ConsumerFactory<String, ProductEvent> productEventConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        // MSA 환경: 메시지에 포함된 타입 정보를 무시하고 직접 지정한 타입으로 역직렬화
        JsonDeserializer<ProductEvent> jsonDeserializer = new JsonDeserializer<>(ProductEvent.class);
        jsonDeserializer.setRemoveTypeHeaders(true);  // 타입 헤더 제거 (다른 모듈의 클래스 정보 무시)
        jsonDeserializer.setUseTypeHeaders(false);     // 타입 헤더 사용 안 함
        jsonDeserializer.addTrustedPackages("*");

        // ErrorHandlingDeserializer로 감싸서 역직렬화 실패 시 에러를 무시하고 계속 진행
        // (오래된 메시지의 타입 정보로 인한 ClassNotFoundException 무시)
        ErrorHandlingDeserializer<ProductEvent> deserializer = new ErrorHandlingDeserializer<>(jsonDeserializer);
        deserializer.setFailedDeserializationFunction(record -> null);  // 실패 시 null 반환하여 건너뛰기

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProductEvent> productEventListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ProductEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(productEventConsumerFactory());

        // 역직렬화 실패 시 즉시 건너뛰기 (재시도 없음)
        CommonErrorHandler errorHandler = new DefaultErrorHandler(
            new FixedBackOff(0L, 0L)  // 재시도 없이 즉시 건너뛰기
        );
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }

    // === Experience Event Consumer (기존) ===

    @Bean
    public ConsumerFactory<String, ExperienceEvent> experienceEventConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        // MSA 환경: 메시지에 포함된 타입 정보를 무시하고 직접 지정한 타입으로 역직렬화
        JsonDeserializer<ExperienceEvent> deserializer = new JsonDeserializer<>(ExperienceEvent.class);
        deserializer.setRemoveTypeHeaders(true);  // 타입 헤더 제거
        deserializer.setUseTypeHeaders(false);     // 타입 헤더 사용 안 함
        deserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ExperienceEvent> experienceEventListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ExperienceEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(experienceEventConsumerFactory());
        return factory;
    }

    // === 개인화 추천을 위한 이벤트 Consumer들 (신규) ===

    @Bean
    public ConsumerFactory<String, CartLogEvent> cartEventConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId + "-cart"); // 별도 그룹으로 분리
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // 개인화 데이터는 처음부터 수집

        // MSA 환경: 메시지에 포함된 타입 정보를 무시하고 직접 지정한 타입으로 역직렬화
        JsonDeserializer<CartLogEvent> deserializer = new JsonDeserializer<>(CartLogEvent.class);
        deserializer.setRemoveTypeHeaders(true);  // 타입 헤더 제거
        deserializer.setUseTypeHeaders(false);     // 타입 헤더 사용 안 함
        deserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CartLogEvent> cartEventListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CartLogEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cartEventConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, OrderLogEvent> orderEventConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId + "-order"); // 별도 그룹으로 분리
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // 개인화 데이터는 처음부터 수집

        // MSA 환경: 메시지에 포함된 타입 정보를 무시하고 직접 지정한 타입으로 역직렬화
        JsonDeserializer<OrderLogEvent> deserializer = new JsonDeserializer<>(OrderLogEvent.class);
        deserializer.setRemoveTypeHeaders(true);  // 타입 헤더 제거
        deserializer.setUseTypeHeaders(false);     // 타입 헤더 사용 안 함
        deserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderLogEvent> orderEventListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderLogEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderEventConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, ReviewEvent> reviewEventConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId + "-review"); // 별도 그룹으로 분리
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // 개인화 데이터는 처음부터 수집

        JsonDeserializer<ReviewEvent> deserializer = new JsonDeserializer<>(ReviewEvent.class);
        deserializer.setRemoveTypeHeaders(true);  // 타입 헤더 제거 (다른 모듈의 클래스 정보 무시)
        deserializer.setUseTypeHeaders(false);     // 타입 헤더 사용 안 함
        deserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ReviewEvent> reviewEventListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ReviewEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(reviewEventConsumerFactory());
        return factory;
    }
}
