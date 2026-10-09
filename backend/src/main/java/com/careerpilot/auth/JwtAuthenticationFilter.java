package com.careerpilot.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.careerpilot.common.ApiErrorWriter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final ApiErrorWriter errorWriter;
    public JwtAuthenticationFilter(JwtService jwtService, ApiErrorWriter errorWriter) { this.jwtService = jwtService; this.errorWriter=errorWriter; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            UUID userId = jwtService.verify(authorization.substring(7));
            if (userId == null) { errorWriter.write(request,response,HttpServletResponse.SC_UNAUTHORIZED,"INVALID_TOKEN","This session has expired. Sign in again."); return; }
            var authentication = new UsernamePasswordAuthenticationToken(userId, null, AuthorityUtils.createAuthorityList("ROLE_USER"));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request, response);
    }
}
