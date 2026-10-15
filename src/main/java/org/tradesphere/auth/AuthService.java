// src/main/java/org/tradesphere/auth/AuthService.java
package org.tradesphere.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.tradesphere.user.User;
import org.tradesphere.user.UserRepository;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthDtos.SessionResponse register(AuthDtos.RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists.");
        }

        User.Role assignedRole = request.role() != null ? request.role() : User.Role.BUYER;

        User user = User.builder()
                .fullName(request.fullName().trim())
                .email(request.email().trim().toLowerCase())
                .phoneNumber(request.phoneNumber() != null ? request.phoneNumber().trim() : null)
                .password(passwordEncoder.encode(request.password()))
                .role(assignedRole)
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser);

        return new AuthDtos.SessionResponse(toDto(savedUser), token);
    }

    public AuthDtos.SessionResponse login(AuthDtos.LoginRequest request) {
        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));

        String token = jwtService.generateToken(user);

        return new AuthDtos.SessionResponse(toDto(user), token);
    }

    public AuthDtos.SessionResponse getCurrentUser(String email) {
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User session not found."));

        return new AuthDtos.SessionResponse(toDto(user), null);
    }

    public AuthDtos.SessionResponse updateProfile(String currentEmail, AuthDtos.UpdateProfileRequest request) {
        if (currentEmail == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated.");
        }

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found."));

        if (!user.getEmail().equalsIgnoreCase(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use.");
        }

        user.setFullName(request.fullName().trim());
        user.setPhoneNumber(request.phoneNumber() != null ? request.phoneNumber().trim() : null);
        user.setEmail(request.email().trim().toLowerCase());
        if (request.role() != null) {
            user.setRole(request.role());
        }

        User updatedUser = userRepository.save(user);
        String newToken = jwtService.generateToken(updatedUser);

        return new AuthDtos.SessionResponse(toDto(updatedUser), newToken);
    }

    private AuthDtos.UserDto toDto(User user) {
        return new AuthDtos.UserDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole() != null ? user.getRole().name() : User.Role.BUYER.name()
        );
    }
}