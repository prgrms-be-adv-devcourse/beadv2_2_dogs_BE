package com.barofarm.log.history.annotation;

import com.barofarm.log.history.model.HistoryEventType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TrackHistory {
    HistoryEventType value();
}
