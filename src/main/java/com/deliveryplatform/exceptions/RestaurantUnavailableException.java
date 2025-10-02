package com.deliveryplatform.exceptions;

/**
 * Exception thrown when a restaurant is unavailable for order processing.
 * This can occur when the restaurant is closed, at capacity, or temporarily disabled.
 */
public class RestaurantUnavailableException extends DeliveryPlatformException {

    private static final String ERROR_CODE = "RESTAURANT_UNAVAILABLE";
    private final String restaurantId;

    /**
     * Creates a new RestaurantUnavailableException for a specific restaurant.
     * 
     * @param restaurantId the ID of the unavailable restaurant
     */
    public RestaurantUnavailableException(String restaurantId) {
        super(String.format("Restaurant %s is currently unavailable", restaurantId), ERROR_CODE);
        this.restaurantId = restaurantId;
    }

    /**
     * Creates a new RestaurantUnavailableException with a custom reason.
     * 
     * @param restaurantId the ID of the unavailable restaurant
     * @param reason the specific reason for unavailability
     */
    public RestaurantUnavailableException(String restaurantId, String reason) {
        super(String.format("Restaurant %s is currently unavailable: %s", restaurantId, reason), ERROR_CODE);
        this.restaurantId = restaurantId;
    }

    /**
     * Creates a new RestaurantUnavailableException with a cause.
     * 
     * @param restaurantId the ID of the unavailable restaurant
     * @param reason the specific reason for unavailability
     * @param cause the underlying cause
     */
    public RestaurantUnavailableException(String restaurantId, String reason, Throwable cause) {
        super(String.format("Restaurant %s is currently unavailable: %s", restaurantId, reason), ERROR_CODE, cause);
        this.restaurantId = restaurantId;
    }

    /**
     * Gets the ID of the restaurant that is unavailable.
     * 
     * @return the restaurant ID
     */
    public String getRestaurantId() {
        return restaurantId;
    }
}