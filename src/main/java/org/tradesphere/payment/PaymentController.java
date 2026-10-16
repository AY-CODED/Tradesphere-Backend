// src/main/java/org/tradesphere/payment/PaymentController.java
package org.tradesphere.payment;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/payments", "/payments"})
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/initialize")
    public ResponseEntity<Map<String, Object>> initialize(@RequestBody Map<String, Object> payload) {
        String email = (String) payload.getOrDefault("email", "customer@tradesphere.com");
        double amount = Double.parseDouble(payload.get("amount").toString());
        String callbackUrl = (String) payload.get("callbackUrl");
        return ResponseEntity.ok(paymentService.initializePayment(email, amount, callbackUrl));
    }

    @GetMapping("/verify/{reference}")
    public ResponseEntity<Map<String, Object>> verify(@PathVariable String reference) {
        return ResponseEntity.ok(paymentService.verifyPayment(reference));
    }

    @PostMapping("/record")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Payment> recordPayment(@RequestBody Map<String, Object> payload) {
        String orderId = (String) payload.get("orderId");
        String buyerId = (String) payload.get("buyerId");
        String buyerName = (String) payload.get("buyerName");
        List<String> vendorIds = (List<String>) payload.get("vendorIds");
        BigDecimal amount = new BigDecimal(payload.getOrDefault("amount", "0").toString());
        String paymentMethod = (String) payload.getOrDefault("paymentMethod", "card");
        String reference = (String) payload.get("transactionReference");
        return ResponseEntity.ok(paymentService.recordEscrowPayment(orderId, buyerId, buyerName, vendorIds, amount, paymentMethod, reference));
    }

    @GetMapping("/me")
    public ResponseEntity<List<Payment>> getMyPayments(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(paymentService.getPaymentsByBuyer(authentication.getName()));
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<Payment>> getVendorPayments(@PathVariable String vendorId) {
        return ResponseEntity.ok(paymentService.getPaymentsByVendor(vendorId));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Payment> getPaymentByOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }

    @PostMapping("/order/{orderId}/release")
    public ResponseEntity<Payment> releaseOrderEscrow(@PathVariable String orderId) {
        return ResponseEntity.ok(paymentService.releaseEscrowByOrderId(orderId));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Map<String, Object>> withdrawVendorFunds(@RequestBody Map<String, String> payload) {
        String vendorId = payload.get("vendorId");
        String bankName = payload.getOrDefault("bankName", "Vendor Bank");
        String accountNumber = payload.getOrDefault("accountNumber", "0000000000");
        String accountName = payload.getOrDefault("accountName", "Vendor Account");
        return ResponseEntity.ok(paymentService.withdrawVendorFunds(vendorId, bankName, accountNumber, accountName));
    }
}