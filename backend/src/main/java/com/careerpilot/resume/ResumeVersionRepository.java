package com.careerpilot.resume;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResumeVersionRepository extends JpaRepository<ResumeVersion,UUID> {
    List<ResumeVersion> findAllByResume_IdOrderByVersionNumberDesc(UUID resumeId);
    Optional<ResumeVersion> findByIdAndResume_Id(UUID id,UUID resumeId);
    Optional<ResumeVersion> findByResume_IdAndVersionNumber(UUID resumeId,int versionNumber);
    int countByResume_Id(UUID resumeId);
}
