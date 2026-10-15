package org.tradesphere.auth;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final List<String> allowedOrigins;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthFilter,
            @Value("${cors.allowed-origins:https://trade-sphere-brown.vercel.app}")
            String allowedOriginsProp) {

        this.jwtAuthFilter = jwtAuthFilter;

        this.allowedOrigins = Arrays.stream(
                        allowedOriginsProp.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .map(origin -> origin.replaceAll("/+$", ""))
                .toList();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                // Configure CORS before authentication.
                .cors(cors ->
                        cors.configurationSource(corsConfigurationSource())
                )

                // Disable CSRF for the stateless JWT API.
                .csrf(AbstractHttpConfigurer::disable)

                // Use stateless authentication.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // Return JSON for unauthenticated requests.
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(
                                (request, response, authException) -> {
                                    response.setStatus(
                                            HttpServletResponse.SC_UNAUTHORIZED
                                    );
                                    response.setContentType("application/json");
                                    response.setCharacterEncoding("UTF-8");

                                    response.getWriter().write(
                                            "{\"message\":\"Unauthorized\",\"errors\":{}}"
                                    );
                                }
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Allow browser preflight requests.
                        .requestMatchers(
                                HttpMethod.OPTIONS, "/**"
                        ).permitAll()

                        // Public authentication endpoints.
                        .requestMatchers(
                                "/auth/signup",
                                "/auth/register",
                                "/auth/login",
                                "/auth/logout",
                                "/api/v1/auth/signup",
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/api/v1/auth/logout"
                        ).permitAll()

                        // Public product listing and product details.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/products/**",
                                "/api/v1/products",
                                "/api/v1/products/**"
                        ).permitAll()

                        // Preserved from your original configuration.
                        .requestMatchers(
                                "/payments/**",
                                "/api/v1/payments/**"
                        ).permitAll()

                        // Require authentication for all other endpoints.
                        .anyRequest().authenticated()
                )

                // Keep JWT authentication enabled.
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // Allow your deployed Vercel frontend.
        configuration.setAllowedOrigins(allowedOrigins);

        // Allow the required HTTP methods.
        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        // Allow Authorization, Content-Type and other request headers.
        configuration.setAllowedHeaders(List.of("*"));

        // Support cross-origin cookies when required.
        configuration.setAllowCredentials(true);

        // Cache preflight results for one hour.
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        // Apply CORS configuration to every API route.
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}