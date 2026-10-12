package com.careerpilot.autoapply;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationPrepSessionRepository extends JpaRepository<ApplicationPrepSession, UUID> {
    @EntityGraph(attributePaths = {"job", "resumeVersion"})
    List<ApplicationPrepSession> findTop50ByOwner_IdOrderByUpdatedAtDesc(UUID ownerId);

    @EntityGraph(attributePaths = {"job", "resumeVersion"})
    Optional<ApplicationPrepSession> findByIdAndOwner_Id(UUID id, UUID ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"job", "resumeVersion"})
    @Query("select s from ApplicationPrepSession s where s.id = :id and s.owner.id = :ownerId")
    Optional<ApplicationPrepSession> lockByIdAndOwner_Id(@Param("id") UUID id, @Param("ownerId") UUID ownerId);
}
