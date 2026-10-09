package com.careerpilot.matching;

import com.careerpilot.auth.UserAccount;
import com.careerpilot.jobs.Job;
import com.careerpilot.resume.ResumeVersion;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity @Table(name="resume_job_matches")
public class ResumeJobMatch {
    @Id @UuidGenerator private UUID id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private UserAccount user;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="resume_version_id",nullable=false) private ResumeVersion resumeVersion;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="job_id",nullable=false) private Job job;
    @Column(nullable=false) private int score;
    @Column(name="formula_version",nullable=false,length=32) private String formulaVersion;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name="result_json",nullable=false,columnDefinition="jsonb") private Map<String,Object> resultJson;
    @CreationTimestamp @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    protected ResumeJobMatch(){}
    public ResumeJobMatch(UserAccount user,ResumeVersion version,Job job,int score,String formulaVersion,Map<String,Object> result){this.user=user;resumeVersion=version;this.job=job;this.score=score;this.formulaVersion=formulaVersion;resultJson=result;}
    public UUID getId(){return id;}public int getScore(){return score;}public String getFormulaVersion(){return formulaVersion;}public Instant getCreatedAt(){return createdAt;}public UUID getJobId(){return job.getId();}public UUID getResumeVersionId(){return resumeVersion.getId();}
}
