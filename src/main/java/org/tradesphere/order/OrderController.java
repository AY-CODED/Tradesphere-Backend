// src/main/java/org/tradesphere/order/OrderController.java
package org.tradesphere.order;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.tradesphere.user.User;
import org.tradesphere.user.UserRepository;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping({"/orders", "/api/v1/orders"})
public class OrderController {
    private final OrderService orderService;
    private final UserRepository userRepository;

    public OrderController(OrderService orderService, UserRepository userRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<Order> placeOrder(
            Authentication authentication,
            @RequestBody PlaceOrderRequest request
    ) {
        User buyer = null;
        if (authentication != null && authentication.getName() != null) {
            buyer = userRepository.findByEmail(authentication.getName()).orElse(null);
        }

        String resolvedBuyerId = (request.buyerId() != null && !request.buyerId().isBlank())
                ? request.buyerId()
                : (buyer != null ? buyer.getId() : "guest");

        String resolvedBuyerName = (request.buyerName() != null && !request.buyerName().isBlank())
                ? request.buyerName()
                : (buyer != null && buyer.getFullName() != null ? buyer.getFullName() : "TradeSphere Buyer");

        return ResponseEntity.ok(orderService.placeOrder(
                resolvedBuyerId,
                resolvedBuyerName,
                request.shippingAddress(),
                request.deliveryMethod(),
                request.paymentMethod(),
                request.deliveryFee(),
                request.items()
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<List<Order>> getMyOrders(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.ok(List.of());
        }
        return userRepository.findByEmail(authentication.getName())
                .map(user -> ResponseEntity.ok(orderService.getBuyerOrders(user.getId())))
                .orElse(ResponseEntity.ok(List.of()));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.getById(orderId));
    }

    @PostMapping("/{orderId}/approve-packing")
    public ResponseEntity<Order> approvePacking(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.approvePacking(orderId));
    }

    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<List<Order>> getBuyerOrders(@PathVariable String buyerId) {
        return ResponseEntity.ok(orderService.getBuyerOrders(buyerId));
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<Order>> getVendorOrders(@PathVariable String vendorId) {
        return ResponseEntity.ok(orderService.getVendorOrders(vendorId));
    }

    public record PlaceOrderRequest(
            String buyerId,
            String buyerName,
            String shippingAddress,
            String deliveryMethod,
            String paymentMethod,
            BigDecimal deliveryFee,
            List<OrderService.CartItemRequest> items
    ) {}
}