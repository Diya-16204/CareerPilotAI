package com.careerpilot.autoapply;

import com.careerpilot.auth.UserAccount;
import com.careerpilot.jobs.Job;
import com.careerpilot.resume.ResumeVersion;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "application_prep_sessions", indexes = {
        @Index(name = "idx_prep_owner_created", columnList = "owner_user_id,created_at"),
        @Index(name = "idx_prep_status_updated", columnList = "status,updated_at")
})
public class ApplicationPrepSession {
    @Id @UuidGenerator private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "owner_user_id", nullable = false) private UserAccount owner;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "job_id", nullable = false) private Job job;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "resume_version_id", nullable = false) private ResumeVersion resumeVersion;
    @Column(name = "cover_letter_id") private UUID coverLetterId;
    @Column(name = "application_url", length = 1000) private String applicationUrl;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private ApplicationPrepStatus status;
    @Column(name = "cover_letter_draft", columnDefinition = "longtext") private String coverLetterDraft;
    @Column(name = "cover_letter_source", length = 32) private String coverLetterSource;
    @Column(name = "answers_json", nullable = false, columnDefinition = "longtext") private String answersJson;
    @Column(name = "missing_information_json", nullable = false, columnDefinition = "longtext") private String missingInformationJson;
    @Column(name = "application_fields_json", nullable = false, columnDefinition = "longtext") private String applicationFieldsJson;
    @Column(name = "approved_content_hash", columnDefinition = "char(64)", length = 64) private String approvedContentHash;
    @Column(name = "approved_at") private Instant approvedAt;
    @Column(name = "result_message", length = 1000) private String resultMessage;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Version @Column(nullable = false) private int version;

    protected ApplicationPrepSession() {}

    public ApplicationPrepSession(UserAccount owner, Job job, ResumeVersion resumeVersion, String applicationUrl) {
        this.owner = owner;
        this.job = job;
        this.resumeVersion = resumeVersion;
        this.applicationUrl = applicationUrl;
        this.status = ApplicationPrepStatus.PREPARING;
        this.answersJson = "[]";
        this.missingInformationJson = "[]";
        this.applicationFieldsJson = "[]";
    }

    public void prepared(UUID letterId, String body, String source, String answers, String missing, ApplicationPrepStatus nextStatus, String message) {
        this.coverLetterId = letterId;
        this.coverLetterDraft = body;
        this.coverLetterSource = source;
        this.answersJson = answers;
        this.missingInformationJson = missing;
        this.status = nextStatus;
        this.resultMessage = message;
        invalidateApproval();
    }

    public void updateDraft(String body, String missing, ApplicationPrepStatus nextStatus) {
        this.coverLetterDraft = body;
        this.missingInformationJson = missing;
        this.status = nextStatus;
        this.resultMessage = nextStatus == ApplicationPrepStatus.READY_FOR_REVIEW ? "Drafts are ready for your review." : "Some required information still needs your confirmation.";
        invalidateApproval();
    }

    public void updateAnswers(String answers, String missing, ApplicationPrepStatus nextStatus) {
        this.answersJson = answers;
        this.missingInformationJson = missing;
        this.status = nextStatus;
        this.resultMessage = nextStatus == ApplicationPrepStatus.READY_FOR_REVIEW ? "Drafts are ready for your review." : "Some required information still needs your confirmation.";
        invalidateApproval();
    }

    public void updateApplicationFields(String fields) {
        this.applicationFieldsJson = fields;
        invalidateApproval();
        if (status == ApplicationPrepStatus.APPROVED) {
            status = ApplicationPrepStatus.READY_FOR_REVIEW;
            resultMessage = "Application fields changed; review and approve the updated draft again.";
        }
    }

    public void approve(String contentHash) {
        this.approvedContentHash = contentHash;
        this.approvedAt = Instant.now();
        this.status = ApplicationPrepStatus.APPROVED;
        this.resultMessage = "You approved this exact draft. No application has been submitted.";
    }

    public void requireManualAction(String message) {
        this.status = ApplicationPrepStatus.REQUIRES_MANUAL_ACTION;
        this.resultMessage = message;
    }

    public void submitted(String message) {
        this.status = ApplicationPrepStatus.SUBMITTED;
        this.resultMessage = message;
    }

    public void fail(String message) {
        this.status = ApplicationPrepStatus.FAILED;
        this.resultMessage = message;
    }

    private void invalidateApproval() {
        this.approvedContentHash = null;
        this.approvedAt = null;
    }

    public UUID getId() { return id; }
    public UUID getOwnerId() { return owner.getId(); }
    public UUID getJobId() { return job.getId(); }
    public String getJobTitle() { return job.getTitle(); }
    public String getCompany() { return job.getCompany(); }
    public String getLocation() { return job.getLocation(); }
    public String getJobDescription() { return job.getDescription(); }
    public UUID getResumeVersionId() { return resumeVersion.getId(); }
    public String getResumeFileName() { return resumeVersion.getOriginalFileName(); }
    public UUID getCoverLetterId() { return coverLetterId; }
    public String getApplicationUrl() { return applicationUrl; }
    public ApplicationPrepStatus getStatus() { return status; }
    public String getCoverLetterDraft() { return coverLetterDraft; }
    public String getCoverLetterSource() { return coverLetterSource; }
    public String getAnswersJson() { return answersJson; }
    public String getMissingInformationJson() { return missingInformationJson; }
    public String getApplicationFieldsJson() { return applicationFieldsJson; }
    public String getApprovedContentHash() { return approvedContentHash; }
    public Instant getApprovedAt() { return approvedAt; }
    public String getResultMessage() { return resultMessage; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
