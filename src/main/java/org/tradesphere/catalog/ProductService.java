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

    public Product update(
            String id,
            String name,
            String category,
            String sku,
            String description,
            String imageUrl,
            BigDecimal price,
            Integer stock
    ) {
        Product product = getById(id);
        if (name != null && !name.isBlank()) product.setName(name.trim());
        if (category != null) product.setCategory(category.trim());
        if (sku != null) product.setSku(sku.trim());
        if (description != null) product.setDescription(description.trim());
        if (imageUrl != null) product.setImageUrl(imageUrl.trim());
        if (price != null) product.setPrice(price);
        if (stock != null) product.setStock(stock);
        return productRepository.save(product);
    }

    public void delete(String id) {
        Product product = getById(id);
        productRepository.delete(product);
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