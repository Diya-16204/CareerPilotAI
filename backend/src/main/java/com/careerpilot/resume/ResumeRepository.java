package com.careerpilot.resume;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResumeRepository extends JpaRepository<Resume,UUID> {
    List<Resume> findAllByUser_IdOrderByUpdatedAtDesc(UUID userId);
    Optional<Resume> findByIdAndUser_Id(UUID id,UUID userId);
}
