package com.deliveryplatform.services;

import com.deliveryplatform.exceptions.InvalidOrderStateException;
import com.deliveryplatform.exceptions.RestaurantUnavailableException;

/**
 * Service interface for restaurant operations related to order management.
 * Defines the contract for restaurant interactions with orders.
 */
public interface RestaurantService {

    /**
     * Accepts an order on behalf of a restaurant.
     * 
     * @param orderId the ID of the order to accept
     * @param restaurantId the ID of the restaurant accepting the order
     * @throws RestaurantUnavailableException if the restaurant cannot accept orders
     * @throws InvalidOrderStateException if the order is not in the correct state for acceptance
     */
    void acceptOrder(String orderId, String restaurantId) 
            throws RestaurantUnavailableException, InvalidOrderStateException;

    /**
     * Rejects an order with a reason.
     * 
     * @param orderId the ID of the order to reject
     * @param restaurantId the ID of the restaurant rejecting the order
     * @param reason the reason for rejection
     * @throws InvalidOrderStateException if the order is not in the correct state for rejection
     */
    void rejectOrder(String orderId, String restaurantId, String reason) 
            throws InvalidOrderStateException;

    /**
     * Marks an order as ready for delivery.
     * 
     * @param orderId the ID of the order to mark as ready
     * @param restaurantId the ID of the restaurant marking the order ready
     * @throws InvalidOrderStateException if the order is not in the correct state
     */
    void markOrderReady(String orderId, String restaurantId) 
            throws InvalidOrderStateException;

    /**
     * Starts preparing an order.
     * 
     * @param orderId the ID of the order to start preparing
     * @param restaurantId the ID of the restaurant preparing the order
     * @throws InvalidOrderStateException if the order is not in the correct state
     */
    void startPreparingOrder(String orderId, String restaurantId) 
            throws InvalidOrderStateException;

    /**
     * Checks if a restaurant can accept orders.
     * 
     * @param restaurantId the ID of the restaurant to check
     * @return true if the restaurant can accept orders
     */
    boolean canAcceptOrders(String restaurantId);

    /**
     * Gets the current capacity information for a restaurant.
     * 
     * @param restaurantId the ID of the restaurant
     * @return the remaining capacity for new orders
     */
    int getRemainingCapacity(String restaurantId);

    /**
     * Temporarily disables order acceptance for a restaurant.
     * 
     * @param restaurantId the ID of the restaurant
     * @param reason the reason for disabling
     */
    void disableOrderAcceptance(String restaurantId, String reason);

    /**
     * Enables order acceptance for a restaurant.
     * 
     * @param restaurantId the ID of the restaurant
     */
    void enableOrderAcceptance(String restaurantId);
}