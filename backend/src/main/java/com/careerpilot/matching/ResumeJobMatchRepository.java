package com.careerpilot.matching;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ResumeJobMatchRepository extends JpaRepository<ResumeJobMatch,UUID> {
    List<ResumeJobMatch> findTop20ByUser_IdOrderByCreatedAtDesc(UUID userId);
}
