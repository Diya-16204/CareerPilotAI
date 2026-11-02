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
    /*
     * JPA annotations describe how this Java entity maps to the cover_letters table.
     * Hibernate uses this object-relational mapping to store and load cover letters.
     */
    @Id @UuidGenerator private UUID id;
    /* Many cover letters can belong to one user; this required relationship uses user_id. */
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private UserAccount user;
    /* Each cover letter belongs to the job it was prepared for. */
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="job_id",nullable=false) private Job job;
    /* Links the letter to the resume version used to prepare it. */
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="resume_version_id",nullable=false) private ResumeVersion resumeVersion;
    @Column(nullable=false,length=180) private String title;
    /* Letters can be longer than a typical VARCHAR, so this column uses TEXT. */
    @Column(nullable=false,columnDefinition="text") private String body;
    /* Records the generator, for example LOCAL_TEMPLATE, OPENAI, or UNCONFIGURED. */
    @Column(name="generation_source",nullable=false,length=32) private String generationSource;
    @CreationTimestamp @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    @UpdateTimestamp @Column(name="updated_at",nullable=false) private Instant updatedAt;
    protected CoverLetter(){}
    public CoverLetter(UserAccount user,Job job,ResumeVersion version,String title,String body,String source){this.user=user;this.job=job;resumeVersion=version;this.title=title;this.body=body;generationSource=source;}
    public UUID getId(){return id;}public UUID getUserId(){return user.getId();}public UUID getJobId(){return job.getId();}public String getTitle(){return title;}public String getBody(){return body;}public String getGenerationSource(){return generationSource;}public Instant getCreatedAt(){return createdAt;}public Instant getUpdatedAt(){return updatedAt;}
    /* JPA refreshes updated_at when the edited draft is saved. */
    public void update(String body){this.body=body;}
}
