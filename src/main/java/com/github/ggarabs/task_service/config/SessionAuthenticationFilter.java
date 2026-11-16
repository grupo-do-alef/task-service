package com.github.ggarabs.task_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private static final Set<String> ROLES = Set.of(
            "WORKER", "EMPLOYER", "AGENCY", "ADMIN");

    private final RestClient userService;

    public SessionAuthenticationFilter(
            @Value("${services.user.url:http://localhost:8080}") String userServiceUrl) {
        this.userService = RestClient.create(userServiceUrl);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!HttpMethod.GET.matches(request.getMethod()))
            return false;
        String path = request.getRequestURI();
        return path.equals("/tasks") || path.startsWith("/tasks/") || path.startsWith("/duvidas/task/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            try {
                SessionUser user = userService.get()
                        .uri("/api/auth/me")
                        .header("Authorization", authorization)
                        .retrieve()
                        .body(SessionUser.class);
                if (user != null && user.id() != null && user.email() != null && ROLES.contains(user.role())) {
                    String role = switch (user.role()) {
                        case "WORKER" -> "CANDIDATE";
                        case "EMPLOYER" -> "CONTRACTOR";
                        case "AGENCY" -> "INTERMEDIARY";
                        case "ADMIN" -> "ADMINISTRATOR";
                        default -> user.role();
                    };
                    var authentication = UsernamePasswordAuthenticationToken.authenticated(
                            user.id(), null, Set.of(new SimpleGrantedAuthority("ROLE_" + role)));
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (RestClientException ignored) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    public record SessionUser(String id, String email, String role, String message) {
    }
}
