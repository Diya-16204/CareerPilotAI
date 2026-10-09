package com.careerpilot.portal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import java.time.Instant;
import java.util.UUID;
import java.util.Arrays;

@Entity @Table(name="simulated_portal_submissions")
public class SimulatedPortalSubmission {
    @Id @UuidGenerator private UUID id;
    @Column(name="owner_user_id",nullable=false) private UUID ownerUserId;
    @Column(name="portal_key",nullable=false,length=24) private String portalKey;
    @Column(name="job_key",nullable=false,length=80) private String jobKey;
    @Column(name="job_title",nullable=false,length=160) private String jobTitle;
    @Column(name="applicant_name",nullable=false,length=120) private String applicantName;
    @Column(name="applicant_email",nullable=false,length=254) private String applicantEmail;
    @Column(length=300) private String education;
    @Column(columnDefinition="text") private String experience;
    @Column(name="cover_letter",columnDefinition="text") private String coverLetter;
    @Column(name="resume_file_name",length=255) private String resumeFileName;
    @Column(name="resume_bytes",columnDefinition="bytea") private byte[] resumeBytes;
    @Column(name="answers_json",nullable=false,columnDefinition="text") private String answersJson;
    @Column(name="idempotency_key",nullable=false) private UUID idempotencyKey;
    @Column(name="confirmation_id",nullable=false,unique=true) private UUID confirmationId;
    @CreationTimestamp @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;

    protected SimulatedPortalSubmission() {}
    public SimulatedPortalSubmission(UUID ownerUserId,String portalKey,String jobKey,String jobTitle,String applicantName,String applicantEmail,String education,String experience,String coverLetter,String resumeFileName,byte[] resumeBytes,String answersJson,UUID idempotencyKey) {
        this.ownerUserId=ownerUserId;this.portalKey=portalKey;this.jobKey=jobKey;this.jobTitle=jobTitle;this.applicantName=applicantName;this.applicantEmail=applicantEmail;this.education=education;this.experience=experience;this.coverLetter=coverLetter;this.resumeFileName=resumeFileName;this.resumeBytes=resumeBytes;this.answersJson=answersJson;this.idempotencyKey=idempotencyKey;this.confirmationId=UUID.randomUUID();
    }
    public UUID getConfirmationId(){return confirmationId;} public String getPortalKey(){return portalKey;} public String getJobKey(){return jobKey;} public String getJobTitle(){return jobTitle;} public Instant getCreatedAt(){return createdAt;}
    public boolean sameSubmission(String applicantEmail,String applicantName,String education,String experience,String coverLetter,String jobKey,byte[] resumeBytes,String answersJson){return this.applicantEmail.equalsIgnoreCase(applicantEmail)&&this.applicantName.equals(applicantName.trim())&&this.jobKey.equals(jobKey)&&same(this.education,education)&&same(this.experience,experience)&&same(this.coverLetter,coverLetter)&&Arrays.equals(this.resumeBytes,resumeBytes)&&this.answersJson.equals(answersJson);}
    private boolean same(String saved,String requested){return saved==null?(requested==null||requested.isBlank()):saved.equals(requested.trim());}
}
