package org.tradesphere.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Skip CORS preflight requests.
     *
     * Browsers send OPTIONS requests before certain cross-origin
     * requests. These requests must not require a JWT.
     */
    @Override
    protected boolean shouldNotFilter(
            @NonNull HttpServletRequest request) {

        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // Do not overwrite an existing authentication.
            if (SecurityContextHolder.getContext().getAuthentication()
                    == null) {

                String jwt = extractToken(request);

                if (jwt != null && !jwt.isBlank()) {
                    authenticateUser(jwt, request);
                }
            }

            filterChain.doFilter(request, response);

        } catch (ServletException | IOException e) {
            throw e;
        } catch (RuntimeException e) {
            log.error(
                    "Unexpected JWT filter error for {} {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    e
            );

            throw e;
        }
    }

    /**
     * Validate the JWT and populate Spring Security's context.
     */
    private void authenticateUser(
            String jwt,
            HttpServletRequest request) {

        try {
            String userEmail = jwtService.extractEmail(jwt);
            String role = jwtService.extractRole(jwt);

            if (userEmail == null || userEmail.isBlank()) {
                log.debug("JWT does not contain a valid user email");
                return;
            }

            if (role == null || role.isBlank()) {
                log.debug("JWT does not contain a valid role");
                return;
            }

            if (!jwtService.isTokenValid(jwt, userEmail)) {
                log.debug("JWT validation failed");
                return;
            }

            // Avoid accepting malformed role values.
            String normalizedRole = role.startsWith("ROLE_")
                    ? role.substring(5)
                    : role;

            if (normalizedRole.isBlank()) {
                log.debug("JWT contains an invalid role");
                return;
            }

            var authorities = List.of(
                    new SimpleGrantedAuthority(
                            "ROLE_" + normalizedRole
                    )
            );

            var authentication =
                    new UsernamePasswordAuthenticationToken(
                            userEmail,
                            null,
                            authorities
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

        } catch (Exception e) {
            // An invalid or expired JWT must not authenticate the user.
            // Protected endpoints will be rejected by Spring Security.
            SecurityContextHolder.clearContext();

            log.warn(
                    "Rejected JWT for {} {}: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    e.getMessage()
            );
        }
    }

    /**
     * Extract the JWT from the Authorization header first,
     * then fall back to the jwt cookie.
     */
    private String extractToken(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null
                && authHeader.regionMatches(
                true, 0, "Bearer ", 0, 7)) {

            String token = authHeader.substring(7).trim();

            if (!token.isBlank()) {
                return token;
            }
        }

        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(cookie -> "jwt".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .filter(value ->
                            value != null && !value.isBlank())
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }
}