package com.careerpilot.user;

import com.careerpilot.auth.UserAccount;
import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="user_profiles")
public class UserProfile {
    @Id @UuidGenerator private UUID id;
    @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false,unique=true) private UserAccount user;
    @Column(length=180) private String headline;
    @Column(length=160) private String location;
    @Column(columnDefinition="text") private String summary;
    @UpdateTimestamp @Column(name="updated_at",nullable=false) private Instant updatedAt;
    @Version @Column(nullable=false) private int version;
    protected UserProfile(){}
    public UserProfile(UserAccount user){this.user=user;}
    public String getHeadline(){return headline;}public String getLocation(){return location;}public String getSummary(){return summary;}public Instant getUpdatedAt(){return updatedAt;}
    public void update(String headline,String location,String summary){this.headline=headline;this.location=location;this.summary=summary;}
}
