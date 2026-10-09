package com.careerpilot.jobs;

import com.careerpilot.auth.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Entity @Table(name = "jobs")
public class Job {
    @Id @UuidGenerator private UUID id;
    @Column(nullable=false,length=160) private String title;
    @Column(nullable=false,length=160) private String company;
    @Column(nullable=false,length=160) private String location;
    @Column(name="employment_type",nullable=false,length=40) private String employmentType;
    @Column(nullable=false,columnDefinition="text") private String description;
    @Column(name="skills_text",nullable=false,columnDefinition="text") private String skillsText;
    @Column(nullable=false,length=120) private String source;
    @ManyToOne(fetch=jakarta.persistence.FetchType.LAZY) @jakarta.persistence.JoinColumn(name="owner_user_id") private UserAccount owner;
    @Column(name="is_sample",nullable=false) private boolean sample;
    @CreationTimestamp @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;

    protected Job() {}
    public Job(UserAccount owner,String title,String company,String location,String employmentType,String description,List<String> skills){this.owner=owner;this.title=title;this.company=company;this.location=location;this.employmentType=employmentType;this.description=description;this.skillsText=String.join(",",skills);this.source="User-provided";this.sample=false;}
    public UUID getId(){return id;} public String getTitle(){return title;} public String getCompany(){return company;}
    public String getLocation(){return location;} public String getEmploymentType(){return employmentType;}
    public String getDescription(){return description;} public String getSource(){return source;} public boolean isSample(){return sample;}
    public UUID getOwnerId(){return owner==null?null:owner.getId();}
    public List<String> getSkills(){return Arrays.stream(skillsText.split(",")).map(String::trim).filter(s->!s.isEmpty()).toList();}
}
