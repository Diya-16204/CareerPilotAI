package com.careerpilot.resume;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="resume_versions",uniqueConstraints=@UniqueConstraint(columnNames={"resume_id","version_number"}))
public class ResumeVersion {
    @Id @UuidGenerator private UUID id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="resume_id",nullable=false) private Resume resume;
    @Column(name="version_number",nullable=false) private int versionNumber;
    @Column(name="storage_key",nullable=false,unique=true,length=80) private String storageKey;
    @Column(name="original_file_name",nullable=false,length=255) private String originalFileName;
    @Column(name="content_type",nullable=false,length=100) private String contentType;
    @Column(name="checksum_sha256",nullable=false,length=64) private String checksumSha256;
    @Column(name="extracted_text",nullable=false,columnDefinition="text") private String extractedText;
    @Column(name="extracted_email",length=254) private String extractedEmail;
    @Column(name="extracted_phone",length=48) private String extractedPhone;
    @Column(name="skills_text",nullable=false,columnDefinition="text") private String skillsText;
    @Column(nullable=false) private boolean reviewed;
    @CreationTimestamp @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    protected ResumeVersion() {}
    public ResumeVersion(Resume resume,int version,String key,String filename,String type,String checksum,String text,String email,String phone,String skills){
        this.resume=resume;versionNumber=version;storageKey=key;originalFileName=filename;contentType=type;checksumSha256=checksum;extractedText=text;extractedEmail=email;extractedPhone=phone;skillsText=skills;reviewed=false;
    }
    public UUID getId(){return id;} public UUID getResumeId(){return resume.getId();} public int getVersionNumber(){return versionNumber;} public String getStorageKey(){return storageKey;} public String getOriginalFileName(){return originalFileName;} public String getContentType(){return contentType;} public String getExtractedText(){return extractedText;} public String getExtractedEmail(){return extractedEmail;} public String getExtractedPhone(){return extractedPhone;} public String getSkillsText(){return skillsText;} public boolean isReviewed(){return reviewed;} public Instant getCreatedAt(){return createdAt;}
    public void review(String email,String phone,String skills){extractedEmail=email;extractedPhone=phone;skillsText=skills;reviewed=true;}
}
