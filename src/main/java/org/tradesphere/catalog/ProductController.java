// src/main/java/org/tradesphere/catalog/ProductController.java
package org.tradesphere.catalog;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.tradesphere.user.User;
import org.tradesphere.user.UserRepository;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping({"/products", "/api/v1/products"})
public class ProductController {
    private final ProductService productService;
    private final UserRepository userRepository;

    public ProductController(ProductService productService, UserRepository userRepository) {
        this.productService = productService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<Product>> listAll() {
        return ResponseEntity.ok(productService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable String id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<Product>> getVendorProducts(@PathVariable String vendorId) {
        return ResponseEntity.ok(productService.getByVendorId(vendorId));
    }

    @PostMapping
    public ResponseEntity<Product> create(
            Authentication authentication,
            @RequestBody CreateProductRequest request
    ) {
        User vendor = null;
        if (authentication != null && authentication.getName() != null) {
            vendor = userRepository.findByEmail(authentication.getName()).orElse(null);
            if (vendor != null && vendor.getRole() != User.Role.VENDOR && vendor.getRole() != User.Role.ADMIN) {
                vendor.setRole(User.Role.VENDOR);
                userRepository.save(vendor);
            }
        }

        String resolvedVendorId = (request.vendorId() != null && !request.vendorId().isBlank())
                ? request.vendorId()
                : (vendor != null ? vendor.getId() : "anonymous-vendor");

        String resolvedVendorName = (request.vendorName() != null && !request.vendorName().isBlank())
                ? request.vendorName()
                : (vendor != null && vendor.getFullName() != null ? vendor.getFullName() : "TradeSphere Vendor");

        int resolvedStock = request.stock() != null
                ? request.stock()
                : (request.quantity() != null ? request.quantity() : 0);

        return ResponseEntity.ok(productService.create(
                resolvedVendorId,
                resolvedVendorName,
                request.name(),
                request.category(),
                request.sku(),
                request.description(),
                request.imageUrl(),
                request.price(),
                resolvedStock
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(
            @PathVariable String id,
            @RequestBody UpdateProductRequest request
    ) {
        Integer resolvedStock = request.stock() != null ? request.stock() : request.quantity();
        return ResponseEntity.ok(productService.update(
                id,
                request.name(),
                request.category(),
                request.sku(),
                request.description(),
                request.imageUrl(),
                request.price(),
                resolvedStock
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    public record CreateProductRequest(
            String vendorId,
            String vendorName,
            String name,
            String category,
            String sku,
            String description,
            String imageUrl,
            BigDecimal price,
            Integer stock,
            Integer quantity
    ) {}

    public record UpdateProductRequest(
            String name,
            String category,
            String sku,
            String description,
            String imageUrl,
            BigDecimal price,
            Integer stock,
            Integer quantity
    ) {}
}