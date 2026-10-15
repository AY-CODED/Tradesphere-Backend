package org.tradesphere.payment;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/initialize")
    public ResponseEntity<Map<String, Object>> initialize(@RequestBody Map<String, Object> payload) {
        String email = (String) payload.get("email");
        double amount = Double.parseDouble(payload.get("amount").toString());
        String callbackUrl = (String) payload.get("callbackUrl");
        return ResponseEntity.ok(paymentService.initializePayment(email, amount, callbackUrl));
    }

    @GetMapping("/verify/{reference}")
    public ResponseEntity<Map<String, Object>> verify(@PathVariable String reference) {
        return ResponseEntity.ok(paymentService.verifyPayment(reference));
    }
}