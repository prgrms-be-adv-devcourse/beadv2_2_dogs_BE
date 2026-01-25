package com.barofarm.auth.infrastructure.jpa;

import com.barofarm.auth.domain.outbox.OutboxEvent;
import com.barofarm.auth.domain.outbox.OutboxStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
