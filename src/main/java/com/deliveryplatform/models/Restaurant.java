package com.deliveryplatform.models;

import java.time.LocalTime;
import java.util.Objects;

/**
 * Represents a restaurant - simplified version.
 */
public class Restaurant {

    private String restaurantId;
    private String name;
    private boolean isOpen;

    public Restaurant() {}

    public Restaurant(String restaurantId, String name, boolean isOpen) {
        this.restaurantId = restaurantId;
        this.name = name;
        this.isOpen = isOpen;
    }

    // Backward compatibility constructor
    public Restaurant(String restaurantId, String name, LocalTime openTime, LocalTime closeTime, int maxOrders) {
        this.restaurantId = restaurantId;
        this.name = name;
        this.isOpen = true;
    }

    // Basic getters and setters
    public String getRestaurantId() { return restaurantId; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public boolean isOpen() { return isOpen; }
    public void setOpen(boolean open) { isOpen = open; }

    // Compatibility methods (simplified)
    public boolean canAcceptOrder() { return isOpen; }
    public boolean isWithinOperatingHours() { return isOpen; }
    public boolean isAcceptingOrders() { return isOpen; }
    public boolean isAtCapacity() { return false; }
    public int getRemainingCapacity() { return 10; }
    public int getMaxConcurrentOrders() { return 10; }
    public int getCurrentOrderCount() { return 0; }
    public String getCuisine() { return "General"; }
    public LocalTime getOpenTime() { return LocalTime.of(9, 0); }
    public LocalTime getCloseTime() { return LocalTime.of(22, 0); }
    
    public void acceptOrder() { /* simplified */ }
    public void stopAcceptingOrders() { this.isOpen = false; }
    public void startAcceptingOrders() { this.isOpen = true; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Restaurant restaurant = (Restaurant) o;
        return Objects.equals(restaurantId, restaurant.restaurantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(restaurantId);
    }

    @Override
    public String toString() {
        return String.format("Restaurant{restaurantId='%s', name='%s', isOpen=%s}",
                           restaurantId, name, isOpen);
    }
}