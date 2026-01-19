package com.barofarm.log.history.mapper;

import com.barofarm.log.history.model.HistoryEventType;
public interface HistoryPayloadMapper {
    HistoryEventType supports();

    Object payload(Object[] args, Object returnValue);

    default boolean mapBeforeProceed() {
        return false;
    }
}
