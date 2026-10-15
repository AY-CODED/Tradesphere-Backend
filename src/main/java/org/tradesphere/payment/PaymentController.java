// src/main/java/org/tradesphere/payment/PaymentController.java
package org.tradesphere.payment;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.tradesphere.order.Order;
import org.tradesphere.order.OrderRepository;
import org.tradesphere.user.User;
import org.tradesphere.user.UserRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/payments", "/api/v1/payments"})
public class PaymentController {
    private final PaymentService paymentService;
    private final PaystackService paystackService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public PaymentController(
            PaymentService paymentService,
            PaystackService paystackService,
            OrderRepository orderRepository,
            UserRepository userRepository
    ) {
        this.paymentService = paymentService;
        this.paystackService = paystackService;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<List<Payment>> getMyPayments(Authentication authentication) {
        User user = resolveUser(authentication);
        if (user == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(paymentService.getBuyerPayments(user.getId()));
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<Payment>> getVendorPayments(@PathVariable String vendorId) {
        return ResponseEntity.ok(paymentService.getVendorPayments(vendorId));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Payment> getPaymentByOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(paymentService.getByOrderId(orderId));
    }

    @PostMapping("/order/{orderId}/release")
    public ResponseEntity<Payment> releaseEscrow(@PathVariable String orderId) {
        return ResponseEntity.ok(paymentService.releaseEscrowByOrderId(orderId));
    }

    @PostMapping("/initialize/{orderId}")
    public ResponseEntity<Map<String, Object>> initializePaystackPayment(
            @PathVariable String orderId,
            Authentication authentication
    ) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        String email = "customer@tradesphere.africa";
        if (authentication != null && authentication.getName() != null) {
            email = authentication.getName();
        } else if (order.getBuyerId() != null) {
            User buyer = userRepository.findById(order.getBuyerId()).orElse(null);
            if (buyer != null && buyer.getEmail() != null) {
                email = buyer.getEmail();
            }
        }

        Map<String, Object> paystackResponse = paystackService.initializeTransaction(
                email,
                order.getTotalAmount(),
                order.getId()
        );

        return ResponseEntity.ok(paystackResponse);
    }

    private User resolveUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) return null;
        return userRepository.findByEmail(authentication.getName()).orElse(null);
    }
}