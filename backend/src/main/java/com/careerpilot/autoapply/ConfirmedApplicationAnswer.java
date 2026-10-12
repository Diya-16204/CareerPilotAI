package com.careerpilot.autoapply;

import com.careerpilot.auth.UserAccount;
import com.careerpilot.jobs.Job;
import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;
import java.time.Duration;
import java.util.UUID;

@Entity
@Table(name = "confirmed_application_answers", uniqueConstraints =
        @UniqueConstraint(name = "uq_confirmed_answer_owner_job_question", columnNames = {"owner_user_id", "job_id", "question_hash"}))
public class ConfirmedApplicationAnswer {
    @Id @org.hibernate.annotations.UuidGenerator private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "owner_user_id", nullable = false) private UserAccount owner;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "job_id", nullable = false) private Job job;
    @Column(name = "question_hash", nullable = false, columnDefinition = "char(64)", length = 64) private String questionHash;
    @Column(name = "question_text", nullable = false, length = 300) private String questionText;
    @Column(name = "answer_text", nullable = false, length = 4000) private String answerText;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    protected ConfirmedApplicationAnswer() {}
    public ConfirmedApplicationAnswer(UserAccount owner, Job job, String questionHash, String questionText, String answerText) {
        this.owner = owner; this.job = job; this.questionHash = questionHash; this.questionText = questionText; this.answerText = answerText;
    }
    public void update(String questionText, String answerText) { this.questionText = questionText; this.answerText = answerText; }
    public UUID getOwnerId() { return owner.getId(); }
    public String getQuestionHash() { return questionHash; }
    public String getQuestionText() { return questionText; }
    public String getAnswerText() { return answerText; }
    public boolean isRecent() { return updatedAt != null && updatedAt.isAfter(Instant.now().minus(Duration.ofDays(90))); }
}
