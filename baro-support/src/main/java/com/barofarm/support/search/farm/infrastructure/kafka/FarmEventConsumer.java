package com.barofarm.support.search.farm.infrastructure.kafka;

import com.barofarm.support.event.FarmEvent;
import com.barofarm.support.search.farm.application.FarmSearchService;
import com.barofarm.support.search.farm.application.dto.FarmIndexRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FarmEventConsumer {

    private final FarmSearchService farmSearchService;

    // TODO: Producer 구현 후 테스트용 Controller 및 IndexRequest DTO 삭제 예정
    // - FarmIndexingController
    // - FarmIndexRequest

    // Farm 모듈에서 상품 CRUD 시 farm-events 토픽에 메세지 발행
    @KafkaListener(topics = "farm-events", groupId = "search-service")
    public void onMessage(FarmEvent event) {
        FarmEvent.FarmEventData data = event.getData();
        switch (event.getType()) {
            case FARM_CREATED, FARM_UPDATED -> farmSearchService.indexFarm(toRequest(data));
            case FARM_DELETED -> farmSearchService.deleteFarm(data.getFarmId());
            default -> {
                // enum의 모든 케이스를 처리하므로 도달 불가능하지만 Checkstyle 요구사항 충족
            }
        }
    }

    private FarmIndexRequest toRequest(FarmEvent.FarmEventData data) {
        return new FarmIndexRequest(
            data.getFarmId(),
            data.getFarmName(),
            data.getFarmAddress(),
            data.getStatus());
    }
}
