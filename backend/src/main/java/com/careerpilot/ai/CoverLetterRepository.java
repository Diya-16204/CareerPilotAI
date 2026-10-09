package com.careerpilot.ai;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CoverLetterRepository extends JpaRepository<CoverLetter,UUID> {
    Page<CoverLetter> findAllByUser_IdOrderByUpdatedAtDesc(UUID userId,Pageable pageable);
    Optional<CoverLetter> findByIdAndUser_Id(UUID id,UUID userId);
}
