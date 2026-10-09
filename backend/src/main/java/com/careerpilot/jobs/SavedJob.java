package com.careerpilot.jobs;

import com.careerpilot.auth.UserAccount;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="saved_jobs",uniqueConstraints=@UniqueConstraint(columnNames={"user_id","job_id"}))
public class SavedJob {
    @Id @UuidGenerator private UUID id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private UserAccount user;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="job_id",nullable=false) private Job job;
    @CreationTimestamp @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    protected SavedJob() {}
    public SavedJob(UserAccount user,Job job){this.user=user;this.job=job;}
}
