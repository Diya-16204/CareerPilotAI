package com.careerpilot.applications;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application,UUID> {
    boolean existsByUser_IdAndJob_Id(UUID userId,UUID jobId);
    Page<Application> findAllByUser_IdOrderByUpdatedAtDesc(UUID userId,Pageable pageable);
    long countByUser_Id(UUID userId);
    long countByUser_IdAndStatus(UUID userId,ApplicationStatus status);
    java.util.List<Application> findTop5ByUser_IdOrderByUpdatedAtDesc(UUID userId);
}
