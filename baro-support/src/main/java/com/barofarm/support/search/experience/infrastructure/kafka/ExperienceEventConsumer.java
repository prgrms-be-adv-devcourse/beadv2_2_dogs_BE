package com.barofarm.support.search.experience.infrastructure.kafka;

import com.barofarm.support.event.ExperienceEvent;
import com.barofarm.support.search.experience.application.ExperienceSearchService;
import com.barofarm.support.search.experience.application.dto.ExperienceIndexRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExperienceEventConsumer {

    private final ExperienceSearchService experienceSearchService;

    // TODO: Producer 구현 후 테스트용 Controller 및 IndexRequest DTO 삭제 예정
    // - ExperienceIndexingController
    // - ExperienceIndexRequest

    // Experience 모듈에서 상품 CRUD 시 experience-events 토픽에 메세지 발행
    @KafkaListener(topics = "experience-events", groupId = "search-service")
    public void onMessage(ExperienceEvent event) {
        ExperienceEvent.ExperienceEventData data = event.getData();
        switch (event.getType()) {
            case EXPERIENCE_CREATED, EXPERIENCE_UPDATED -> experienceSearchService.indexExperience(toRequest(data));
            case EXPERIENCE_DELETED -> experienceSearchService.deleteExperience(data.getExperienceId());
            default -> {
                // enum의 모든 케이스를 처리하므로 도달 불가능하지만 Checkstyle 요구사항 충족
            }
        }
    }

    private ExperienceIndexRequest toRequest(ExperienceEvent.ExperienceEventData data) {
        return new ExperienceIndexRequest(
            data.getExperienceId(),
            data.getExperienceName(),
            data.getPricePerPerson(),
            data.getCapacity(),
            data.getDurationMinutes(),
            data.getAvailableStartDate(),
            data.getAvailableEndDate(),
            data.getStatus());
    }
}
