package com.deliveryplatform.services;

import com.deliveryplatform.exceptions.InvalidOrderStateException;
import com.deliveryplatform.exceptions.OrderValidationException;
import com.deliveryplatform.models.Order;
import com.deliveryplatform.models.OrderItem;
import com.deliveryplatform.models.OrderStatus;
import com.deliveryplatform.repositories.OrderRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing customer order operations including creation, validation, and lifecycle management.
 */
@Service
public class OrderService {

    private static final Logger logger = LogManager.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderLoggingService loggingService;

    @Autowired
    public OrderService(OrderRepository orderRepository, OrderLoggingService loggingService) {
        this.orderRepository = orderRepository;
        this.loggingService = loggingService;
    }

    /**
     * Creates a new order with validation.
     * 
     * @param customerId the customer placing the order
     * @param restaurantId the restaurant fulfilling the order
     * @param items the list of items in the order
     * @param deliveryAddress the delivery address
     * @return the created order
     * @throws OrderValidationException if the order data is invalid
     */
    public Order createOrder(String customerId, String restaurantId, List<OrderItem> items, String deliveryAddress) 
            throws OrderValidationException {
        
        loggingService.logMethodEntry("createOrder", customerId, restaurantId, items.size(), deliveryAddress);
        
        try {
            // Validate order data
            validateOrderData(customerId, restaurantId, items, deliveryAddress);
            
            // Create the order
            Order order = new Order(customerId, restaurantId, items, deliveryAddress);
            
            // Save the order
            Order savedOrder = orderRepository.save(order);
            
            // Log order creation
            loggingService.logOrderCreated(savedOrder);
            loggingService.logFinancialEvent(savedOrder.getOrderId(), savedOrder.getTotalAmount(), "ORDER_CREATED");
            
            loggingService.logMethodExit("createOrder", savedOrder.getOrderId());
            
            return savedOrder;
            
        } catch (OrderValidationException e) {
            loggingService.logBusinessException(e, null);
            throw e;
        } catch (Exception e) {
            loggingService.logSystemError(e, "createOrder");
            throw new RuntimeException("Failed to create order due to system error", e);
        }
    }

    /**
     * Retrieves an order by its ID.
     * 
     * @param orderId the order ID
     * @return the order if found
     */
    public Optional<Order> getOrder(String orderId) {
        try {
            return orderRepository.findById(orderId);
        } catch (Exception e) {
            loggingService.logSystemError(e, "getOrder", orderId);
            return Optional.empty();
        }
    }

    /**
     * Gets all orders for a specific customer.
     * 
     * @param customerId the customer ID
     * @return list of customer orders
     */
    public List<Order> getOrdersByCustomer(String customerId) {
        try {
            return orderRepository.findByCustomerId(customerId);
        } catch (Exception e) {
            loggingService.logSystemError(e, "getOrdersByCustomer", customerId);
            return List.of();
        }
    }

    /**
     * Gets all orders for a specific restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @return list of restaurant orders
     */
    public List<Order> getOrdersByRestaurant(String restaurantId) {
        try {
            return orderRepository.findByRestaurantId(restaurantId);
        } catch (Exception e) {
            loggingService.logSystemError(e, "getOrdersByRestaurant", restaurantId);
            return List.of();
        }
    }

    /**
     * Gets orders by status.
     * 
     * @param status the order status
     * @return list of orders with the specified status
     */
    public List<Order> getOrdersByStatus(OrderStatus status) {
        try {
            return orderRepository.findByStatus(status);
        } catch (Exception e) {
            loggingService.logSystemError(e, "getOrdersByStatus");
            return List.of();
        }
    }

