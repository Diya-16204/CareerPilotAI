package com.careerpilot.user;

import com.careerpilot.auth.UserAccount;
import com.careerpilot.auth.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;
import static com.careerpilot.user.ProfileDtos.*;

@Service
public class ProfileService {
    private final UserAccountRepository users;private final UserProfileRepository profiles;private final PasswordEncoder passwords;
    public ProfileService(UserAccountRepository users,UserProfileRepository profiles,PasswordEncoder passwords){this.users=users;this.profiles=profiles;this.passwords=passwords;}
    @Transactional(readOnly=true) public Response get(UUID id){UserAccount user=user(id);return dto(user,profiles.findByUserId(id).orElse(null));}
    @Transactional public Response update(UUID id,UpdateRequest request){UserAccount user=user(id);UserProfile profile=profiles.findByUserId(id).orElseGet(()->profiles.save(new UserProfile(user)));user.setDisplayName(request.name().trim());profile.update(blank(request.headline()),blank(request.location()),blank(request.summary()));users.saveAndFlush(user);profiles.saveAndFlush(profile);return dto(user,profile);}
    @Transactional public void changePassword(UUID id,PasswordRequest request){UserAccount user=user(id);if(!passwords.matches(request.currentPassword(),user.getPasswordHash()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Current password is incorrect.");user.setPasswordHash(passwords.encode(request.newPassword()));users.saveAndFlush(user);}
    private UserAccount user(UUID id){return users.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Sign in again to continue."));}
    private Response dto(UserAccount user,UserProfile profile){return new Response(user.getId(),user.getEmail(),user.getDisplayName(),profile==null?null:profile.getHeadline(),profile==null?null:profile.getLocation(),profile==null?null:profile.getSummary(),profile==null?null:profile.getUpdatedAt());}
    private String blank(String value){return value==null||value.isBlank()?null:value.trim();}
}
