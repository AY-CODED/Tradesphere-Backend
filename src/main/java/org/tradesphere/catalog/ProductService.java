// src/main/java/org/tradesphere/catalog/ProductService.java
package org.tradesphere.catalog;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAll() {
        return productRepository.findAll();
    }

    public List<Product> getByVendorId(String vendorId) {
        return productRepository.findByVendorId(vendorId);
    }

    public Product getById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    public Product create(
            String vendorId,
            String vendorName,
            String name,
            String category,
            String sku,
            String description,
            String imageUrl,
            BigDecimal price,
            int stock
    ) {
        Product product = Product.builder()
                .vendorId(vendorId)
                .vendorName(vendorName)
                .name(name)
                .category(category)
                .sku(sku)
                .description(description)
                .imageUrl(imageUrl)
                .price(price)
                .stock(stock)
                .build();
        return productRepository.save(product);
    }

    public void deductStock(String productId, int quantity) {
        Product product = getById(productId);
        if (product.getStock() < quantity) {
            throw new IllegalStateException("Insufficient stock for product: " + product.getName());
        }
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }
}