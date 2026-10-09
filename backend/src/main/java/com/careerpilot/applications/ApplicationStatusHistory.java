package com.careerpilot.applications;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="application_status_history")
public class ApplicationStatusHistory {
    @Id @UuidGenerator private UUID id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="application_id",nullable=false) private Application application;
    @Enumerated(EnumType.STRING) @Column(name="from_status",length=24) private ApplicationStatus fromStatus;
    @Enumerated(EnumType.STRING) @Column(name="to_status",nullable=false,length=24) private ApplicationStatus toStatus;
    @Column(length=500) private String note;
    @CreationTimestamp @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    protected ApplicationStatusHistory() {}
    public ApplicationStatusHistory(Application app,ApplicationStatus from,ApplicationStatus to,String note){application=app;fromStatus=from;toStatus=to;this.note=note;}
}
