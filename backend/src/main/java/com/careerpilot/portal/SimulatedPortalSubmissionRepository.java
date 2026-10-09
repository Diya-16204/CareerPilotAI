package com.careerpilot.portal;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SimulatedPortalSubmissionRepository extends JpaRepository<SimulatedPortalSubmission,UUID> {
    Optional<SimulatedPortalSubmission> findByOwnerUserIdAndPortalKeyAndIdempotencyKey(UUID ownerUserId,String portalKey,UUID idempotencyKey);
    Optional<SimulatedPortalSubmission> findByConfirmationIdAndOwnerUserId(UUID confirmationId,UUID ownerUserId);
}
