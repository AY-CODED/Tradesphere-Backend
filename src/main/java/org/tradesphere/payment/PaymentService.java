package org.tradesphere.payment;

import org.tradesphere.order.OrderCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        Payment payment = Payment.builder()
                .orderId(event.orderId())
                .amount(event.totalAmount())
                .status(Payment.PaymentStatus.SUCCESS)
                .build();

        paymentRepository.save(payment);
    }
}