package com.careerpilot.resume;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AtsAnalysisRepository extends JpaRepository<AtsAnalysis,UUID> {
    List<AtsAnalysis> findTop20ByUser_IdAndResumeVersion_IdOrderByCreatedAtDesc(UUID userId,UUID versionId);
}
