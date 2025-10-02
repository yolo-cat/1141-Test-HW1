package com.deliveryplatform.repositories;

import com.deliveryplatform.models.Order;
import com.deliveryplatform.models.OrderStatus;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Order entity operations.
 */
public interface OrderRepository {

    /**
     * Saves an order to the repository.
     * 
     * @param order the order to save
     * @return the saved order
     */
    Order save(Order order);

    /**
     * Finds an order by its ID.
     * 
     * @param orderId the order ID
     * @return the order if found
     */
    Optional<Order> findById(String orderId);

    /**
     * Finds all orders for a specific customer.
     * 
     * @param customerId the customer ID
     * @return list of orders
     */
    List<Order> findByCustomerId(String customerId);

    /**
     * Finds all orders for a specific restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @return list of orders
     */
    List<Order> findByRestaurantId(String restaurantId);

    /**
     * Finds all orders with a specific status.
     * 
     * @param status the order status
     * @return list of orders
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Finds orders for a restaurant with a specific status.
     * 
     * @param restaurantId the restaurant ID
     * @param status the order status
     * @return list of orders
     */
    List<Order> findByRestaurantIdAndStatus(String restaurantId, OrderStatus status);

    /**
     * Deletes an order from the repository.
     * 
     * @param orderId the order ID
     * @return true if the order was deleted
     */
    boolean deleteById(String orderId);

    /**
     * Checks if an order exists.
     * 
     * @param orderId the order ID
     * @return true if the order exists
     */
    boolean existsById(String orderId);

    /**
     * Gets the count of active orders for a restaurant.
     * 
     * @param restaurantId the restaurant ID
     * @return count of active orders
     */
    long countActiveOrdersByRestaurant(String restaurantId);
}