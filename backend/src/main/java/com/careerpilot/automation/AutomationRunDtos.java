package com.careerpilot.automation;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public final class AutomationRunDtos {
    private AutomationRunDtos() {}

    public record StartRequest(
            @NotBlank @Pattern(regexp="PORTAL_A|PORTAL_B|PORTAL_C") String portalKey,
            @NotBlank @Size(max=80) String jobKey,
            @NotNull UUID resumeVersionId,
            @Size(max=300) String education,
            @Size(max=2000) String experience,
            @Size(max=4000) String coverLetter,
            @NotNull Map<@NotBlank @Size(max=300) String,@NotBlank @Size(max=2000) String> answers,
            @AssertTrue Boolean consent) {}

    public record RunResponse(UUID id,String portalKey,String jobKey,AutomationRunStatus status,
                              UUID confirmationId,String message,Instant createdAt,Instant startedAt,Instant completedAt) {}
}
