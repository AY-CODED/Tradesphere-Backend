package org.tradesphere.payment;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    private String id;

    @Indexed
    private String orderId;

    private BigDecimal amount;

    private PaymentStatus status;

    @Builder.Default
    private Instant processedAt = Instant.now();

    public enum PaymentStatus {
        SUCCESS, FAILED
    }
}
