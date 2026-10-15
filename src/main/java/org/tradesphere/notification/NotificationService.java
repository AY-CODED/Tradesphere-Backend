// src/main/java/org/tradesphere/notification/NotificationService.java
package org.tradesphere.notification;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.tradesphere.order.OrderCreatedEvent;
import org.tradesphere.order.OrderItem;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification create(String userId, String title, String message, String type, String link) {
        if (userId == null || userId.isBlank()) return null;
        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type != null ? type : "SYSTEM")
                .link(link)
                .read(false)
                .build();
        return notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Notification markAsRead(String notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (userId != null && userId.equals(notification.getUserId())) {
            notification.setRead(true);
            return notificationRepository.save(notification);
        }
        return notification;
    }

    public void markAllAsRead(String userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalse(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Async
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        String shortOrderId = event.orderId() != null && event.orderId().length() > 6
                ? event.orderId().substring(event.orderId().length() - 6).toUpperCase()
                : String.valueOf(event.orderId());

        if (event.buyerId() != null && !event.buyerId().equals("guest")) {
            create(
                    event.buyerId(),
                    "Order Confirmed",
                    "Your order #" + shortOrderId + " ($" + event.totalAmount() + ") has been placed and secured in escrow.",
                    "ORDER",
                    "/account?section=My%20orders"
            );
        }

        if (event.items() != null) {
            Set<String> vendorIds = event.items().stream()
                    .map(OrderItem::getVendorId)
                    .filter(id -> id != null && !id.isBlank())
                    .collect(Collectors.toSet());

            for (String vendorId : vendorIds) {
                create(
                        vendorId,
                        "New Order Received",
                        "You received a new order (#" + shortOrderId + ") from " + (event.buyerName() != null ? event.buyerName() : "a buyer") + ".",
                        "VENDOR",
                        "/vendor-workspace"
                );
            }
        }
    }
}