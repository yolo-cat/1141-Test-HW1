package com.deliveryplatform.services;

import com.deliveryplatform.exceptions.DeliveryAssignmentException;
import com.deliveryplatform.exceptions.InvalidOrderStateException;
import com.deliveryplatform.models.Order;
import com.deliveryplatform.models.OrderStatus;
import com.deliveryplatform.repositories.OrderRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing delivery operations - simplified version.
 */
public class DeliveryService {

    private static final Logger logger = LogManager.getLogger(DeliveryService.class);
    
    private final OrderRepository orderRepository;
    private final Set<String> availableDrivers = ConcurrentHashMap.newKeySet();

    public DeliveryService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
        initializeTestDrivers();
    }

    /**
     * Assigns an available driver to an order.
     */
    public String assignDriver(String orderId) throws DeliveryAssignmentException, InvalidOrderStateException {
        
        logger.info("Attempting to assign driver for order: {}", orderId);
        
        try {
            if (orderId == null || orderId.trim().isEmpty()) {
                throw new IllegalArgumentException("Order ID cannot be null or empty");
            }
            
            // Get the order and validate state
            Order order = getOrderAndValidateForDelivery(orderId);
            
            // Find an available driver (simplified - just get first available)
            String driverId = findAvailableDriver();
            if (driverId == null) {
                throw DeliveryAssignmentException.noDriversAvailable(orderId);
            }
            
            // Assign driver to order
            order.assignDriver(driverId);
            order.updateStatus(OrderStatus.IN_DELIVERY);
            
            // Save the order
            orderRepository.save(order);
            
            logger.info("Driver assigned successfully: orderId={}, driverId={}", orderId, driverId);
            
            return driverId;
            
        } catch (DeliveryAssignmentException | InvalidOrderStateException e) {
            logger.warn("Business exception in assignDriver: orderId={}, error={}", orderId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("System error in assignDriver: orderId={}", orderId, e);
            throw new RuntimeException("Failed to assign driver due to system error", e);
        }
    }

    /**
     * Marks an order as delivered by the assigned driver.
     */
    public void completeDelivery(String orderId, String driverId) throws InvalidOrderStateException {
        
        logger.info("Completing delivery: orderId={}, driverId={}", orderId, driverId);
        
        try {
            if (orderId == null || orderId.trim().isEmpty()) {
                throw new IllegalArgumentException("Order ID cannot be null or empty");
            }
            if (driverId == null || driverId.trim().isEmpty()) {
                throw new IllegalArgumentException("Driver ID cannot be null or empty");
            }
            
            // Get the order and validate state
            Order order = getOrderAndValidateForCompletion(orderId, driverId);
            
            // Mark order as delivered
            order.markDelivered();
            
            // Save the order
            orderRepository.save(order);
            
            logger.info("Delivery completed successfully: orderId={}, driverId={}", orderId, driverId);
            
        } catch (InvalidOrderStateException e) {
            logger.warn("Business exception in completeDelivery: orderId={}, driverId={}, error={}", 
                       orderId, driverId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("System error in completeDelivery: orderId={}, driverId={}", orderId, driverId, e);
            throw new RuntimeException("Failed to complete delivery due to system error", e);
        }
    }

    /**
     * Adds a driver to the available drivers pool.
     */
    public void addDriver(String driverId) {
        if (driverId != null && !driverId.trim().isEmpty()) {
            availableDrivers.add(driverId);
            logger.info("Driver added to available pool: driverId={}", driverId);
        }
    }

    /**
     * Gets the count of available drivers.
     */
    public int getAvailableDriverCount() {
        return availableDrivers.size();
    }

    /**
     * Removes a driver from the available drivers pool.
     */
    public void removeDriver(String driverId) {
        if (driverId != null) {
            availableDrivers.remove(driverId);
            logger.info("Driver removed from available pool: driverId={}", driverId);
        }
    }

    // Private helper methods

    private void initializeTestDrivers() {
        addDriver("DRIVER-001");
        addDriver("DRIVER-002");
        addDriver("DRIVER-003");
        addDriver("DRIVER-004");
        addDriver("DRIVER-005");
    }

    private Order getOrderAndValidateForDelivery(String orderId) throws InvalidOrderStateException {
        return orderRepository.findById(orderId)
            .filter(order -> order.getStatus() == OrderStatus.READY_FOR_DELIVERY)
            .orElseThrow(() -> new InvalidOrderStateException(orderId, null, OrderStatus.IN_DELIVERY,
                "Order must be ready for delivery to assign a driver"));
    }

    private Order getOrderAndValidateForCompletion(String orderId, String driverId) throws InvalidOrderStateException {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        
        if (order.getStatus() != OrderStatus.IN_DELIVERY) {
            throw new InvalidOrderStateException(orderId, order.getStatus(), OrderStatus.DELIVERED,
                "Order must be in delivery to be completed");
        }
        
        if (!driverId.equals(order.getDriverId())) {
            throw new IllegalArgumentException(
                String.format("Order %s is not assigned to driver %s", orderId, driverId));
        }
        
        return order;
    }

    private String findAvailableDriver() {
        return availableDrivers.isEmpty() ? null : availableDrivers.iterator().next();
    }
}