    /**
     * Cancels an order with a reason.
     * 
     * @param orderId the order ID to cancel
     * @param reason the cancellation reason
     * @throws InvalidOrderStateException if the order cannot be cancelled
     */
    public void cancelOrder(String orderId, String reason) throws InvalidOrderStateException {
        
        loggingService.logMethodEntry("cancelOrder", orderId, reason);
        
        try {
            // Validate inputs
            if (orderId == null || orderId.trim().isEmpty()) {
                throw new IllegalArgumentException("Order ID cannot be null or empty");
            }
            if (reason == null || reason.trim().isEmpty()) {
                throw new IllegalArgumentException("Cancellation reason is required");
            }
            
            // Get the order
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                throw new IllegalArgumentException("Order not found: " + orderId);
            }
            
            Order order = orderOpt.get();
            OrderStatus oldStatus = order.getStatus();
            
            // Cancel the order
            order.cancel(reason);
            
            // Save the updated order
            orderRepository.save(order);
            
            // Log the cancellation
            loggingService.logOrderStatusChange(orderId, oldStatus, OrderStatus.CANCELLED, reason);
            loggingService.logFinancialEvent(orderId, order.getTotalAmount(), "ORDER_CANCELLED");
            
            logger.info("Order cancelled: orderId={}, reason={}", orderId, reason);
            loggingService.logMethodExit("cancelOrder", "success");
            
        } catch (InvalidOrderStateException e) {
            loggingService.logBusinessException(e, orderId);
            throw e;
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid arguments for order cancellation: orderId={}, reason={}", orderId, reason);
            throw e;
        } catch (Exception e) {
            loggingService.logSystemError(e, "cancelOrder", orderId);
            throw new RuntimeException("Failed to cancel order due to system error", e);
        }
    }

    /**
     * Updates the special instructions for an order (only if in PENDING state).
     * 
     * @param orderId the order ID
     * @param specialInstructions the special instructions
     * @throws InvalidOrderStateException if the order cannot be modified
     */
    public void updateSpecialInstructions(String orderId, String specialInstructions) 
            throws InvalidOrderStateException {
        
        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                throw new IllegalArgumentException("Order not found: " + orderId);
            }
            
            Order order = orderOpt.get();
            
            // Check if order can be modified
            if (order.getStatus() != OrderStatus.PENDING) {
                throw new InvalidOrderStateException(orderId, order.getStatus(), order.getStatus(),
                                                   "Special instructions can only be updated for pending orders");
            }
            
            order.setSpecialInstructions(specialInstructions);
            orderRepository.save(order);
            
            logger.info("Special instructions updated for order: orderId={}", orderId);
            
        } catch (InvalidOrderStateException e) {
            loggingService.logBusinessException(e, orderId);
            throw e;
        } catch (Exception e) {
            loggingService.logSystemError(e, "updateSpecialInstructions", orderId);
            throw new RuntimeException("Failed to update special instructions due to system error", e);
        }
    }

    /**
     * Validates order data before creation.
     * 
     * @param customerId the customer ID
     * @param restaurantId the restaurant ID
     * @param items the order items
     * @param deliveryAddress the delivery address
     * @throws OrderValidationException if validation fails
     */
    private void validateOrderData(String customerId, String restaurantId, List<OrderItem> items, String deliveryAddress) 
            throws OrderValidationException {
        
        List<String> errors = new java.util.ArrayList<>();
        
        // Validate customer ID
        if (customerId == null || customerId.trim().isEmpty()) {
            errors.add("Customer ID is required");
        }
        
        // Validate restaurant ID
        if (restaurantId == null || restaurantId.trim().isEmpty()) {
            errors.add("Restaurant ID is required");
        }
        
        // Validate delivery address
        if (deliveryAddress == null || deliveryAddress.trim().isEmpty()) {
            errors.add("Delivery address is required");
        } else if (deliveryAddress.length() > 500) {
            errors.add("Delivery address cannot exceed 500 characters");
        }
        
        // Validate items
        if (items == null || items.isEmpty()) {
            errors.add("Order must have at least one item");
        } else {
            validateOrderItems(items, errors);
        }
        
        if (!errors.isEmpty()) {
            throw new OrderValidationException(errors);
        }
    }

    /**
     * Validates individual order items.
     * 
     * @param items the items to validate
     * @param errors the list to add validation errors to
     */
    private void validateOrderItems(List<OrderItem> items, List<String> errors) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);
            
            if (item.getItemId() == null || item.getItemId().trim().isEmpty()) {
                errors.add(String.format("Item %d: Item ID is required", i + 1));
            }
            
            if (item.getName() == null || item.getName().trim().isEmpty()) {
                errors.add(String.format("Item %d: Item name is required", i + 1));
            }
            
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                errors.add(String.format("Item %d: Quantity must be positive", i + 1));
            }
            
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                errors.add(String.format("Item %d: Unit price must be positive", i + 1));
            }
            
            if (item.getTotalPrice() != null) {
                totalAmount = totalAmount.add(item.getTotalPrice());
            }
        }
        
        // Check if total amount is reasonable
        if (totalAmount.compareTo(new BigDecimal("10000")) > 0) {
            errors.add("Order total exceeds maximum allowed amount ($10,000)");
        }
        
        if (totalAmount.compareTo(new BigDecimal("0.01")) < 0) {
            errors.add("Order total must be at least $0.01");
        }
    }
}