package com.barofarm.auth.application.port;

import com.barofarm.auth.application.event.HotlistEventMessage;

public interface HotlistEventPublisher {
    void publish(HotlistEventMessage message);
}
