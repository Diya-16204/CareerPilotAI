package com.careerpilot.resume;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResumeVersionRepository extends JpaRepository<ResumeVersion,UUID> {
    List<ResumeVersion> findAllByResumeIdOrderByVersionNumberDesc(UUID resumeId);
    Optional<ResumeVersion> findByIdAndResumeId(UUID id,UUID resumeId);
    Optional<ResumeVersion> findByResumeIdAndVersionNumber(UUID resumeId,int versionNumber);
    int countByResumeId(UUID resumeId);
}
