package com.careerpilot.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.UUID;

import static com.careerpilot.auth.AuthDtos.*;

@Service
public class AuthService {
    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserAccountRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users; this.passwordEncoder = passwordEncoder; this.jwtService = jwtService;
    }

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        String email = normalize(request.email());
        if (users.existsByEmail(email)) throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists.");
        UserAccount user = users.save(new UserAccount(email, passwordEncoder.encode(request.password()), request.name().trim()));
        return tokenResponse(user);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        UserAccount user = users.findByEmail(normalize(request.email())).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect."));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect.");
        return tokenResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(UUID id) {
        UserAccount user = users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign in again to continue."));
        return toResponse(user);
    }

    private TokenResponse tokenResponse(UserAccount user) { return new TokenResponse(toResponse(user), jwtService.issue(user.getId()), "Bearer", jwtService.getExpirationSeconds()); }
    private UserResponse toResponse(UserAccount user) { return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName()); }
    private String normalize(String email) { return email.trim().toLowerCase(Locale.ROOT); }
}
