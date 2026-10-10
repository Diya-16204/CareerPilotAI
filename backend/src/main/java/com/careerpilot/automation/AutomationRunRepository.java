package com.careerpilot.automation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface AutomationRunRepository extends JpaRepository<AutomationRun,UUID> {
    Optional<AutomationRun> findByOwnerUserIdAndIdempotencyKey(UUID ownerUserId,UUID idempotencyKey);
    Optional<AutomationRun> findByIdAndOwnerUserId(UUID id,UUID ownerUserId);
    Page<AutomationRun> findAllByOwnerUserIdOrderByCreatedAtDesc(UUID ownerUserId,Pageable pageable);
    List<AutomationRun> findAllByStatusIn(List<AutomationRunStatus> statuses);
}
