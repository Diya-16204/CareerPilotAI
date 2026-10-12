package com.careerpilot.autoapply;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

public final class ApplicationPrepDtos {
    private ApplicationPrepDtos() {}

    public record QuestionRequest(
            @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{1,64}") String key,
            @NotBlank @Size(max = 300) String label,
            boolean required,
            boolean consequential) {}

    public record CreateRequest(
            @NotNull UUID jobId,
            @NotNull UUID resumeVersionId,
            @Size(max = 1000) String applicationUrl,
            @Size(max = 32) String tone,
            @Size(max = 20) List<@Valid QuestionRequest> questions) {}

    public record AnswerUpdateRequest(@NotBlank @Size(max = 2000) String answer,
                                      boolean confirm,
                                      boolean rememberForSimilarQuestions) {}
    public record DraftUpdateRequest(@NotBlank @Size(max = 12000) String coverLetterDraft) {}
    public record ApprovalRequest(boolean consent) {}

    public record ApplicationFieldRequest(@NotBlank @Size(max = 80) String key,
                                          @NotBlank @Size(max = 300) String label,
                                          @NotBlank @Size(max = 20) String type,
                                          boolean required,
                                          @Size(max = 100) List<@Size(max = 300) String> options,
                                          @Size(max = 12000) String value) {}
    public record DemoSubmissionRequest(boolean consent,
                                        @Size(max = 100) List<@Valid ApplicationFieldRequest> fields) {}
    public record DemoSubmissionResponse(UUID reference, String jobTitle, String company, Instant submittedAt,
                                         boolean alreadySubmitted) {}

    public record AnswerResponse(String key, String label, boolean required, boolean consequential,
                                 String draftAnswer, String answerSource, String draftReason,
                                 String confirmedAnswer, boolean confirmed, String reusableAnswer) {}

    public record SessionResponse(UUID id, UUID jobId, String jobTitle, String company, String location,
                                  UUID resumeVersionId, String resumeFileName, UUID coverLetterId,
                                  String applicationUrl, ApplicationPrepStatus status,
                                  String coverLetterDraft, String coverLetterSource,
                                  List<AnswerResponse> answers, List<String> missingInformation,
                                  List<ApplicationFieldRequest> applicationFields,
                                  boolean approved, Instant approvedAt, String resultMessage,
                                  Instant createdAt, Instant updatedAt) {}
}
