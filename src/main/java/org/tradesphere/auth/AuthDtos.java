// src/main/java/org/tradesphere/auth/AuthDtos.java
package org.tradesphere.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.tradesphere.user.User;

public class AuthDtos {
    public record RegisterRequest(
            @NotBlank(message = "Enter your full name.") String fullName,
            @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
            String phoneNumber,
            @NotBlank(message = "Password is required") String password,
            User.Role role
    ) {}

    public record LoginRequest(
            @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
            @NotBlank(message = "Password is required") String password
    ) {}

    public record UpdateProfileRequest(
            @NotBlank(message = "Full name is required") String fullName,
            String phoneNumber,
            @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
            User.Role role
    ) {}

    public record UserDto(
            String id,
            String fullName,
            String email,
            String phoneNumber,
            String role
    ) {}

    public record SessionResponse(
            UserDto user,
            String token
    ) {}
}