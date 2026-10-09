package com.careerpilot.jobs;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SavedJobRepository extends JpaRepository<SavedJob,UUID> {
    boolean existsByUserIdAndJobId(UUID userId,UUID jobId);
    void deleteByUserIdAndJobId(UUID userId,UUID jobId);
}
