// src/main/java/org/tradesphere/payment/PaystackWebhookController.java
package org.tradesphere.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tradesphere.order.Order;
import org.tradesphere.order.OrderRepository;

import java.util.Map;

@RestController
@RequestMapping({"/payments/webhook", "/api/v1/payments/webhook"})
public class PaystackWebhookController {

    @Value("${paystack.secret-key:sk_test_placeholder}")
    private String secretKey;

    private final OrderRepository orderRepository;
    private final PaymentService paymentService;

    public PaystackWebhookController(OrderRepository orderRepository, PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
    }

    @PostMapping
    @SuppressWarnings("unchecked")
    public ResponseEntity<String> handlePaystackWebhook(
            @RequestHeader(value = "x-paystack-signature", required = false) String signature,
            @RequestBody Map<String, Object> payload
    ) {
        String event = (String) payload.get("event");
        if ("charge.success".equals(event)) {
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            if (data != null) {
                Map<String, Object> metadata = (Map<String, Object>) data.get("metadata");
                if (metadata != null && metadata.get("orderId") != null) {
                    String orderId = (String) metadata.get("orderId");
                    orderRepository.findById(orderId).ifPresent(order -> {
                        order.setStatus(Order.OrderStatus.PAID);
                        orderRepository.save(order);
                    });
                }
            }
        }

        return ResponseEntity.ok("Webhook received");
    }
}