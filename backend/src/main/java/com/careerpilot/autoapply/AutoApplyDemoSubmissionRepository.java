package com.careerpilot.autoapply;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AutoApplyDemoSubmissionRepository extends JpaRepository<AutoApplyDemoSubmission, UUID> {
    Optional<AutoApplyDemoSubmission> findBySessionIdAndOwnerId(UUID sessionId, UUID ownerId);
}
