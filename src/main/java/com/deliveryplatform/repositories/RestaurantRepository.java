package com.deliveryplatform.repositories;

import com.deliveryplatform.models.Restaurant;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Restaurant entity operations.
 */
public interface RestaurantRepository {

    /**
     * Saves a restaurant to the repository.
     * 
     * @param restaurant the restaurant to save
     * @return the saved restaurant
     */
    Restaurant save(Restaurant restaurant);

    /**
     * Finds a restaurant by its ID.
     * 
     * @param restaurantId the restaurant ID
     * @return the restaurant if found
     */
    Optional<Restaurant> findById(String restaurantId);

    /**
     * Finds all restaurants.
     * 
     * @return list of all restaurants
     */
    List<Restaurant> findAll();

    /**
     * Finds all restaurants that are currently open.
     * 
     * @return list of open restaurants
     */
    List<Restaurant> findByIsOpenTrue();

    /**
     * Finds restaurants by cuisine type.
     * 
     * @param cuisine the cuisine type
     * @return list of restaurants
     */
    List<Restaurant> findByCuisine(String cuisine);

    /**
     * Finds restaurants that are accepting orders.
     * 
     * @return list of restaurants accepting orders
     */
    List<Restaurant> findByAcceptingOrdersTrue();

    /**
     * Deletes a restaurant from the repository.
     * 
     * @param restaurantId the restaurant ID
     * @return true if the restaurant was deleted
     */
    boolean deleteById(String restaurantId);

    /**
     * Checks if a restaurant exists.
     * 
     * @param restaurantId the restaurant ID
     * @return true if the restaurant exists
     */
    boolean existsById(String restaurantId);
}