package com.careerpilot.applications;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public final class ApplicationDtos {
    private ApplicationDtos(){}
    public record CreateRequest(UUID jobId,@NotBlank @Size(max=160) String title,@NotBlank @Size(max=160) String company,ApplicationStatus status,@Size(max=4000) String notes){}
    public record StatusRequest(@NotNull ApplicationStatus status,@Size(max=500) String note){}
    public record Response(UUID id,String title,String company,ApplicationStatus status,Instant appliedAt,Instant updatedAt){}
    public record Activity(UUID id,String description,Instant occurredAt){}
    public record AnalyticsSummary(long totalApplications,long interviews,long offers,long practiceSessions,long answersSubmitted,java.util.Map<ApplicationStatus,Long> applicationsByStatus,java.util.List<Activity> recentActivity){}
}
