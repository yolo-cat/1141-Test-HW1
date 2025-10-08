package com.deliveryplatform.repositories;

import com.deliveryplatform.models.Restaurant;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of RestaurantRepository for development and testing.
 */
public class InMemoryRestaurantRepository implements RestaurantRepository {

    private final Map<String, Restaurant> restaurants = new ConcurrentHashMap<>();

    @Override
    public Restaurant save(Restaurant restaurant) {
        if (restaurant == null) {
            throw new IllegalArgumentException("Restaurant cannot be null");
        }
        restaurants.put(restaurant.getRestaurantId(), restaurant);
        return restaurant;
    }

    @Override
    public Optional<Restaurant> findById(String restaurantId) {
        return Optional.ofNullable(restaurants.get(restaurantId));
    }

    @Override
    public List<Restaurant> findAll() {
        return List.copyOf(restaurants.values());
    }

    @Override
    public List<Restaurant> findByIsOpenTrue() {
        return restaurants.values().stream()
                .filter(Restaurant::isOpen)
                .collect(Collectors.toList());
    }

    @Override
    public List<Restaurant> findByCuisine(String cuisine) {
        return restaurants.values().stream()
                .filter(restaurant -> cuisine.equals(restaurant.getCuisine()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Restaurant> findByAcceptingOrdersTrue() {
        return restaurants.values().stream()
                .filter(Restaurant::isAcceptingOrders)
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteById(String restaurantId) {
        return restaurants.remove(restaurantId) != null;
    }

    @Override
    public boolean existsById(String restaurantId) {
        return restaurants.containsKey(restaurantId);
    }

    // Additional methods for testing
    public void clear() {
        restaurants.clear();
    }

    public int size() {
        return restaurants.size();
    }
}