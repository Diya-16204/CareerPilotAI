package com.careerpilot.interview;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import java.util.UUID;

@Entity @Table(name="interview_questions")
public class InterviewQuestion {
    @Id @UuidGenerator private UUID id;
    @Column(name="seed_key",nullable=false,unique=true,length=100) private String seedKey;
    @Column(nullable=false,length=40) private String topic;
    @Column(nullable=false,length=24) private String difficulty;
    @Column(nullable=false,columnDefinition="text") private String prompt;
    @Column(name="model_answer",nullable=false,columnDefinition="text") private String modelAnswer;
    @Column(nullable=false,columnDefinition="text") private String rubric;
    protected InterviewQuestion(){}
    public UUID getId(){return id;}public String getTopic(){return topic;}public String getDifficulty(){return difficulty;}public String getPrompt(){return prompt;}public String getModelAnswer(){return modelAnswer;}public String getRubric(){return rubric;}
}
