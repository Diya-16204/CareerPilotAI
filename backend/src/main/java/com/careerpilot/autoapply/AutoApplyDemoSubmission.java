package com.careerpilot.autoapply;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auto_apply_demo_submissions", uniqueConstraints =
        @UniqueConstraint(name = "uq_auto_apply_demo_session", columnNames = "session_id"), indexes =
        @Index(name = "idx_auto_apply_demo_owner_submitted", columnList = "owner_user_id,submitted_at"))
public class AutoApplyDemoSubmission {
    @Id @UuidGenerator private UUID id;
    @Column(name = "owner_user_id", nullable = false, columnDefinition = "binary(16)") private UUID ownerId;
    @Column(name = "session_id", nullable = false, columnDefinition = "binary(16)") private UUID sessionId;
    @Column(name = "job_title", nullable = false, length = 200) private String jobTitle;
    @Column(nullable = false, length = 200) private String company;
    @Column(name = "resume_file_name", nullable = false, length = 255) private String resumeFileName;
    @Column(name = "cover_letter", nullable = false, columnDefinition = "longtext") private String coverLetter;
    @Column(name = "application_url", length = 1000) private String applicationUrl;
    @Column(name = "application_fields_json", nullable = false, columnDefinition = "longtext") private String fieldsJson;
    @CreationTimestamp @Column(name = "submitted_at", nullable = false, updatable = false) private Instant submittedAt;

    protected AutoApplyDemoSubmission() {}

    public AutoApplyDemoSubmission(UUID ownerId, UUID sessionId, String jobTitle, String company,
                                   String resumeFileName, String coverLetter, String applicationUrl, String fieldsJson) {
        this.ownerId = ownerId;
        this.sessionId = sessionId;
        this.jobTitle = jobTitle;
        this.company = company;
        this.resumeFileName = resumeFileName;
        this.coverLetter = coverLetter;
        this.applicationUrl = applicationUrl;
        this.fieldsJson = fieldsJson;
    }

    public UUID getId() { return id; }
    public UUID getOwnerId() { return ownerId; }
    public UUID getSessionId() { return sessionId; }
    public String getJobTitle() { return jobTitle; }
    public String getCompany() { return company; }
    public String getResumeFileName() { return resumeFileName; }
    public Instant getSubmittedAt() { return submittedAt; }
}
