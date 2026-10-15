// AuthController.java
package org.tradesphere.auth;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping({"/auth", "/api/v1/auth"})
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping({"/signup", "/register"})
    public ResponseEntity<AuthDtos.SessionResponse> register(
            @Valid @RequestBody AuthDtos.RegisterRequest request,
            HttpServletResponse response
    ) {
        AuthDtos.SessionResponse session = authService.register(request);
        setAuthCookie(response, session.token(), Duration.ofDays(1));
        return ResponseEntity.ok(session);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDtos.SessionResponse> login(
            @Valid @RequestBody AuthDtos.LoginRequest request,
            HttpServletResponse response
    ) {
        AuthDtos.SessionResponse session = authService.login(request);
        setAuthCookie(response, session.token(), Duration.ofDays(1));
        return ResponseEntity.ok(session);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        setAuthCookie(response, "", Duration.ZERO);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<AuthDtos.SessionResponse> getCurrentUser(Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(authService.getCurrentUser(email));
    }

    @PutMapping({"/profile", "/me"})
    public ResponseEntity<AuthDtos.SessionResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody AuthDtos.UpdateProfileRequest request,
            HttpServletResponse response
    ) {
        String email = authentication != null ? authentication.getName() : null;
        AuthDtos.SessionResponse session = authService.updateProfile(email, request);
        setAuthCookie(response, session.token(), Duration.ofDays(1));
        return ResponseEntity.ok(session);
    }

    private void setAuthCookie(HttpServletResponse response, String token, Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(maxAge)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}