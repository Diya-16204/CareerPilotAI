package com.careerpilot.interview;

import com.careerpilot.auth.UserAccount;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="interview_sessions")
public class InterviewSession {
    @Id @UuidGenerator private UUID id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private UserAccount user;
    @Column(nullable=false,length=160) private String role;
    @Column(nullable=false,length=40) private String topic;
    @Column(nullable=false,length=24) private String difficulty;
    @Column(nullable=false,length=24) private String state="IN_PROGRESS";
    @CreationTimestamp @Column(name="started_at",nullable=false,updatable=false) private Instant startedAt;
    @Column(name="ended_at") private Instant endedAt;
    @Column(name="average_score") private Integer averageScore;
    protected InterviewSession(){}
    public InterviewSession(UserAccount user,String role,String topic,String difficulty){this.user=user;this.role=role;this.topic=topic;this.difficulty=difficulty;}
    public UUID getId(){return id;}public UUID getUserId(){return user.getId();}public String getRole(){return role;}public String getTopic(){return topic;}public String getDifficulty(){return difficulty;}public String getState(){return state;}public Instant getStartedAt(){return startedAt;}public Instant getEndedAt(){return endedAt;}public Integer getAverageScore(){return averageScore;}
    public void recordScore(int average){averageScore=average;}
    public void complete(){state="COMPLETED";endedAt=Instant.now();}
}
