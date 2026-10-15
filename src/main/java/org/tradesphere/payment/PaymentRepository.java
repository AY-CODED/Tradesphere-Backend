// src/main/java/org/tradesphere/payment/PaymentRepository.java
package org.tradesphere.payment;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    Optional<Payment> findByOrderId(String orderId);
    List<Payment> findByBuyerIdOrderByProcessedAtDesc(String buyerId);
    List<Payment> findByVendorIdsContainingOrderByProcessedAtDesc(String vendorId);
}