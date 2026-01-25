package com.barofarm.opabundle.infra.kafka;

import com.barofarm.opabundle.application.OpaBundleService;
import com.barofarm.opabundle.application.dto.HotlistEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// 토픽에서 이벤트 받아서 서비스로 전달하는 입구
// 카프카 로그는 Kafka 브로커 디스크에 쌓이고, 브로커에서 읽어온 이벤트를 JVM메모리에 반영

@Component
// [0] Kafka에서 hotlist 이벤트를 소비해 서비스 계층으로 전달하는 리스너 클래스.
public class HotlistEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(HotlistEventConsumer.class);

    // [1] Kafka listener that feeds hotlist events into the bundle service.
    private final OpaBundleService opaBundleService;

    public HotlistEventConsumer(OpaBundleService opaBundleService) {
        this.opaBundleService = opaBundleService;
    }

    // [2] Consume JSON hotlist events from the configured topic.
    @KafkaListener(topics = "${opa.kafka.topic:opa-hotlist-events}")
    public void consume(HotlistEvent event) {
        if (event == null) {
            LOG.warn("Received null hotlist event");
            return;
        }
        opaBundleService.handleEvent(event);
    }
}
