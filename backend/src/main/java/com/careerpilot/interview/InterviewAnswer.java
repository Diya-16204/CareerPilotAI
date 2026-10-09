package com.careerpilot.interview;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="interview_answers",uniqueConstraints={@UniqueConstraint(columnNames={"session_id","ordinal"}),@UniqueConstraint(columnNames={"session_id","question_id"})})
public class InterviewAnswer {
    @Id @UuidGenerator private UUID id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="session_id",nullable=false) private InterviewSession session;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="question_id",nullable=false) private InterviewQuestion question;
    @Column(nullable=false) private int ordinal;
    @Column(nullable=false,columnDefinition="text") private String answer;
    @Column(name="content_coverage_score",nullable=false) private int contentCoverageScore;
    @Column(nullable=false,columnDefinition="text") private String feedback;
    @CreationTimestamp @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    protected InterviewAnswer(){}
    public InterviewAnswer(InterviewSession session,InterviewQuestion question,int ordinal,String answer,int score,String feedback){this.session=session;this.question=question;this.ordinal=ordinal;this.answer=answer;contentCoverageScore=score;this.feedback=feedback;}
    public UUID getId(){return id;}public UUID getQuestionId(){return question.getId();}public String getQuestionPrompt(){return question.getPrompt();}public String getAnswer(){return answer;}public int getContentCoverageScore(){return contentCoverageScore;}public String getFeedback(){return feedback;}public Instant getCreatedAt(){return createdAt;}public int getOrdinal(){return ordinal;}public String getModelAnswer(){return question.getModelAnswer();}
}
