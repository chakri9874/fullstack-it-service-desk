package com.itservicedesk.backend.config;

import com.itservicedesk.backend.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(
                                        (request, response, authException) -> {
                                            response.setStatus(
                                                    HttpServletResponse.SC_UNAUTHORIZED
                                            );
                                            response.setContentType("application/json");
                                            response.getWriter().write(
                                                    "{\"message\":\"Authentication is required.\"}"
                                            );
                                        }
                                )
                                .accessDeniedHandler(
                                        (request, response, accessDeniedException) -> {
                                            response.setStatus(
                                                    HttpServletResponse.SC_FORBIDDEN
                                            );
                                            response.setContentType("application/json");
                                            response.getWriter().write(
                                                    "{\"message\":\"You do not have permission to access this resource.\"}"
                                            );
                                        }
                                )
                )
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                                .requestMatchers("/api/auth/**").permitAll()

                                .requestMatchers("/api/dashboard/**")
                                .hasRole("ADMIN")

                                .requestMatchers("/api/users/**")
                                .hasRole("ADMIN")

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/tickets/escalation-queue"
                                )
                                .hasRole("ADMIN")

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/tickets/support-stats"
                                )
                                .hasRole("IT_SUPPORT")

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/tickets/support-queue"
                                )
                                .hasRole("IT_SUPPORT")

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/tickets/created-by/**"
                                )
                                .hasAnyRole("EMPLOYEE", "ADMIN")

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/tickets/assigned-to/**"
                                )
                                .hasAnyRole("IT_SUPPORT", "ADMIN")

                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/tickets"
                                )
                                .hasAnyRole("EMPLOYEE", "ADMIN")

                                .requestMatchers(
                                        HttpMethod.PATCH,
                                        "/api/tickets/*/accept"
                                )
                                .hasRole("IT_SUPPORT")

                                .requestMatchers(
                                        HttpMethod.PATCH,
                                        "/api/tickets/*/reject"
                                )
                                .hasRole("IT_SUPPORT")

                                .requestMatchers(
                                        HttpMethod.PATCH,
                                        "/api/tickets/*/status"
                                )
                                .hasAnyRole("IT_SUPPORT", "ADMIN")

                                .requestMatchers(
                                        HttpMethod.PATCH,
                                        "/api/tickets/*/assign"
                                )
                                .hasRole("ADMIN")

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/tickets/**"
                                )
                                .hasAnyRole("IT_SUPPORT", "ADMIN")

                                .requestMatchers(
                                        HttpMethod.PUT,
                                        "/api/tickets/**"
                                )
                                .hasRole("ADMIN")

                                .requestMatchers(
                                        HttpMethod.DELETE,
                                        "/api/tickets/**"
                                )
                                .hasRole("ADMIN")

                                .requestMatchers("/api/assets/**")
                                .hasRole("ADMIN")

                                .anyRequest()
                                .authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of("http://localhost:5173")
        );

        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        configuration.setExposedHeaders(
                List.of("Authorization")
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/api/**",
                configuration
        );

        return source;
    }
}
