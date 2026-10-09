package com.careerpilot.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public final class AuthDtos {
    private AuthDtos() {}
    public record RegisterRequest(@NotBlank @Size(min=2,max=80) String name, @NotBlank @Email @Size(max=254) String email,
                                  @NotBlank @Size(min=10,max=72) String password) {}
    public record LoginRequest(@NotBlank @Email @Size(max=254) String email, @NotBlank @Size(max=72) String password) {}
    public record UserResponse(UUID id, String email, String name) {}
    public record TokenResponse(UserResponse user, String accessToken, String tokenType, long expiresIn) {}
}
