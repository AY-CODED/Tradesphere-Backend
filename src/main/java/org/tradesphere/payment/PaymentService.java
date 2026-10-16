// src/main/java/org/tradesphere/payment/PaymentService.java
package org.tradesphere.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    @Value("${paystack.secret-key:}")
    private String paystackSecretKey;

    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> initializePayment(String email, double amountInNaira, String callbackUrl) {
        String url = "https://api.paystack.co/transaction/initialize";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(paystackSecretKey);

        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("amount", (int) Math.round(amountInNaira * 100)); // Convert Naira to Kobo
        if (callbackUrl != null && !callbackUrl.isEmpty()) {
            body.put("callback_url", callbackUrl);
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        return response.getBody();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> verifyPayment(String reference) {
        String url = "https://api.paystack.co/transaction/verify/" + reference;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(paystackSecretKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        return response.getBody();
    }

    public Payment recordEscrowPayment(String orderId, String buyerId, String buyerName, List<String> vendorIds, BigDecimal amount, String paymentMethod, String reference) {
        return paymentRepository.findByOrderId(orderId).orElseGet(() -> {
            Payment payment = Payment.builder()
                    .orderId(orderId)
                    .buyerId(buyerId)
                    .buyerName(buyerName)
                    .vendorIds(vendorIds != null ? vendorIds : List.of())
                    .amount(amount != null ? amount : BigDecimal.ZERO)
                    .paymentMethod(paymentMethod != null ? paymentMethod : "card")
                    .transactionReference(reference != null && !reference.isBlank()
                            ? reference
                            : "TS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .status(Payment.PaymentStatus.ESCROW_HELD)
                    .processedAt(Instant.now())
                    .build();
            return paymentRepository.save(payment);
        });
    }

    public List<Payment> getPaymentsByBuyer(String buyerId) {
        return paymentRepository.findByBuyerIdOrderByProcessedAtDesc(buyerId);
    }

    public List<Payment> getPaymentsByVendor(String vendorId) {
        return paymentRepository.findByVendorIdsContainingOrderByProcessedAtDesc(vendorId);
    }

    public Payment getPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found for order: " + orderId));
    }

    public Payment releaseEscrowByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId).map(payment -> {
            payment.setStatus(Payment.PaymentStatus.RELEASED);
            payment.setReleasedAt(Instant.now());
            return paymentRepository.save(payment);
        }).orElse(null);
    }

    public Map<String, Object> withdrawVendorFunds(String vendorId, String bankName, String accountNumber, String accountName) {
        List<Payment> vendorPayments = paymentRepository.findByVendorIdsContainingOrderByProcessedAtDesc(vendorId);
        List<Payment> releasedPayments = vendorPayments.stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.RELEASED)
                .toList();

        if (releasedPayments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No released escrow funds available for withdrawal.");
        }

        BigDecimal totalWithdrawn = BigDecimal.ZERO;
        for (Payment payment : releasedPayments) {
            totalWithdrawn = totalWithdrawn.add(payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO);
            payment.setStatus(Payment.PaymentStatus.WITHDRAWN);
            paymentRepository.save(payment);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("withdrawnAmount", totalWithdrawn);
        response.put("bankName", bankName);
        response.put("accountNumber", accountNumber);
        response.put("accountName", accountName);
        response.put("processedAt", Instant.now());
        response.put("message", "Withdrawal of ₦" + totalWithdrawn + " to " + bankName + " (" + accountNumber + ") processed.");
        return response;
    }
}