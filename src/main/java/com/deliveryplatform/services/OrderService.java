package com.deliveryplatform.services;

import com.deliveryplatform.exceptions.InvalidOrderStateException;
import com.deliveryplatform.exceptions.OrderValidationException;
import com.deliveryplatform.models.Order;
import com.deliveryplatform.models.OrderItem;
import com.deliveryplatform.models.OrderStatus;
import com.deliveryplatform.repositories.OrderRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing customer order operations - simplified version.
 */
public class OrderService {

    private static final Logger logger = LogManager.getLogger(OrderService.class);

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Creates a new order with validation.
     */
    public Order createOrder(String customerId, String restaurantId, List<OrderItem> items, String deliveryAddress) 
            throws OrderValidationException {
        
        logger.info("Creating order: customerId={}, restaurantId={}, itemCount={}", 
                   customerId, restaurantId, items.size());
        
        try {
            // Basic validation
            validateOrderData(customerId, restaurantId, items, deliveryAddress);
            
            // Create the order
            Order order = new Order(customerId, restaurantId, items, deliveryAddress);
            
            // Save the order
            Order savedOrder = orderRepository.save(order);
            
            logger.info("Order created successfully: orderId={}, amount={}", 
                       savedOrder.getOrderId(), savedOrder.getTotalAmount());
            
            return savedOrder;
            
        } catch (OrderValidationException e) {
            logger.warn("Order validation failed: customerId={}, restaurantId={}, errors={}", 
                       customerId, restaurantId, e.getValidationErrors());
            throw e;
        } catch (Exception e) {
            logger.error("System error in createOrder: customerId={}, restaurantId={}", customerId, restaurantId, e);
            throw new RuntimeException("Failed to create order due to system error", e);
        }
    }

    /**
     * Retrieves an order by its ID.
     */
    public Optional<Order> getOrder(String orderId) {
        return orderRepository.findById(orderId);
    }

    /**
     * Cancels an order if it's in a cancellable state.
     */
    public void cancelOrder(String orderId, String reason) throws InvalidOrderStateException {
        
        logger.info("Attempting to cancel order: orderId={}, reason={}", orderId, reason);
        
        try {
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
            
            // Cancel the order
            order.cancel(reason);
            
            // Save the updated order
            orderRepository.save(order);
            
            logger.info("Order cancelled successfully: orderId={}", orderId);
            
        } catch (InvalidOrderStateException e) {
            logger.warn("Cannot cancel order: orderId={}, currentStatus={}, error={}", 
                       orderId, e.getCurrentStatus(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("System error in cancelOrder: orderId={}", orderId, e);
            throw new RuntimeException("Failed to cancel order due to system error", e);
        }
    }

    // Private validation method
    private void validateOrderData(String customerId, String restaurantId, List<OrderItem> items, String deliveryAddress) 
            throws OrderValidationException {
        
        List<String> errors = new java.util.ArrayList<>();
        
        if (customerId == null || customerId.trim().isEmpty()) {
            errors.add("Customer ID is required");
        }
        
        if (restaurantId == null || restaurantId.trim().isEmpty()) {
            errors.add("Restaurant ID is required");
        }
        
        if (deliveryAddress == null || deliveryAddress.trim().isEmpty()) {
            errors.add("Delivery address is required");
        }
        
        if (items == null || items.isEmpty()) {
            errors.add("Order must have at least one item");
        } else {
            // Basic item validation
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
                if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                    errors.add(String.format("Item %d: Unit price must be positive", i + 1));
                }
            }
        }
        
        if (!errors.isEmpty()) {
            throw new OrderValidationException(errors);
        }
    }
}