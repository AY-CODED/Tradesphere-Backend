// src/main/java/org/tradesphere/catalog/Product.java
package org.tradesphere.catalog;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    private String id;

    @Indexed
    private String vendorId;

    private String vendorName;

    private String name;

    private String category;

    private String sku;

    private String description;

    private String imageUrl;

    private BigDecimal price;

    private Integer stock;

    @Builder.Default
    private Instant createdAt = Instant.now();
}