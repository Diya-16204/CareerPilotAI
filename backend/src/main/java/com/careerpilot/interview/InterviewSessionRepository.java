package com.careerpilot.interview;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession,UUID> {
    Optional<InterviewSession> findByIdAndUser_Id(UUID id,UUID userId);
    Page<InterviewSession> findAllByUser_IdOrderByStartedAtDesc(UUID userId,Pageable pageable);
    long countByUser_Id(UUID userId);
}
