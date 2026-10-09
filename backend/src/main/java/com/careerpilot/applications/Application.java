package com.careerpilot.applications;

import com.careerpilot.auth.UserAccount;
import com.careerpilot.jobs.Job;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="applications")
public class Application {
    @Id @UuidGenerator private UUID id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private UserAccount user;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="job_id") private Job job;
    @Column(nullable=false,length=160) private String title;
    @Column(nullable=false,length=160) private String company;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=24) private ApplicationStatus status;
    @Column(name="applied_at") private Instant appliedAt;
    @Column(name="follow_up_at") private Instant followUpAt;
    @Column(columnDefinition="text") private String notes;
    @CreationTimestamp @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    @UpdateTimestamp @Column(name="updated_at",nullable=false) private Instant updatedAt;
    protected Application() {}
    public Application(UserAccount user,Job job,String title,String company,ApplicationStatus status){this.user=user;this.job=job;this.title=title;this.company=company;this.status=status;if(status==ApplicationStatus.APPLIED)this.appliedAt=Instant.now();}
    public UUID getId(){return id;} public UUID getUserId(){return user.getId();} public String getTitle(){return title;} public String getCompany(){return company;} public ApplicationStatus getStatus(){return status;} public Instant getAppliedAt(){return appliedAt;} public Instant getUpdatedAt(){return updatedAt;}
    public void setStatus(ApplicationStatus status){this.status=status;if(status==ApplicationStatus.APPLIED&&appliedAt==null)appliedAt=Instant.now();}
}
