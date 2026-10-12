package com.careerpilot.autoapply;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ConfirmedApplicationAnswerRepository extends JpaRepository<ConfirmedApplicationAnswer, UUID> {
    Optional<ConfirmedApplicationAnswer> findByOwner_IdAndJob_IdAndQuestionHash(UUID ownerId, UUID jobId, String questionHash);
}
