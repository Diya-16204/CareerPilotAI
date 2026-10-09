package com.careerpilot.ai;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CoverLetterRepository extends JpaRepository<CoverLetter,UUID> {
    Page<CoverLetter> findAllByUserIdOrderByUpdatedAtDesc(UUID userId,Pageable pageable);
    Optional<CoverLetter> findByIdAndUserId(UUID id,UUID userId);
}
