// src/main/java/org/tradesphere/order/Dispute.java
package org.tradesphere.order;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "disputes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dispute {
    @Id
    private String id;

    private String orderId;

    private String stakeholderName;

    private String stakeholderId;

    private String issue;

    private BigDecimal amount;

    @Builder.Default
    private String priority = "High"; // High, Medium, Low

    @Builder.Default
    private String status = "Open"; // Open, Under review, Awaiting evidence, Resolved

    @Builder.Default
    private Instant createdAt = Instant.now();
}