package com.careerpilot.jobs;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SavedJobRepository extends JpaRepository<SavedJob,UUID> {
    boolean existsByUser_IdAndJob_Id(UUID userId,UUID jobId);
    void deleteByUser_IdAndJob_Id(UUID userId,UUID jobId);
}
