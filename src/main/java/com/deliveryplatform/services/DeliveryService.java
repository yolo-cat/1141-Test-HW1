package com.deliveryplatform.services;

import com.deliveryplatform.exceptions.DeliveryAssignmentException;
import com.deliveryplatform.exceptions.InvalidOrderStateException;
import com.deliveryplatform.models.Order;
import com.deliveryplatform.models.OrderStatus;
import com.deliveryplatform.repositories.OrderRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing delivery operations including driver assignment and delivery completion.
 */
@Service
public class DeliveryService {

    private static final Logger logger = LogManager.getLogger(DeliveryService.class);
    private static final int MAX_DRIVER_CAPACITY = 3; // Maximum orders per driver

    private final OrderRepository orderRepository;
    private final OrderLoggingService loggingService;
    
    // Simple in-memory storage for available drivers and their current loads
    private final Set<String> availableDrivers = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<String, Integer> driverOrderCount = new ConcurrentHashMap<>();

    @Autowired
    public DeliveryService(OrderRepository orderRepository, OrderLoggingService loggingService) {
        this.orderRepository = orderRepository;
        this.loggingService = loggingService;
        
        // Initialize with some test drivers
        initializeTestDrivers();
    }

    /**
     * Assigns an available driver to an order.
     * 
     * @param orderId the order ID to assign a driver to
     * @return the assigned driver ID
     * @throws DeliveryAssignmentException if no drivers are available or assignment fails
     * @throws InvalidOrderStateException if the order is not ready for delivery
     */
    public String assignDriver(String orderId) throws DeliveryAssignmentException, InvalidOrderStateException {
        
        loggingService.logMethodEntry("assignDriver", orderId);
        
        try {
            // Validate input
            if (orderId == null || orderId.trim().isEmpty()) {
                throw new IllegalArgumentException("Order ID cannot be null or empty");
            }
            
            // Get the order and validate state
            Order order = getOrderAndValidateForDelivery(orderId);
            
            // Find an available driver
            String driverId = findAvailableDriver();
            if (driverId == null) {
                loggingService.logDeliveryOperation(orderId, null, "DRIVER_ASSIGNMENT", false);
                throw DeliveryAssignmentException.noDriversAvailable(orderId);
            }
            
            // Assign driver to order
            order.assignDriver(driverId);
            
            // Update driver availability
            updateDriverAssignment(driverId);
            
            // Save the order
            orderRepository.save(order);
            
            // Log successful assignment
            loggingService.logDeliveryOperation(orderId, driverId, "DRIVER_ASSIGNMENT", true);
            loggingService.logOrderStatusChange(orderId, OrderStatus.READY_FOR_DELIVERY, OrderStatus.IN_DELIVERY,
                                              "Assigned to driver " + driverId);
            
            logger.info("Driver assigned successfully: orderId={}, driverId={}", orderId, driverId);
            loggingService.logMethodExit("assignDriver", driverId);
            
            return driverId;
            
        } catch (DeliveryAssignmentException | InvalidOrderStateException e) {
            loggingService.logBusinessException(e, orderId);
            throw e;
        } catch (Exception e) {
            loggingService.logSystemError(e, "assignDriver", orderId);
            throw new RuntimeException("Failed to assign driver due to system error", e);
        }
    }

    /**
     * Marks an order as delivered by the assigned driver.
     * 
     * @param orderId the order ID to mark as delivered
     * @param driverId the driver completing the delivery
     * @throws InvalidOrderStateException if the order is not in delivery state
     */
    public void completeDelivery(String orderId, String driverId) throws InvalidOrderStateException {
        
        loggingService.logMethodEntry("completeDelivery", orderId, driverId);
        
        try {
            // Validate inputs
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
            
            // Release driver capacity
            releaseDriverCapacity(driverId);
            
            // Save the order
            orderRepository.save(order);
            
            // Log successful delivery
            loggingService.logDeliveryOperation(orderId, driverId, "DELIVERY_COMPLETION", true);
            loggingService.logOrderStatusChange(orderId, OrderStatus.IN_DELIVERY, OrderStatus.DELIVERED,
                                              "Delivered by driver " + driverId);
            loggingService.logFinancialEvent(orderId, order.getTotalAmount(), "ORDER_DELIVERED");
            
            logger.info("Delivery completed successfully: orderId={}, driverId={}", orderId, driverId);
            loggingService.logMethodExit("completeDelivery", "success");
            
        } catch (InvalidOrderStateException e) {
            loggingService.logBusinessException(e, orderId);
            loggingService.logDeliveryOperation(orderId, driverId, "DELIVERY_COMPLETION", false);
            throw e;
        } catch (Exception e) {
            loggingService.logSystemError(e, "completeDelivery", orderId);
            loggingService.logDeliveryOperation(orderId, driverId, "DELIVERY_COMPLETION", false);
            throw new RuntimeException("Failed to complete delivery due to system error", e);
        }
    }

