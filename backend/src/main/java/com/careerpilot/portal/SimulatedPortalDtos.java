package com.careerpilot.portal;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SimulatedPortalDtos {
    private SimulatedPortalDtos() {}
    public record PortalJob(String key,String title,String department,String location,String description,List<String> requirements,List<String> questions) {}
    public record SubmissionRequest(String jobKey,String applicantName,String applicantEmail,String resumeFileName,String resumeBase64,Map<String,String> answers,Boolean consent) {}
    public record SubmissionResponse(UUID confirmationId,String portalKey,String jobKey,String jobTitle,Instant createdAt,boolean reused) {}
    public record VerificationResponse(UUID confirmationId,String portalKey,String jobKey,String jobTitle,Instant createdAt,String status) {}
}
