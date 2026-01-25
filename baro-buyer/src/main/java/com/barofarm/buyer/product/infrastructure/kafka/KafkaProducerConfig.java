package com.barofarm.buyer.product.infrastructure.kafka;

import com.barofarm.buyer.product.event.ProductEvent;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

// 카프카 이벤트 발행을 위한 설정 파일
@Configuration
@EnableKafka
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, ProductEvent> productEventProducerFactory() {
        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // MSA 환경: 타입 헤더를 추가하지 않도록 설정 (다른 모듈의 클래스 정보 제거)
        JsonSerializer<ProductEvent> serializer = new JsonSerializer<>();
        serializer.setAddTypeInfo(false);  // 타입 헤더(__TypeId__) 추가 안 함

        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, serializer);
        // TODO: 3번 재시도 너무 많음, DLQ(Dead Letter Queue) 사용 고려
        config.put(ProducerConfig.RETRIES_CONFIG, 3); // 최대 3번 재시도
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // 중복 방지

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, ProductEvent> productEventKafkaTemplate() {
        return new KafkaTemplate<>(productEventProducerFactory());
    }

    @Bean
    public ProducerFactory<String, String> historyProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    @Primary
    public KafkaTemplate<String, String> historyKafkaTemplate() {
        return new KafkaTemplate<>(historyProducerFactory());
    }
}
