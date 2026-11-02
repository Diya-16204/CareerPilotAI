package com.careerpilot.ai;

import com.careerpilot.auth.UserAccount;
import com.careerpilot.jobs.Job;
import com.careerpilot.resume.ResumeVersion;
import jakarta.persistence.*;
/*Import all publicly accessible classes from jakarta.persistence.
These annotations include things such as:
@Entity
@Table
@Id
@Column
@ManyToOne
@JoinColumn

They help Java/Spring Boot map your class to a database table.

This is called ORM.
ORM = Object Relational Mapping
It connects:
Java Object
Database Table
*/
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="cover_letters")
public class CoverLetter {
    @Id @UuidGenerator private UUID id;
    /*
    connecting column
    @ManyToOne
    hota hai
    */
    /*
    @ManyToOne → Many cover letters can belong to one user.
    fetch=FetchType.LAZY → User data is loaded only when it is actually needed.
    optional=false → A cover letter must have a user; the relationship is mandatory.
    @JoinColumn(name="user_id") → Creates/uses the user_id foreign-key column to connect the cover letter with its user.
    nullable=false → The user_id column cannot be NULL in the database.
    private UserAccount user; → Stores the UserAccount object associated with this cover letter.
    */
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private UserAccount user;
    /* Common/connecting column hota hai JoinColumn. */
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="job_id",nullable=false) private Job job;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="resume_version_id",nullable=false) private ResumeVersion resumeVersion;
    @Column(nullable=false,length=180) private String title;
    /*
    A normal database string column might have a limited size.But a cover letter can be long.
    Therefore:
        columnDefinition="text"
    */
    @Column(nullable=false,columnDefinition="text") private String body;
    /*
    OPENAI
    LOCAL_TEMPLATE
    UNCONFIGURED
    */
    @Column(name="generation_source",nullable=false,length=32) private String generationSource;
    @CreationTimestamp @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    @UpdateTimestamp @Column(name="updated_at",nullable=false) private Instant updatedAt;
    protected CoverLetter(){}
    public CoverLetter(UserAccount user,Job job,ResumeVersion version,String title,String body,String source){this.user=user;this.job=job;resumeVersion=version;this.title=title;this.body=body;generationSource=source;}
    public UUID getId(){return id;}public UUID getUserId(){return user.getId();}public UUID getJobId(){return job.getId();}public String getTitle(){return title;}public String getBody(){return body;}public String getGenerationSource(){return generationSource;}public Instant getCreatedAt(){return createdAt;}public Instant getUpdatedAt(){return updatedAt;}
    public void update(String body){this.body=body;}
}
