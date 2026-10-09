package com.careerpilot.applications;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application,UUID> {
    boolean existsByUserIdAndJobId(UUID userId,UUID jobId);
    Page<Application> findAllByUserIdOrderByUpdatedAtDesc(UUID userId,Pageable pageable);
    long countByUserId(UUID userId);
    long countByUserIdAndStatus(UUID userId,ApplicationStatus status);
    java.util.List<Application> findTop5ByUserIdOrderByUpdatedAtDesc(UUID userId);
}
