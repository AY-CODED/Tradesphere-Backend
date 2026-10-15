// src/main/java/org/tradesphere/order/DisputeController.java
package org.tradesphere.order;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.tradesphere.user.User;
import org.tradesphere.user.UserRepository;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping({"/disputes", "/api/v1/disputes"})
public class DisputeController {
    private final DisputeRepository disputeRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public DisputeController(
            DisputeRepository disputeRepository,
            OrderRepository orderRepository,
            UserRepository userRepository
    ) {
        this.disputeRepository = disputeRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<Dispute>> listAll() {
        return ResponseEntity.ok(disputeRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Dispute> createDispute(
            Authentication authentication,
            @RequestBody CreateDisputeRequest request
    ) {
        User user = null;
        if (authentication != null && authentication.getName() != null) {
            user = userRepository.findByEmail(authentication.getName()).orElse(null);
        }

        Order order = null;
        if (request.orderId() != null && !request.orderId().isBlank()) {
            order = orderRepository.findById(request.orderId()).orElse(null);
            if (order != null) {
                order.setStatus(Order.OrderStatus.DISPUTED);
                orderRepository.save(order);
            }
        }

        String stakeholderName = user != null && user.getFullName() != null
                ? user.getFullName()
                : (order != null && order.getBuyerName() != null ? order.getBuyerName() : "TradeSphere Buyer");

        BigDecimal amount = request.amount() != null
                ? request.amount()
                : (order != null && order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);

        Dispute dispute = Dispute.builder()
                .orderId(request.orderId() != null ? request.orderId() : "N/A")
                .stakeholderId(user != null ? user.getId() : null)
                .stakeholderName(stakeholderName)
                .issue(request.issue())
                .amount(amount)
                .priority(request.priority() != null ? request.priority() : "High")
                .status("Open")
                .build();

        return ResponseEntity.ok(disputeRepository.save(dispute));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Dispute> updateStatus(
            @PathVariable String id,
            @RequestBody UpdateDisputeStatusRequest request
    ) {
        Dispute dispute = disputeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found"));
        dispute.setStatus(request.status());
        return ResponseEntity.ok(disputeRepository.save(dispute));
    }

    public record CreateDisputeRequest(String orderId, String issue, BigDecimal amount, String priority) {}
    public record UpdateDisputeStatusRequest(String status) {}
}