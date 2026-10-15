// src/main/java/org/tradesphere/notification/Notification.java
package org.tradesphere.notification;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    private String id;

    @Indexed
    private String userId;

    private String title;

    private String message;

    private String type; // ORDER, VENDOR, DISPUTE, SYSTEM

    private String link;

    @Builder.Default
    private boolean read = false;

    @Builder.Default
    private Instant createdAt = Instant.now();
}