package com.careerpilot.automation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="automation_runs")
public class AutomationRun {
    @Id @UuidGenerator private UUID id;
    @Column(name="owner_user_id",nullable=false) private UUID ownerUserId;
    @Column(name="portal_key",nullable=false,length=24) private String portalKey;
    @Column(name="job_key",nullable=false,length=80) private String jobKey;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=24) private AutomationRunStatus status;
    @Column(name="idempotency_key",nullable=false) private UUID idempotencyKey;
    @Column(name="request_fingerprint",nullable=false,length=64) private String requestFingerprint;
    @Column(name="confirmation_id") private UUID confirmationId;
    @Column(name="result_message",length=500) private String resultMessage;
    @CreationTimestamp @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    @Column(name="started_at") private Instant startedAt;
    @Column(name="completed_at") private Instant completedAt;

    protected AutomationRun() {}

    public AutomationRun(UUID ownerUserId,String portalKey,String jobKey,UUID idempotencyKey,String requestFingerprint) {
        this.ownerUserId=ownerUserId;
        this.portalKey=portalKey;
        this.jobKey=jobKey;
        this.idempotencyKey=idempotencyKey;
        this.requestFingerprint=requestFingerprint;
        this.status=AutomationRunStatus.PENDING;
    }

    public void start() { status=AutomationRunStatus.RUNNING; startedAt=Instant.now(); resultMessage=null; }
    public void finish(AutomationRunStatus status,UUID confirmationId,String message) {
        if(status==AutomationRunStatus.PENDING||status==AutomationRunStatus.RUNNING) throw new IllegalArgumentException("A completed run needs a terminal status.");
        this.status=status;
        this.confirmationId=confirmationId;
        this.resultMessage=message;
        this.completedAt=Instant.now();
    }

    public UUID getId(){return id;}
    public UUID getOwnerUserId(){return ownerUserId;}
    public String getPortalKey(){return portalKey;}
    public String getJobKey(){return jobKey;}
    public AutomationRunStatus getStatus(){return status;}
    public UUID getIdempotencyKey(){return idempotencyKey;}
    public String getRequestFingerprint(){return requestFingerprint;}
    public UUID getConfirmationId(){return confirmationId;}
    public String getResultMessage(){return resultMessage;}
    public Instant getCreatedAt(){return createdAt;}
    public Instant getStartedAt(){return startedAt;}
    public Instant getCompletedAt(){return completedAt;}
}
