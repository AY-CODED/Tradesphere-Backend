// src/main/java/org/tradesphere/order/OrderService.java
package org.tradesphere.order;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.tradesphere.catalog.Product;
import org.tradesphere.catalog.ProductService;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository, ProductService productService, ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.eventPublisher = eventPublisher;
    }

    public Order placeOrder(
            String buyerId,
            String buyerName,
            String shippingAddress,
            String deliveryMethod,
            String paymentMethod,
            BigDecimal deliveryFee,
            List<CartItemRequest> items
    ) {
        BigDecimal subtotal = BigDecimal.ZERO;
        Order order = Order.builder()
                .buyerId(buyerId)
                .buyerName(buyerName)
                .shippingAddress(shippingAddress)
                .deliveryMethod(deliveryMethod)
                .paymentMethod(paymentMethod)
                .status(Order.OrderStatus.PAID)
                .build();

        for (CartItemRequest itemReq : items) {
            Product product = productService.getById(itemReq.productId());
            productService.deductStock(product.getId(), itemReq.quantity());

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.quantity()));
            subtotal = subtotal.add(lineTotal);

            order.getItems().add(OrderItem.builder()
                    .productId(product.getId())
                    .vendorId(product.getVendorId())
                    .vendorName(product.getVendorName() != null ? product.getVendorName() : "TradeSphere Vendor")
                    .productName(product.getName())
                    .category(product.getCategory())
                    .sku(product.getSku())
                    .quantity(itemReq.quantity())
                    .price(product.getPrice())
                    .build());
        }

        BigDecimal safeDeliveryFee = deliveryFee != null ? deliveryFee : BigDecimal.valueOf(45);
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.05));
        BigDecimal total = subtotal.add(safeDeliveryFee).add(tax);

        order.setSubtotal(subtotal);
        order.setDeliveryFee(safeDeliveryFee);
        order.setTax(tax);
        order.setTotalAmount(total);

        Order savedOrder = orderRepository.save(order);
        eventPublisher.publishEvent(new OrderCreatedEvent(savedOrder.getId(), buyerId, savedOrder.getTotalAmount()));

        return savedOrder;
    }

    public Order getById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    public List<Order> getBuyerOrders(String buyerId) {
        return orderRepository.findByBuyerId(buyerId);
    }

    public List<Order> getVendorOrders(String vendorId) {
        return orderRepository.findByItemsVendorId(vendorId);
    }

    public Order approvePacking(String orderId) {
        Order order = getById(orderId);
        order.setPackingApproved(true);
        order.setStatus(Order.OrderStatus.SHIPPED);
        return orderRepository.save(order);
    }

    public record CartItemRequest(String productId, int quantity) {}
}