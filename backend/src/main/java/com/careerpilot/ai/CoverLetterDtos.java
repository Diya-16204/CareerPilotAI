package com.careerpilot.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public final class CoverLetterDtos {
    private CoverLetterDtos(){}
    public record GenerateRequest(@NotNull UUID jobId,@NotNull UUID resumeVersionId,@Size(max=32) String tone){}
    public record UpdateRequest(@NotBlank @Size(max=12000) String body){}
    public record Response(UUID id,UUID jobId,String title,String body,String generationSource,Instant createdAt,Instant updatedAt){}
}
