// src/main/java/org/tradesphere/order/OrderCreatedEvent.java
package org.tradesphere.order;

import java.math.BigDecimal;
import java.util.List;

public record OrderCreatedEvent(
        String orderId,
        String buyerId,
        String buyerName,
        BigDecimal totalAmount,
        List<OrderItem> items
) {}