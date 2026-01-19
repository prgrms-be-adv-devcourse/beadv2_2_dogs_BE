package com.barofarm.config;

import com.barofarm.log.history.aspect.HistoryAspect;
import com.barofarm.log.history.mapper.HistoryPayloadMapper;
import com.barofarm.log.history.writer.HistoryLogWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.kafka.core.KafkaTemplate;

@AutoConfiguration
@EnableAspectJAutoProxy
@ConditionalOnProperty(prefix = "history.log", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(HistoryLogProperties.class)
public class HistoryLogAutoConfiguration {

    @Bean
    @ConditionalOnBean(KafkaTemplate.class)
    public HistoryLogWriter historyLogWriter(
        ObjectMapper objectMapper,
        KafkaTemplate<?, ?> kafkaTemplate,
        HistoryLogProperties properties
    ) {
        return new HistoryLogWriter(objectMapper, kafkaTemplate, properties);
    }

    @Bean
    @ConditionalOnBean(HistoryLogWriter.class)
    public HistoryAspect historyAspect(
        List<HistoryPayloadMapper> mappers,
        HistoryLogWriter writer,
        HistoryLogProperties properties
    ) {
        return new HistoryAspect(mappers, writer, properties);
    }
}
