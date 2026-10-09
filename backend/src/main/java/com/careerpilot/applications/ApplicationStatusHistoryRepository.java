package com.careerpilot.applications;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ApplicationStatusHistoryRepository extends JpaRepository<ApplicationStatusHistory,UUID> {}
