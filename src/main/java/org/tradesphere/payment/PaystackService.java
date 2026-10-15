// src/main/java/org/tradesphere/payment/PaystackService.java
package org.tradesphere.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaystackService {

    @Value("${paystack.secret.key:sk_test_placeholder}")
    private String secretKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> initializeTransaction(String email, BigDecimal amount, String orderId) {
        String url = "https://api.paystack.co/transaction/initialize";

        long amountInKobo = amount.multiply(BigDecimal.valueOf(100)).longValue();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", email);
        requestBody.put("amount", amountInKobo);
        requestBody.put("reference", "TS-PAYSTACK-" + orderId + "-" + System.currentTimeMillis());
        requestBody.put("callback_url", "http://localhost:5173/orders/confirmation");

        Map<String, String> metadata = new HashMap<>();
        metadata.put("orderId", orderId);
        requestBody.put("metadata", metadata);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + secretKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (Map<String, Object>) response.getBody().get("data");
            }
        } catch (Exception e) {
            Map<String, Object> mock = new HashMap<>();
            mock.put("authorization_url", "https://checkout.paystack.com/mock-" + orderId);
            mock.put("reference", "TS-PAYSTACK-" + orderId);
            return mock;
        }

        return null;
    }
}