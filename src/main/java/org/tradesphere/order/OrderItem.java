// src/main/java/org/tradesphere/order/OrderItem.java
package org.tradesphere.order;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    private String productId;
    private String vendorId;
    private String vendorName;
    private String productName;
    private String category;
    private String sku;
    private Integer quantity;
    private BigDecimal price;
}