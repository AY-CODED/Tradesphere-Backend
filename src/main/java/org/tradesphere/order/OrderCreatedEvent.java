package org.tradesphere.order;

import java.math.BigDecimal;

public record OrderCreatedEvent(
        String orderId,
        String buyerId,
        BigDecimal totalAmount
) {}
