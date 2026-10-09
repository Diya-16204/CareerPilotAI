package com.careerpilot.resume;

import com.careerpilot.auth.UserAccount;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="resumes")
public class Resume {
    @Id @UuidGenerator private UUID id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private UserAccount user;
    @Column(nullable=false,length=160) private String title;
    @Column(name="active_version_number",nullable=false) private int activeVersionNumber=1;
    @Version @Column(nullable=false) private int version;
    @CreationTimestamp @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    @UpdateTimestamp @Column(name="updated_at",nullable=false) private Instant updatedAt;
    protected Resume() {}
    public Resume(UserAccount user,String title){this.user=user;this.title=title;}
    public UUID getId(){return id;} public UUID getUserId(){return user.getId();} public String getTitle(){return title;} public int getActiveVersionNumber(){return activeVersionNumber;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
    public void setActiveVersionNumber(int number){activeVersionNumber=number;}
}
