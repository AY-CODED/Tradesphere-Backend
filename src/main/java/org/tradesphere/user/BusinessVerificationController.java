// src/main/java/org/tradesphere/user/BusinessVerificationController.java
package org.tradesphere.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping({"/verification", "/api/v1/verification"})
public class BusinessVerificationController {
    private final BusinessVerificationRepository verificationRepository;
    private final UserRepository userRepository;

    public BusinessVerificationController(
            BusinessVerificationRepository verificationRepository,
            UserRepository userRepository
    ) {
        this.verificationRepository = verificationRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<BusinessVerificationDoc> getMyVerification(Authentication authentication) {
        User user = requireUser(authentication);
        BusinessVerificationDoc doc = verificationRepository.findByUserId(user.getId())
                .orElseGet(() -> BusinessVerificationDoc.builder()
                        .userId(user.getId())
                        .email(user.getEmail())
                        .status("DRAFT")
                        .build());
        return ResponseEntity.ok(doc);
    }

    @PutMapping("/me")
    public ResponseEntity<BusinessVerificationDoc> saveVerification(
            Authentication authentication,
            @RequestBody SaveVerificationRequest request
    ) {
        User user = requireUser(authentication);
        BusinessVerificationDoc doc = verificationRepository.findByUserId(user.getId())
                .orElseGet(() -> BusinessVerificationDoc.builder()
                        .userId(user.getId())
                        .email(user.getEmail())
                        .build());

        if (request.details() != null) {
            doc.setDetails(request.details());
        }
        if (request.submitted() != null && request.submitted()) {
            doc.setStatus("SUBMITTED");
            if (user.getRole() != User.Role.ADMIN) {
                user.setRole(User.Role.VENDOR);
                userRepository.save(user);
            }
        } else if (doc.getStatus() == null) {
            doc.setStatus("DRAFT");
        }
        doc.setUpdatedAt(Instant.now());

        return ResponseEntity.ok(verificationRepository.save(doc));
    }

    private User requireUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated.");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found."));
    }

    public record SaveVerificationRequest(Map<String, String> details, Boolean submitted) {}
}