package com.deliveryplatform.services;

import com.deliveryplatform.exceptions.InvalidOrderStateException;
import com.deliveryplatform.exceptions.RestaurantUnavailableException;
import com.deliveryplatform.models.Order;
import com.deliveryplatform.models.OrderStatus;
import com.deliveryplatform.models.Restaurant;
import com.deliveryplatform.repositories.OrderRepository;
import com.deliveryplatform.repositories.RestaurantRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Implementation of RestaurantService that handles restaurant operations for order management.
 * Includes proper validation, exception handling, and logging.
 */
@Service
public class RestaurantServiceImpl implements RestaurantService {

    private static final Logger logger = LogManager.getLogger(RestaurantServiceImpl.class);

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderLoggingService loggingService;

    @Autowired
    public RestaurantServiceImpl(OrderRepository orderRepository, 
                               RestaurantRepository restaurantRepository,
                               OrderLoggingService loggingService) {
        this.orderRepository = orderRepository;
        this.restaurantRepository = restaurantRepository;
        this.loggingService = loggingService;
    }

    @Override
    public void acceptOrder(String orderId, String restaurantId) 
            throws RestaurantUnavailableException, InvalidOrderStateException {
        
        loggingService.logMethodEntry("acceptOrder", orderId, restaurantId);
        
        try {
            // Validate inputs
            validateOrderAndRestaurantIds(orderId, restaurantId);
            
            // Get the restaurant and verify availability
            Restaurant restaurant = getRestaurantAndValidateAvailability(restaurantId);
            
            // Get the order and validate state
            Order order = getOrderAndValidateForAcceptance(orderId, restaurantId);
            
            // Accept the order
            order.accept("Restaurant " + restaurantId + " accepted the order");
            
            // Update restaurant capacity
            restaurant.acceptOrder();
            
            // Save changes
            orderRepository.save(order);
            restaurantRepository.save(restaurant);
            
            // Log success
            loggingService.logRestaurantOperation(restaurantId, "ACCEPT_ORDER", orderId, true);
            loggingService.logOrderStatusChange(orderId, OrderStatus.PENDING, OrderStatus.ACCEPTED, 
                                              "Accepted by restaurant");
            
            loggingService.logMethodExit("acceptOrder", "success");
            
        } catch (RestaurantUnavailableException | InvalidOrderStateException e) {
            loggingService.logBusinessException(e, orderId);
            loggingService.logRestaurantOperation(restaurantId, "ACCEPT_ORDER", orderId, false);
            throw e;
        } catch (Exception e) {
            loggingService.logSystemError(e, "acceptOrder", orderId);
            loggingService.logRestaurantOperation(restaurantId, "ACCEPT_ORDER", orderId, false);
            throw new RuntimeException("Failed to accept order due to system error", e);
        }
    }

