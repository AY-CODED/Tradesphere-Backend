// src/main/java/org/tradesphere/payment/PaymentService.java
package org.tradesphere.payment;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.tradesphere.notification.NotificationService;
import org.tradesphere.order.OrderCreatedEvent;
import org.tradesphere.order.OrderItem;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;

    public PaymentService(PaymentRepository paymentRepository, NotificationService notificationService) {
        this.paymentRepository = paymentRepository;
        this.notificationService = notificationService;
    }

    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        List<String> vendorIds = event.items() != null
                ? event.items().stream()
                .map(OrderItem::getVendorId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .collect(Collectors.toList())
                : List.of();

        Payment payment = Payment.builder()
                .orderId(event.orderId())
                .buyerId(event.buyerId())
                .buyerName(event.buyerName())
                .vendorIds(vendorIds)
                .amount(event.totalAmount())
                .paymentMethod("ESCROW")
                .transactionReference("TS-PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .status(Payment.PaymentStatus.ESCROW_HELD)
                .processedAt(Instant.now())
                .build();

        paymentRepository.save(payment);
    }

    public Payment releaseEscrowByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(payment -> {
                    payment.setStatus(Payment.PaymentStatus.RELEASED);
                    payment.setReleasedAt(Instant.now());
                    Payment saved = paymentRepository.save(payment);

                    if (saved.getVendorIds() != null) {
                        for (String vendorId : saved.getVendorIds()) {
                            notificationService.create(
                                    vendorId,
                                    "Escrow Payout Released",
                                    "Escrow funds ($" + saved.getAmount() + ") for Order #" +
                                            orderId.substring(Math.max(0, orderId.length() - 6)).toUpperCase() +
                                            " have been released to your TradeSphere payout balance.",
                                    "VENDOR",
                                    "/vendor-workspace"
                            );
                        }
                    }
                    return saved;
                })
                .orElse(null);
    }

    public List<Payment> getBuyerPayments(String buyerId) {
        return paymentRepository.findByBuyerIdOrderByProcessedAtDesc(buyerId);
    }

    public List<Payment> getVendorPayments(String vendorId) {
        return paymentRepository.findByVendorIdsContainingOrderByProcessedAtDesc(vendorId);
    }

    public Payment getByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment record not found for order"));
    }
}