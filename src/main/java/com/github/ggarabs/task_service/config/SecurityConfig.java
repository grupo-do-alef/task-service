package com.github.ggarabs.task_service.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
            @Value("${services.user.url:http://localhost:8080}") String userUrl,
            @Value("${app.cors.allowed-origin:http://localhost:3000}") String origin) throws Exception {
        var cors = new CorsConfiguration();
        cors.setAllowedOrigins(List.of(origin));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Content-Type", "Authorization"));
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return http.cors(config -> config.configurationSource(source))
                .csrf(config -> config.disable())
                .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(config -> config.authenticationEntryPoint((request, response, exception) -> response.sendError(403)))
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.GET, "/tasks", "/tasks/**", "/duvidas/task/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/tasks").hasAnyRole("CONTRACTOR", "INTERMEDIARY", "ADMINISTRATOR")
                    .anyRequest().authenticated())
                .addFilterBefore(new SessionAuthenticationFilter(userUrl), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
