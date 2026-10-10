package com.careerpilot.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {
    private static final String HEADER = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
    private final byte[] secret;
    private final long expirationSeconds;
    private final ObjectMapper mapper;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-seconds}") long expirationSeconds,
                      ObjectMapper mapper) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        if (this.secret.length < 32) throw new IllegalArgumentException("JWT secret must contain at least 32 bytes.");
        if (expirationSeconds < 1) throw new IllegalArgumentException("JWT expiration must be a positive number of seconds.");
        this.expirationSeconds = expirationSeconds;
        this.mapper = mapper;
    }

    public String issue(UUID subject) {
        long now = Instant.now().getEpochSecond();
        try {
            String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(mapper.writeValueAsBytes(Map.of("sub", subject.toString(), "iat", now, "exp", now + expirationSeconds)));
            String body = HEADER + "." + payload;
            return body + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(sign(body));
        } catch (Exception exception) { throw new IllegalStateException("Token could not be created.", exception); }
    }

    public UUID verify(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3 || !HEADER.equals(parts[0])) return null;
            String body = parts[0] + "." + parts[1];
            byte[] provided = Base64.getUrlDecoder().decode(parts[2]);
            if (!MessageDigest.isEqual(sign(body), provided)) return null;
            Map<?, ?> claims = mapper.readValue(Base64.getUrlDecoder().decode(parts[1]), Map.class);
            if (!(claims.get("exp") instanceof Number exp) || exp.longValue() <= Instant.now().getEpochSecond()) return null;
            return UUID.fromString((String) claims.get("sub"));
        } catch (Exception exception) { return null; }
    }

    private byte[] sign(String content) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        return mac.doFinal(content.getBytes(StandardCharsets.US_ASCII));
    }
    public long getExpirationSeconds() { return expirationSeconds; }
}
