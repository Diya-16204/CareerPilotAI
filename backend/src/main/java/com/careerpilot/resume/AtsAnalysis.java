package com.careerpilot.resume;

import com.careerpilot.auth.UserAccount;
import com.careerpilot.jobs.Job;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="resume_analyses", indexes={
        @Index(name="idx_resume_analysis_owner_created", columnList="user_id,created_at"),
        @Index(name="idx_resume_analysis_version_created", columnList="resume_version_id,created_at")
})
public class AtsAnalysis {
    @Id @UuidGenerator private UUID id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="user_id",nullable=false) private UserAccount user;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="resume_version_id",nullable=false) private ResumeVersion resumeVersion;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="job_id") private Job job;
    @Column(nullable=false) private int score;
    @Column(name="analysis_type",nullable=false,length=24) private String analysisType;
    @Column(name="formula_version",nullable=false,length=40) private String formulaVersion;
    @Column(name="result_json",nullable=false,columnDefinition="longtext") private String resultJson;
    @CreationTimestamp @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;

    protected AtsAnalysis() {}
    public AtsAnalysis(UserAccount user,ResumeVersion resumeVersion,Job job,int score,String analysisType,String formulaVersion,String resultJson){
        this.user=user;this.resumeVersion=resumeVersion;this.job=job;this.score=score;this.analysisType=analysisType;this.formulaVersion=formulaVersion;this.resultJson=resultJson;
    }
    public UUID getId(){return id;} public UUID getResumeVersionId(){return resumeVersion.getId();}
    public UUID getJobId(){return job==null?null:job.getId();} public String getJobTitle(){return job==null?null:job.getTitle();}
    public int getScore(){return score;} public String getAnalysisType(){return analysisType;} public String getFormulaVersion(){return formulaVersion;}
    public String getResultJson(){return resultJson;} public Instant getCreatedAt(){return createdAt;}
}

        //SELECT * FROM resume_analyses
        //WHERE user_id = '550e8400-e29b-41d4-a716-446655440000'
        //ORDER BY created_at DESC;

        //SELECT * FROM resume_analyses
        //WHERE resume_version_id = '9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d'
        //ORDER BY created_at DESC;

//Why Column Order Matters in Composite Indexes
//In database indexing, column order follows the Leftmost Prefix Rule:
//Filtering Column First (user_id / resume_version_id): The database uses the index tree to instantly jump to rows belonging to that specific user or version.
//Sorting Column Second (created_at): Because the index stores items pre-sorted by created_at within each user's subgroup, the database can return paginated results immediately without a Filesort operation.
