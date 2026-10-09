package com.careerpilot.matching;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import java.util.UUID;

@Entity @Table(name="match_skills",uniqueConstraints=@UniqueConstraint(columnNames={"match_id","skill_id"}))
public class MatchSkill {
    @Id @UuidGenerator private UUID id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="match_id",nullable=false) private ResumeJobMatch match;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="skill_id",nullable=false) private Skill skill;
    @Column(nullable=false,length=24) private String classification;
    @Column(name="resume_evidence",columnDefinition="text") private String resumeEvidence;
    @Column(name="job_evidence",columnDefinition="text") private String jobEvidence;
    protected MatchSkill(){}
    public MatchSkill(ResumeJobMatch match,Skill skill,String classification,String resumeEvidence,String jobEvidence){this.match=match;this.skill=skill;this.classification=classification;this.resumeEvidence=resumeEvidence;this.jobEvidence=jobEvidence;}
}
