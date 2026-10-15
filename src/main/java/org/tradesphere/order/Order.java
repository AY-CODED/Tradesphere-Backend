// src/main/java/org/tradesphere/order/Order.java
package org.tradesphere.order;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    private String id;

    @Indexed
    private String buyerId;

    private String buyerName;

    private String shippingAddress;

    private String deliveryMethod;

    private String paymentMethod;

    private BigDecimal subtotal;

    private BigDecimal deliveryFee;

    private BigDecimal tax;

    private BigDecimal totalAmount;

    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Builder.Default
    private boolean packingApproved = false;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    public enum OrderStatus {
        PENDING, PAID, SHIPPED, COMPLETED, DISPUTED, CANCELLED
    }
}