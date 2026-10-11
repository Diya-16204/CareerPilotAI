package com.careerpilot.jobs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;

public interface SavedJobRepository extends JpaRepository<SavedJob,UUID> {
    boolean existsByUser_IdAndJob_Id(UUID userId,UUID jobId);
    void deleteByUser_IdAndJob_Id(UUID userId,UUID jobId);

    @Query("select savedJob.job from SavedJob savedJob where savedJob.user.id = :userId")
    Page<Job> findJobsSavedByUser(@Param("userId") UUID userId, Pageable pageable);
}
