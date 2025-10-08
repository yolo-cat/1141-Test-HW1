package com.deliveryplatform.models;

import com.deliveryplatform.exceptions.InvalidOrderStateException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a food delivery order - simplified version.
 */
public class Order {
    
    private static final Logger logger = LogManager.getLogger(Order.class);

    private String orderId;
    private String customerId;
    private String restaurantId;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private String deliveryAddress;
    private String driverId;

    public Order() {
        this.orderId = generateOrderId();
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.items = new ArrayList<>();
    }

    public Order(String customerId, String restaurantId, List<OrderItem> items, String deliveryAddress) {
        this();
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.items = new ArrayList<>(items);
        this.deliveryAddress = deliveryAddress;
        this.totalAmount = calculateTotalAmount();
        
        logger.info("Order created: orderId={}, customerId={}, restaurantId={}, amount={}", 
                   orderId, customerId, restaurantId, totalAmount);
    }



    public void updateStatus(OrderStatus newStatus) throws InvalidOrderStateException {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new InvalidOrderStateException(this.orderId, this.status, newStatus);
        }
        
        OrderStatus oldStatus = this.status;
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
        
        logger.info("Order status transition: orderId={}, from={}, to={}", 
                   orderId, oldStatus, newStatus);
    }

    public void assignDriver(String driverId) {
        if (driverId != null && !driverId.trim().isEmpty()) {
            this.driverId = driverId;
            this.updatedAt = LocalDateTime.now();
            logger.info("Driver {} assigned to order {}", driverId, orderId);
        }
    }



    /**
     * Recalculates and updates the total amount.
     */


    private String generateOrderId() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private BigDecimal calculateTotalAmount() {
        return items.stream()
                   .map(OrderItem::getTotalPrice)
                   .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Simplified compatibility methods
    public boolean isTerminal() { return status.isTerminal(); }
    public void accept(String reason) throws InvalidOrderStateException { updateStatus(OrderStatus.ACCEPTED); }
    public void reject(String reason) throws InvalidOrderStateException { updateStatus(OrderStatus.REJECTED); }
    public void cancel(String reason) throws InvalidOrderStateException { updateStatus(OrderStatus.CANCELLED); }
    public void startPreparing() throws InvalidOrderStateException { updateStatus(OrderStatus.PREPARING); }
    public void markReadyForDelivery() throws InvalidOrderStateException { updateStatus(OrderStatus.READY_FOR_DELIVERY); }
    public void markDelivered() throws InvalidOrderStateException { updateStatus(OrderStatus.DELIVERED); }
    public void setSpecialInstructions(String instructions) { 
        logger.info("Special instructions set for order {}: {}", orderId, instructions);
    }

    // Getters
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public String getRestaurantId() { return restaurantId; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<OrderItem> getItems() { return new ArrayList<>(items); }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public String getDriverId() { return driverId; }

    public void setItems(List<OrderItem> items) {
        this.items = new ArrayList<>(items);
        this.totalAmount = calculateTotalAmount();
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(orderId, order.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }

    @Override
    public String toString() {
        return String.format("Order{orderId='%s', customerId='%s', restaurantId='%s', status=%s, totalAmount=%s, itemCount=%d}",
                           orderId, customerId, restaurantId, status, totalAmount, items.size());
    }
}