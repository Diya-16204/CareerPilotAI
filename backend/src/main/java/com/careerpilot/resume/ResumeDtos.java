package com.careerpilot.resume;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ResumeDtos {
    private ResumeDtos(){}
    public record ReviewRequest(@Email @Size(max=254) String email,@Size(max=48) String phone,@Size(max=40) List<@NotBlank @Size(max=80) String> skills){}
    public record VersionResponse(UUID id,int versionNumber,String originalFileName,String contentType,String extractedEmail,String extractedPhone,List<String> skills,boolean reviewed,Instant createdAt,String extractedText){}
    public record ResumeResponse(UUID id,String title,int activeVersionNumber,Instant createdAt,Instant updatedAt,List<VersionResponse> versions){}
    public record ResumeSummary(UUID id,String title,int activeVersionNumber,Instant createdAt,Instant updatedAt,int versionCount,String latestFileName){}
}
