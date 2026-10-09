package com.careerpilot.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public final class ProfileDtos {
    private ProfileDtos(){}
    public record UpdateRequest(@NotBlank @Size(min=2,max=80) String name,@Size(max=180) String headline,@Size(max=160) String location,@Size(max=4000) String summary){}
    public record PasswordRequest(@NotBlank @Size(max=72) String currentPassword,@NotBlank @Size(min=10,max=72) String newPassword){}
    public record Response(UUID id,@Email String email,String name,String headline,String location,String summary,Instant updatedAt){}
}
