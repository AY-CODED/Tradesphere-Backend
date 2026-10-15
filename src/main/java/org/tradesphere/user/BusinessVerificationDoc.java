// src/main/java/org/tradesphere/user/BusinessVerificationDoc.java
package org.tradesphere.user;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "business_verifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessVerificationDoc {
    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private String email;

    @Builder.Default
    private Map<String, String> details = new HashMap<>();

    @Builder.Default
    private String status = "DRAFT"; // DRAFT, SUBMITTED, APPROVED

    @Builder.Default
    private Instant updatedAt = Instant.now();
}