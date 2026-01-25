package com.barofarm.log.history.mapper;

import com.barofarm.log.history.model.HistoryEventType;
public interface HistoryPayloadMapper {
    HistoryEventType supports();

    Object payload(Object[] args, Object returnValue);

    /**
     * Kafka 리스너 등 HTTP 컨텍스트가 없는 경우,
     * 이벤트별로 userId를 직접 추출하고 싶다면 이 메서드를 override 한다.
     * null 을 리턴하면 HistoryAspect 가 기본 헤더 기반 로직을 사용한다.
     */
    default java.util.UUID resolveUserId(Object[] args, Object returnValue) {
        return null;
    }

    default boolean mapBeforeProceed() {
        return false;
    }
}