    @Override
    public void rejectOrder(String orderId, String restaurantId, String reason) 
            throws InvalidOrderStateException {
        
        loggingService.logMethodEntry("rejectOrder", orderId, restaurantId, reason);
        
        try {
            // Validate inputs
            validateOrderAndRestaurantIds(orderId, restaurantId);
            if (reason == null || reason.trim().isEmpty()) {
                throw new IllegalArgumentException("Rejection reason is required");
            }
            
            // Verify restaurant exists
            if (!restaurantRepository.existsById(restaurantId)) {
                throw new IllegalArgumentException("Restaurant not found: " + restaurantId);
            }
            
            // Get the order and validate state
            Order order = getOrderAndValidateForRejection(orderId, restaurantId);
            
            // Reject the order
            order.reject(reason);
            
            // Save changes
            orderRepository.save(order);
            
            // Log success
            loggingService.logRestaurantOperation(restaurantId, "REJECT_ORDER", orderId, true);
            loggingService.logOrderStatusChange(orderId, OrderStatus.PENDING, OrderStatus.REJECTED, 
                                              "Rejected by restaurant: " + reason);
            
            loggingService.logMethodExit("rejectOrder", "success");
            
        } catch (InvalidOrderStateException e) {
            loggingService.logBusinessException(e, orderId);
            loggingService.logRestaurantOperation(restaurantId, "REJECT_ORDER", orderId, false);
            throw e;
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid arguments for order rejection: orderId={}, restaurantId={}, reason={}", 
                       orderId, restaurantId, reason);
            loggingService.logRestaurantOperation(restaurantId, "REJECT_ORDER", orderId, false);
            throw e;
        } catch (Exception e) {
            loggingService.logSystemError(e, "rejectOrder", orderId);
            loggingService.logRestaurantOperation(restaurantId, "REJECT_ORDER", orderId, false);
            throw new RuntimeException("Failed to reject order due to system error", e);
        }
    }

    @Override
    public void markOrderReady(String orderId, String restaurantId) 
            throws InvalidOrderStateException {
        
        loggingService.logMethodEntry("markOrderReady", orderId, restaurantId);
        
        try {
            // Validate inputs
            validateOrderAndRestaurantIds(orderId, restaurantId);
            
            // Get the order and validate state
            Order order = getOrderAndValidateForReadyMarking(orderId, restaurantId);
            
            // Mark order ready for delivery
            order.markReadyForDelivery();
            
            // Save changes
            orderRepository.save(order);
            
            // Log success
            loggingService.logRestaurantOperation(restaurantId, "MARK_ORDER_READY", orderId, true);
            loggingService.logOrderStatusChange(orderId, OrderStatus.PREPARING, OrderStatus.READY_FOR_DELIVERY, 
                                              "Marked ready by restaurant");
            
            loggingService.logMethodExit("markOrderReady", "success");
            
        } catch (InvalidOrderStateException e) {
            loggingService.logBusinessException(e, orderId);
            loggingService.logRestaurantOperation(restaurantId, "MARK_ORDER_READY", orderId, false);
            throw e;
        } catch (Exception e) {
            loggingService.logSystemError(e, "markOrderReady", orderId);
            loggingService.logRestaurantOperation(restaurantId, "MARK_ORDER_READY", orderId, false);
            throw new RuntimeException("Failed to mark order ready due to system error", e);
        }
    }

    @Override
    public void startPreparingOrder(String orderId, String restaurantId) 
            throws InvalidOrderStateException {
        
        loggingService.logMethodEntry("startPreparingOrder", orderId, restaurantId);
        
        try {
            // Validate inputs
            validateOrderAndRestaurantIds(orderId, restaurantId);
            
            // Get the order and validate state
            Order order = getOrderAndValidateForPreparation(orderId, restaurantId);
            
            // Start preparing the order
            order.startPreparing();
            
            // Save changes
            orderRepository.save(order);
            
            // Log success
            loggingService.logRestaurantOperation(restaurantId, "START_PREPARING", orderId, true);
            loggingService.logOrderStatusChange(orderId, OrderStatus.ACCEPTED, OrderStatus.PREPARING, 
                                              "Preparation started by restaurant");
            
            loggingService.logMethodExit("startPreparingOrder", "success");
            
        } catch (InvalidOrderStateException e) {
            loggingService.logBusinessException(e, orderId);
            loggingService.logRestaurantOperation(restaurantId, "START_PREPARING", orderId, false);
            throw e;
        } catch (Exception e) {
            loggingService.logSystemError(e, "startPreparingOrder", orderId);
            loggingService.logRestaurantOperation(restaurantId, "START_PREPARING", orderId, false);
            throw new RuntimeException("Failed to start preparing order due to system error", e);
        }
    }

    @Override
    public boolean canAcceptOrders(String restaurantId) {
        try {
            Optional<Restaurant> restaurantOpt = restaurantRepository.findById(restaurantId);
            return restaurantOpt.map(Restaurant::canAcceptOrder).orElse(false);
        } catch (Exception e) {
            loggingService.logSystemError(e, "canAcceptOrders", restaurantId);
            return false;
        }
    }

    @Override
    public int getRemainingCapacity(String restaurantId) {
        try {
            Optional<Restaurant> restaurantOpt = restaurantRepository.findById(restaurantId);
            return restaurantOpt.map(Restaurant::getRemainingCapacity).orElse(0);
        } catch (Exception e) {
            loggingService.logSystemError(e, "getRemainingCapacity", restaurantId);
            return 0;
        }
    }

    @Override
    public void disableOrderAcceptance(String restaurantId, String reason) {
        try {
            Optional<Restaurant> restaurantOpt = restaurantRepository.findById(restaurantId);
            if (restaurantOpt.isPresent()) {
                Restaurant restaurant = restaurantOpt.get();
                restaurant.stopAcceptingOrders();
                restaurantRepository.save(restaurant);
                
                logger.info("Order acceptance disabled for restaurant: restaurantId={}, reason={}", 
                           restaurantId, reason);
            } else {
                logger.warn("Attempted to disable order acceptance for non-existent restaurant: {}", restaurantId);
            }
        } catch (Exception e) {
            loggingService.logSystemError(e, "disableOrderAcceptance", restaurantId);
        }
    }

    @Override
    public void enableOrderAcceptance(String restaurantId) {
        try {
            Optional<Restaurant> restaurantOpt = restaurantRepository.findById(restaurantId);
            if (restaurantOpt.isPresent()) {
                Restaurant restaurant = restaurantOpt.get();
                restaurant.startAcceptingOrders();
                restaurantRepository.save(restaurant);
                
                logger.info("Order acceptance enabled for restaurant: restaurantId={}", restaurantId);
            } else {
                logger.warn("Attempted to enable order acceptance for non-existent restaurant: {}", restaurantId);
            }
        } catch (Exception e) {
            loggingService.logSystemError(e, "enableOrderAcceptance", restaurantId);
        }
    }

    // Private helper methods

    private void validateOrderAndRestaurantIds(String orderId, String restaurantId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
        if (restaurantId == null || restaurantId.trim().isEmpty()) {
            throw new IllegalArgumentException("Restaurant ID cannot be null or empty");
        }
    }

    private Restaurant getRestaurantAndValidateAvailability(String restaurantId) 
            throws RestaurantUnavailableException {
        
        Optional<Restaurant> restaurantOpt = restaurantRepository.findById(restaurantId);
        if (restaurantOpt.isEmpty()) {
            throw new RestaurantUnavailableException(restaurantId, "Restaurant not found");
        }
        
        Restaurant restaurant = restaurantOpt.get();
        if (!restaurant.canAcceptOrder()) {
            String reason = buildUnavailabilityReason(restaurant);
            throw new RestaurantUnavailableException(restaurantId, reason);
        }
        
        return restaurant;
    }

    private String buildUnavailabilityReason(Restaurant restaurant) {
        if (!restaurant.isOpen()) {
            return "Restaurant is closed";
        }
        if (!restaurant.isWithinOperatingHours()) {
            return "Outside operating hours";
        }
        if (!restaurant.isAcceptingOrders()) {
            return "Temporarily not accepting orders";
        }
        if (restaurant.isAtCapacity()) {
            return "At maximum capacity (" + restaurant.getMaxConcurrentOrders() + " orders)";
        }
        return "Unknown availability issue";
    }

    private Order getOrderAndValidateForAcceptance(String orderId, String restaurantId) 
            throws InvalidOrderStateException {
        
        Order order = getOrderAndValidateOwnership(orderId, restaurantId);
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStateException(orderId, order.getStatus(), OrderStatus.ACCEPTED,
                                               "Order must be in PENDING status to be accepted");
        }
        
        return order;
    }

    private Order getOrderAndValidateForRejection(String orderId, String restaurantId) 
            throws InvalidOrderStateException {
        
        Order order = getOrderAndValidateOwnership(orderId, restaurantId);
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStateException(orderId, order.getStatus(), OrderStatus.REJECTED,
                                               "Order must be in PENDING status to be rejected");
        }
        
        return order;
    }

    private Order getOrderAndValidateForPreparation(String orderId, String restaurantId) 
            throws InvalidOrderStateException {
        
        Order order = getOrderAndValidateOwnership(orderId, restaurantId);
        
        if (order.getStatus() != OrderStatus.ACCEPTED) {
            throw new InvalidOrderStateException(orderId, order.getStatus(), OrderStatus.PREPARING,
                                               "Order must be in ACCEPTED status to start preparation");
        }
        
        return order;
    }

    private Order getOrderAndValidateForReadyMarking(String orderId, String restaurantId) 
            throws InvalidOrderStateException {
        
        Order order = getOrderAndValidateOwnership(orderId, restaurantId);
        
        if (order.getStatus() != OrderStatus.PREPARING) {
            throw new InvalidOrderStateException(orderId, order.getStatus(), OrderStatus.READY_FOR_DELIVERY,
                                               "Order must be in PREPARING status to be marked ready");
        }
        
        return order;
    }

    private Order getOrderAndValidateOwnership(String orderId, String restaurantId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        
        Order order = orderOpt.get();
        if (!restaurantId.equals(order.getRestaurantId())) {
            throw new IllegalArgumentException(
                String.format("Order %s does not belong to restaurant %s", orderId, restaurantId));
        }
        
        return order;
    }
}