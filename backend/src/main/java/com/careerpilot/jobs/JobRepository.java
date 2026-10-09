package com.careerpilot.jobs;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {
    @Query("select j from Job j where (j.owner is null or (:ownerId is not null and j.owner.id = :ownerId)) and (:q is null or lower(j.title) like lower(concat('%', :q, '%')) or lower(j.company) like lower(concat('%', :q, '%')) or lower(j.location) like lower(concat('%', :q, '%')) or lower(j.skillsText) like lower(concat('%', :q, '%'))) and (:employmentType is null or j.employmentType = :employmentType)")
    Page<Job> search(@Param("ownerId") UUID ownerId,@Param("q") String q,@Param("employmentType") String employmentType,Pageable pageable);
    @Query("select j from Job j where j.id=:id and (j.owner is null or (:ownerId is not null and j.owner.id=:ownerId))")
    java.util.Optional<Job> findVisibleById(@Param("id") UUID id,@Param("ownerId") UUID ownerId);
    boolean existsByOwnerIdAndTitleIgnoreCaseAndCompanyIgnoreCase(UUID ownerId,String title,String company);
}
