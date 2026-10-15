package org.tradesphere.notification;

import org.tradesphere.order.OrderCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Async
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        System.out.printf("Notification dispatched: Order %s placed for buyer %s (Total: %s)%n",
                event.orderId(), event.buyerId(), event.totalAmount());
    }
}