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
 * Represents a food delivery order with complete lifecycle management.
 * Includes state transition validation and logging capabilities.
 */
public class Order {
    
    private static final Logger logger = LogManager.getLogger(Order.class);

    @NotBlank(message = "Order ID cannot be blank")
    private String orderId;

    @NotBlank(message = "Customer ID cannot be blank")
    private String customerId;

    @NotBlank(message = "Restaurant ID cannot be blank")
    private String restaurantId;

    @NotNull(message = "Order status cannot be null")
    private OrderStatus status;

    @NotNull(message = "Created timestamp cannot be null")
    private LocalDateTime createdAt;

    @NotNull(message = "Updated timestamp cannot be null")
    private LocalDateTime updatedAt;

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItem> items;

    @NotNull(message = "Total amount cannot be null")
    @DecimalMin(value = "0.01", message = "Total amount must be positive")
    private BigDecimal totalAmount;

    @NotBlank(message = "Delivery address cannot be blank")
    private String deliveryAddress;

    private String driverId;
    private String cancellationReason;
    private String rejectionReason;
    private String specialInstructions;
    private LocalDateTime estimatedDeliveryTime;
    private LocalDateTime actualDeliveryTime;

    /**
     * Default constructor for frameworks.
     */
    public Order() {
        this.orderId = generateOrderId();
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.items = new ArrayList<>();
    }

    /**
     * Constructor for creating a new order.
     * 
     * @param customerId the customer placing the order
     * @param restaurantId the restaurant fulfilling the order
     * @param items the list of items in the order
     * @param deliveryAddress the delivery address
     */
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

    /**
     * Transitions the order to a new status with validation and logging.
     * 
     * @param newStatus the new status to transition to
     * @param reason the reason for the status change
     * @throws InvalidOrderStateException if the transition is not valid
     */
    public void transitionTo(OrderStatus newStatus, String reason) throws InvalidOrderStateException {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new InvalidOrderStateException(this.orderId, this.status, newStatus);
        }
        
        OrderStatus oldStatus = this.status;
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
        
        // Handle special status-specific logic
        switch (newStatus) {
            case CANCELLED -> this.cancellationReason = reason;
            case REJECTED -> this.rejectionReason = reason;
            case DELIVERED -> this.actualDeliveryTime = LocalDateTime.now();
        }
        
        logger.info("Order status transition: orderId={}, from={}, to={}, reason={}", 
                   orderId, oldStatus, newStatus, reason);
    }

    /**
     * Accepts the order (transitions from PENDING to ACCEPTED).
     * 
     * @param reason optional reason for acceptance
     * @throws InvalidOrderStateException if the transition is not valid
     */
    public void accept(String reason) throws InvalidOrderStateException {
        transitionTo(OrderStatus.ACCEPTED, reason != null ? reason : "Order accepted by restaurant");
    }

    /**
     * Rejects the order (transitions from PENDING to REJECTED).
     * 
     * @param reason the reason for rejection
     * @throws InvalidOrderStateException if the transition is not valid
     */
    public void reject(String reason) throws InvalidOrderStateException {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        transitionTo(OrderStatus.REJECTED, reason);
    }

    /**
     * Cancels the order (can be done from most states).
     * 
     * @param reason the reason for cancellation
     * @throws InvalidOrderStateException if the transition is not valid
     */
    public void cancel(String reason) throws InvalidOrderStateException {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Cancellation reason is required");
        }
        transitionTo(OrderStatus.CANCELLED, reason);
    }

    /**
     * Marks the order as being prepared.
     * 
     * @throws InvalidOrderStateException if the transition is not valid
     */
    public void startPreparing() throws InvalidOrderStateException {
        transitionTo(OrderStatus.PREPARING, "Restaurant started preparing the order");
    }

    /**
     * Marks the order as ready for delivery.
     * 
     * @throws InvalidOrderStateException if the transition is not valid
     */
    public void markReadyForDelivery() throws InvalidOrderStateException {
        transitionTo(OrderStatus.READY_FOR_DELIVERY, "Order is ready for pickup by delivery driver");
    }

    /**
     * Assigns a driver and marks the order as in delivery.
     * 
     * @param driverId the ID of the assigned driver
     * @throws InvalidOrderStateException if the transition is not valid
     */
    public void assignDriver(String driverId) throws InvalidOrderStateException {
        if (driverId == null || driverId.trim().isEmpty()) {
            throw new IllegalArgumentException("Driver ID is required");
        }
        this.driverId = driverId;
        transitionTo(OrderStatus.IN_DELIVERY, "Assigned to driver: " + driverId);
    }

    /**
     * Marks the order as delivered.
     * 
     * @throws InvalidOrderStateException if the transition is not valid
     */
    public void markDelivered() throws InvalidOrderStateException {
        transitionTo(OrderStatus.DELIVERED, "Order delivered successfully");
    }

    /**
     * Calculates the total amount from all order items.
     * 
     * @return the total amount
     */
    private BigDecimal calculateTotalAmount() {
        return items.stream()
                   .map(OrderItem::getTotalPrice)
                   .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Recalculates and updates the total amount.
     */
    public void recalculateTotalAmount() {
        this.totalAmount = calculateTotalAmount();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Adds an item to the order and recalculates the total.
     * 
     * @param item the item to add
     * @throws IllegalStateException if the order cannot be modified
     */
    public void addItem(OrderItem item) {
        if (!canModifyItems()) {
            throw new IllegalStateException("Cannot modify items for order in status: " + status);
        }
        this.items.add(item);
        recalculateTotalAmount();
    }

    /**
     * Removes an item from the order and recalculates the total.
     * 
     * @param itemId the ID of the item to remove
     * @return true if the item was removed
     * @throws IllegalStateException if the order cannot be modified
     */
    public boolean removeItem(String itemId) {
        if (!canModifyItems()) {
            throw new IllegalStateException("Cannot modify items for order in status: " + status);
        }
        boolean removed = items.removeIf(item -> Objects.equals(item.getItemId(), itemId));
        if (removed) {
            recalculateTotalAmount();
        }
        return removed;
    }

    /**
     * Checks if items can be modified (only in PENDING status).
     * 
     * @return true if items can be modified
     */
    public boolean canModifyItems() {
        return status == OrderStatus.PENDING;
    }

    /**
     * Checks if the order is in a terminal state (no further changes allowed).
     * 
     * @return true if the order is in a terminal state
     */
    public boolean isTerminal() {
        return status.isTerminal();
    }

    /**
     * Gets the estimated delivery time, calculating it if not set.
     * 
     * @return the estimated delivery time
     */
    public LocalDateTime getEstimatedDeliveryTime() {
        if (estimatedDeliveryTime == null && status.ordinal() >= OrderStatus.ACCEPTED.ordinal()) {
            // Default estimation: 45 minutes from acceptance
            estimatedDeliveryTime = createdAt.plusMinutes(45);
        }
        return estimatedDeliveryTime;
    }

    /**
     * Generates a unique order ID.
     * 
     * @return a new order ID
     */
    private String generateOrderId() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // Getters and Setters

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<OrderItem> getItems() {
        return new ArrayList<>(items);
    }

    public void setItems(List<OrderItem> items) {
        this.items = new ArrayList<>(items);
        recalculateTotalAmount();
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getDriverId() {
        return driverId;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public void setEstimatedDeliveryTime(LocalDateTime estimatedDeliveryTime) {
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }

    public LocalDateTime getActualDeliveryTime() {
        return actualDeliveryTime;
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