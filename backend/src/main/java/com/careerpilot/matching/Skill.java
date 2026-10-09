package com.careerpilot.matching;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import java.util.UUID;

@Entity @Table(name="skills")
public class Skill {
    @Id @UuidGenerator private UUID id;
    @Column(name="canonical_name",nullable=false,unique=true,length=80) private String canonicalName;
    @Column(length=80) private String category;
    protected Skill(){}
    public Skill(String name){canonicalName=name;}
    public UUID getId(){return id;}public String getCanonicalName(){return canonicalName;}
}
