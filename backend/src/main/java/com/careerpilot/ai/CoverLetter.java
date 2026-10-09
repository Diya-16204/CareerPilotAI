package com.careerpilot.ai;

import com.careerpilot.auth.UserAccount;
import com.careerpilot.jobs.Job;
import com.careerpilot.resume.ResumeVersion;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="cover_letters")
public class CoverLetter {
    @Id @UuidGenerator private UUID id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private UserAccount user;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="job_id",nullable=false) private Job job;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="resume_version_id",nullable=false) private ResumeVersion resumeVersion;
    @Column(nullable=false,length=180) private String title;
    @Column(nullable=false,columnDefinition="text") private String body;
    @Column(name="generation_source",nullable=false,length=32) private String generationSource;
    @CreationTimestamp @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    @UpdateTimestamp @Column(name="updated_at",nullable=false) private Instant updatedAt;
    protected CoverLetter(){}
    public CoverLetter(UserAccount user,Job job,ResumeVersion version,String title,String body,String source){this.user=user;this.job=job;resumeVersion=version;this.title=title;this.body=body;generationSource=source;}
    public UUID getId(){return id;}public UUID getUserId(){return user.getId();}public UUID getJobId(){return job.getId();}public String getTitle(){return title;}public String getBody(){return body;}public String getGenerationSource(){return generationSource;}public Instant getCreatedAt(){return createdAt;}public Instant getUpdatedAt(){return updatedAt;}
    public void update(String body){this.body=body;}
}
