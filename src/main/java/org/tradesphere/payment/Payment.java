// src/main/java/org/tradesphere/payment/Payment.java
package org.tradesphere.payment;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

    @Indexed
    private String buyerId;

    private String buyerName;

    @Indexed
    @Builder.Default
    private List<String> vendorIds = new ArrayList<>();

    private BigDecimal amount;

    private String paymentMethod;

    private String transactionReference;

    @Builder.Default
    private PaymentStatus status = PaymentStatus.ESCROW_HELD;

    @Builder.Default
    private Instant processedAt = Instant.now();

    private Instant releasedAt;

    public enum PaymentStatus {
        ESCROW_HELD, RELEASED, REFUNDED, FAILED
    }
}