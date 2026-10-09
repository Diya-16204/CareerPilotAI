package com.careerpilot.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {
    private final JwtService tokens = new JwtService("test-secret-that-is-at-least-32-bytes-long",900,new ObjectMapper());

    @Test void verifiesAnIssuedTokenAndReturnsItsSubject(){
        UUID userId=UUID.randomUUID();
        assertThat(tokens.verify(tokens.issue(userId))).isEqualTo(userId);
    }

    @Test void rejectsTamperedAndMalformedTokens(){
        String token=tokens.issue(UUID.randomUUID());
        String[] parts=token.split("\\.");
        parts[1]=(parts[1].startsWith("A")?"B":"A")+parts[1].substring(1);
        assertThat(tokens.verify(String.join(".",parts))).isNull();
        assertThat(tokens.verify("not-a-jwt")).isNull();
    }
}
