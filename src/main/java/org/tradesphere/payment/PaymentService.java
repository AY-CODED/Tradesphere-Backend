package org.tradesphere.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    @Value("${paystack.secret-key}")
    private String paystackSecretKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> initializePayment(String email, double amountInNaira, String callbackUrl) {
        String url = "https://api.paystack.co/transaction/initialize";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(paystackSecretKey);

        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("amount", (int) (amountInNaira * 100)); // Convert Naira to Kobo
        if (callbackUrl != null && !callbackUrl.isEmpty()) {
            body.put("callback_url", callbackUrl);
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        return response.getBody();
    }

    public Map<String, Object> verifyPayment(String reference) {
        String url = "https://api.paystack.co/transaction/verify/" + reference;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(paystackSecretKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        return response.getBody();
    }
}