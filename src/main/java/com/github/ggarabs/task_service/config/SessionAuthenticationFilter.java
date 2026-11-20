package com.github.ggarabs.task_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.filter.OncePerRequestFilter;

public class SessionAuthenticationFilter extends OncePerRequestFilter {
    private static final Map<String, String> ROLES = Map.of(
            "WORKER", "CANDIDATE", "EMPLOYER", "CONTRACTOR",
            "INTERMEDIARY", "INTERMEDIARY", "ADMIN", "ADMINISTRATOR");
    private final RestClient client;

    public SessionAuthenticationFilter(String userUrl) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        client = RestClient.builder().baseUrl(userUrl).requestFactory(factory).build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            try {
                var user = client.get().uri("/api/auth/me").header("Authorization", authorization)
                        .retrieve().body(SessionUser.class);
                if (user != null && user.id() != null && user.email() != null && ROLES.containsKey(user.role())) {
                    var auth = UsernamePasswordAuthenticationToken.authenticated(
                            UUID.fromString(user.id()).toString(), null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + ROLES.get(user.role()))));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (RestClientException | IllegalArgumentException exception) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }

    public record SessionUser(String id, String email, String role, String message) { }
}
