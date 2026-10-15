// src/main/java/org/tradesphere/notification/NotificationController.java
package org.tradesphere.notification;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.tradesphere.user.User;
import org.tradesphere.user.UserRepository;

import java.util.List;

@RestController
@RequestMapping({"/notifications", "/api/v1/notifications"})
public class NotificationController {
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationController(NotificationService notificationService, UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications(Authentication authentication) {
        User user = resolveUser(authentication);
        if (user == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(notificationService.getUserNotifications(user.getId()));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(
            @PathVariable String id,
            Authentication authentication
    ) {
        User user = resolveUser(authentication);
        String userId = user != null ? user.getId() : null;
        return ResponseEntity.ok(notificationService.markAsRead(id, userId));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        User user = resolveUser(authentication);
        if (user != null) {
            notificationService.markAllAsRead(user.getId());
        }
        return ResponseEntity.noContent().build();
    }

    private User resolveUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) return null;
        return userRepository.findByEmail(authentication.getName()).orElse(null);
    }
}