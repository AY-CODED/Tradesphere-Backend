// src/main/java/org/tradesphere/payment/PaymentController.java
package org.tradesphere.payment;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    public ResponseEntity<Map<String, Object>> initialize(
            @RequestBody Map<String, Object> payload,
            Authentication authentication
    ) {
        String email = (String) payload.get("email");
        if ((email == null || email.isBlank()) && authentication != null) {
            email = authentication.getName();
        }
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid customer email is required.");
        }

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
        String buyerName = sanitizeDisplayName((String) payload.get("buyerName"));
        List<String> vendorIds = (List<String>) payload.get("vendorIds");
        BigDecimal amount = new BigDecimal(payload.getOrDefault("amount", "0").toString());
        String paymentMethod = (String) payload.getOrDefault("paymentMethod", "card");
        String reference = (String) payload.get("transactionReference");
        return ResponseEntity.ok(
                paymentService.recordEscrowPayment(orderId, buyerId, buyerName, vendorIds, amount, paymentMethod, reference)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<List<Payment>> getMyPayments(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.ok(List.of());
        }
        List<Payment> payments = paymentService.getPaymentsByBuyer(authentication.getName());
        payments.forEach(p -> p.setBuyerName(sanitizeDisplayName(p.getBuyerName())));
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<Payment>> getVendorPayments(@PathVariable String vendorId) {
        List<Payment> payments = paymentService.getPaymentsByVendor(vendorId);
        payments.forEach(p -> p.setBuyerName(sanitizeDisplayName(p.getBuyerName())));
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Payment> getPaymentByOrder(@PathVariable String orderId) {
        Payment payment = paymentService.getPaymentByOrderId(orderId);
        if (payment != null) {
            payment.setBuyerName(sanitizeDisplayName(payment.getBuyerName()));
        }
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/order/{orderId}/release")
    public ResponseEntity<Payment> releaseOrderEscrow(@PathVariable String orderId) {
        Payment payment = paymentService.releaseEscrowByOrderId(orderId);
        if (payment != null) {
            payment.setBuyerName(sanitizeDisplayName(payment.getBuyerName()));
        }
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/resolve-account")
    public ResponseEntity<Map<String, Object>> resolveAccount(
            @RequestParam String accountNumber,
            @RequestParam String bankCode
    ) {
        return ResponseEntity.ok(paymentService.resolveBankAccount(accountNumber, bankCode));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Map<String, Object>> withdrawVendorFunds(@RequestBody Map<String, String> payload) {
        String vendorId = payload.get("vendorId");
        String bankCode = payload.getOrDefault("bankCode", "058");
        String bankName = payload.getOrDefault("bankName", "Vendor Bank");
        String accountNumber = payload.getOrDefault("accountNumber", "0000000000");
        String accountName = payload.getOrDefault("accountName", "Vendor Account");
        return ResponseEntity.ok(
                paymentService.withdrawVendorFunds(vendorId, bankCode, bankName, accountNumber, accountName)
        );
    }

    private String sanitizeDisplayName(String name) {
        if (name == null || name.isBlank()) {
            return "Verified Buyer";
        }
        if (name.contains("@")) {
            return name.substring(0, name.indexOf('@'));
        }
        return name;
    }
}