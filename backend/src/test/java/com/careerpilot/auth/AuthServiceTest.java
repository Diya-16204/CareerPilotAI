package com.careerpilot.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private final UserAccountRepository users=mock(UserAccountRepository.class);
    private final PasswordEncoder encoder=mock(PasswordEncoder.class);
    private final JwtService tokens=new JwtService("test-secret-that-is-at-least-32-bytes-long",900,new com.fasterxml.jackson.databind.ObjectMapper());
    private final AuthService service=new AuthService(users,encoder,tokens);

    @Test void registrationNormalizesEmailHashesPasswordAndReturnsShortLivedToken(){
        UUID id=UUID.randomUUID();
        when(users.existsByEmail("person@example.com")).thenReturn(false);
        when(encoder.encode("a-secure-password")).thenReturn("bcrypt-hash");
        when(users.save(any())).thenAnswer(call->{UserAccount user=call.getArgument(0);ReflectionTestUtils.setField(user,"id",id);return user;});
        var result=service.register(new AuthDtos.RegisterRequest(" Sample User "," Person@Example.com ","a-secure-password"));
        assertThat(result.user().id()).isEqualTo(id);
        assertThat(result.user().email()).isEqualTo("person@example.com");
        assertThat(result.user().name()).isEqualTo("Sample User");
        assertThat(tokens.verify(result.accessToken())).isEqualTo(id);
        verify(encoder).encode("a-secure-password");
    }

    @Test void duplicateEmailIsRejected(){
        when(users.existsByEmail("person@example.com")).thenReturn(true);
        assertThatThrownBy(()->service.register(new AuthDtos.RegisterRequest("Sample User","person@example.com","a-secure-password")))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("409 CONFLICT");
        verify(users,never()).save(any());
    }

    @Test void loginDoesNotRevealWhetherEmailExists(){
        when(users.findByEmail("person@example.com")).thenReturn(Optional.empty());
        assertThatThrownBy(()->service.login(new AuthDtos.LoginRequest("person@example.com","wrong-password")))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("Email or password is incorrect.");
    }
}