    /**
     * Gets all orders ready for delivery assignment.
     * 
     * @return list of orders ready for delivery
     */
    public List<Order> getOrdersReadyForDelivery() {
        try {
            return orderRepository.findByStatus(OrderStatus.READY_FOR_DELIVERY);
        } catch (Exception e) {
            loggingService.logSystemError(e, "getOrdersReadyForDelivery");
            return List.of();
        }
    }

    /**
     * Gets all orders currently in delivery.
     * 
     * @return list of orders in delivery
     */
    public List<Order> getOrdersInDelivery() {
        try {
            return orderRepository.findByStatus(OrderStatus.IN_DELIVERY);
        } catch (Exception e) {
            loggingService.logSystemError(e, "getOrdersInDelivery");
            return List.of();
        }
    }

    /**
     * Gets orders assigned to a specific driver.
     * 
     * @param driverId the driver ID
     * @return list of orders assigned to the driver
     */
    public List<Order> getOrdersByDriver(String driverId) {
        try {
            return orderRepository.findByStatus(OrderStatus.IN_DELIVERY)
                    .stream()
                    .filter(order -> driverId.equals(order.getDriverId()))
                    .toList();
        } catch (Exception e) {
            loggingService.logSystemError(e, "getOrdersByDriver", driverId);
            return List.of();
        }
    }

    /**
     * Adds a driver to the available drivers pool.
     * 
     * @param driverId the driver ID to add
     */
    public void addDriver(String driverId) {
        if (driverId != null && !driverId.trim().isEmpty()) {
            availableDrivers.add(driverId);
            driverOrderCount.put(driverId, 0);
            logger.info("Driver added to available pool: driverId={}", driverId);
        }
    }

    /**
     * Removes a driver from the available drivers pool.
     * 
     * @param driverId the driver ID to remove
     */
    public void removeDriver(String driverId) {
        if (driverId != null) {
            availableDrivers.remove(driverId);
            driverOrderCount.remove(driverId);
            logger.info("Driver removed from available pool: driverId={}", driverId);
        }
    }

    /**
     * Gets the count of available drivers.
     * 
     * @return number of available drivers
     */
    public int getAvailableDriverCount() {
        return (int) availableDrivers.stream()
                .filter(this::isDriverAvailableForNewOrder)
                .count();
    }

    /**
     * Gets the current order count for a driver.
     * 
     * @param driverId the driver ID
     * @return current order count for the driver
     */
    public int getDriverOrderCount(String driverId) {
        return driverOrderCount.getOrDefault(driverId, 0);
    }

    // Private helper methods

    private void initializeTestDrivers() {
        // Add some test drivers
        addDriver("DRIVER-001");
        addDriver("DRIVER-002");
        addDriver("DRIVER-003");
        addDriver("DRIVER-004");
        addDriver("DRIVER-005");
    }

    private Order getOrderAndValidateForDelivery(String orderId) throws InvalidOrderStateException {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        
        Order order = orderOpt.get();
        if (order.getStatus() != OrderStatus.READY_FOR_DELIVERY) {
            throw new InvalidOrderStateException(orderId, order.getStatus(), OrderStatus.IN_DELIVERY,
                                               "Order must be ready for delivery to assign a driver");
        }
        
        return order;
    }

    private Order getOrderAndValidateForCompletion(String orderId, String driverId) throws InvalidOrderStateException {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        
        Order order = orderOpt.get();
        
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
        return availableDrivers.stream()
                .filter(this::isDriverAvailableForNewOrder)
                .findFirst()
                .orElse(null);
    }

    private boolean isDriverAvailableForNewOrder(String driverId) {
        int currentOrders = driverOrderCount.getOrDefault(driverId, 0);
        return currentOrders < MAX_DRIVER_CAPACITY;
    }

    private void updateDriverAssignment(String driverId) {
        int currentCount = driverOrderCount.getOrDefault(driverId, 0);
        driverOrderCount.put(driverId, currentCount + 1);
        
        if (currentCount + 1 >= MAX_DRIVER_CAPACITY) {
            logger.info("Driver at capacity: driverId={}, orderCount={}", driverId, currentCount + 1);
        }
    }

    private void releaseDriverCapacity(String driverId) {
        int currentCount = driverOrderCount.getOrDefault(driverId, 0);
        if (currentCount > 0) {
            driverOrderCount.put(driverId, currentCount - 1);
            logger.info("Driver capacity released: driverId={}, remainingOrders={}", driverId, currentCount - 1);
        }
    }
}