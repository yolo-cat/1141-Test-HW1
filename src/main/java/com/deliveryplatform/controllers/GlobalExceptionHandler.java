package com.deliveryplatform.controllers;

import com.deliveryplatform.exceptions.DeliveryAssignmentException;
import com.deliveryplatform.exceptions.DeliveryPlatformException;
import com.deliveryplatform.exceptions.InvalidOrderStateException;
import com.deliveryplatform.exceptions.OrderValidationException;
import com.deliveryplatform.exceptions.RestaurantUnavailableException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Console-based exception handler for the food delivery platform - simplified version.
 */
public class GlobalExceptionHandler {

    private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

    public GlobalExceptionHandler() {
        // Simplified constructor
    }

    /**
     * Handles order validation exceptions.
     */
    public void handleOrderValidationException(OrderValidationException ex) {
        logger.warn("Order validation failed: errorCode={}, errors={}", ex.getErrorCode(), ex.getValidationErrors());

        System.err.println("=== ORDER VALIDATION EXCEPTION ===");
        System.err.println("Error Code: " + ex.getErrorCode());
        System.err.println("Message: Order validation failed");
        System.err.println("Details: " + ex.getValidationErrors());
        System.err.println("Timestamp: " + ex.getTimestamp());
        System.err.println("=====================================");
    }

    /**
     * Handles restaurant unavailable exceptions.
     */
    public void handleRestaurantUnavailableException(RestaurantUnavailableException ex) {
        logger.warn("Restaurant unavailable: restaurantId={}, reason={}", ex.getRestaurantId(), ex.getMessage());

        System.err.println("=== RESTAURANT UNAVAILABLE EXCEPTION ===");
        System.err.println("Error Code: " + ex.getErrorCode());
        System.err.println("Message: " + ex.getMessage());
        System.err.println("Restaurant ID: " + ex.getRestaurantId());
        System.err.println("Timestamp: " + ex.getTimestamp());
        System.err.println("========================================");
    }

    /**
     * Handles invalid order state exceptions.
     */
    public void handleInvalidOrderStateException(InvalidOrderStateException ex) {
        logger.warn("Invalid order state transition: orderId={}, currentStatus={}, attemptedStatus={}", 
                   ex.getOrderId(), ex.getCurrentStatus(), ex.getAttemptedStatus());

        System.err.println("=== INVALID ORDER STATE EXCEPTION ===");
        System.err.println("Error Code: " + ex.getErrorCode());
        System.err.println("Message: " + ex.getMessage());
        System.err.println("Order ID: " + ex.getOrderId());
        System.err.println("Current Status: " + ex.getCurrentStatus());
        System.err.println("Attempted Status: " + ex.getAttemptedStatus());
        System.err.println("Timestamp: " + ex.getTimestamp());
        System.err.println("====================================");
    }

    /**
     * Handles delivery assignment exceptions.
     */
    public void handleDeliveryAssignmentException(DeliveryAssignmentException ex) {
        logger.warn("Delivery assignment failed: orderId={}, reason={}", ex.getOrderId(), ex.getFailureReason());

        System.err.println("=== DELIVERY ASSIGNMENT EXCEPTION ===");
        System.err.println("Error Code: " + ex.getErrorCode());
        System.err.println("Message: " + ex.getMessage());
        System.err.println("Order ID: " + ex.getOrderId());
        System.err.println("Failure Reason: " + ex.getFailureReason());
        System.err.println("Timestamp: " + ex.getTimestamp());
        System.err.println("=====================================");
    }

    /**
     * Handles generic delivery platform exceptions.
     */
    public void handleDeliveryPlatformException(DeliveryPlatformException ex) {
        logger.warn("Business exception: errorCode={}, message={}", ex.getErrorCode(), ex.getMessage());

        System.err.println("=== DELIVERY PLATFORM EXCEPTION ===");
        System.err.println("Error Code: " + ex.getErrorCode());
        System.err.println("Message: " + ex.getMessage());
        System.err.println("Timestamp: " + ex.getTimestamp());
        System.err.println("===================================");
    }

    /**
     * Handles runtime exceptions (system errors).
     */
    public void handleRuntimeException(RuntimeException ex) {
        logger.error("System error occurred", ex);

        System.err.println("=== RUNTIME EXCEPTION ===");
        System.err.println("Error Code: SYSTEM_ERROR");
        System.err.println("Message: An unexpected error occurred. Please try again later.");
        System.err.println("=========================");
    }

    /**
     * Handles generic exceptions (unchecked exceptions).
     */
    public void handleGenericException(Exception ex) {
        logger.error("Unchecked exception occurred", ex);

        System.err.println("=== GENERIC EXCEPTION ===");
        System.err.println("Error Type: " + ex.getClass().getSimpleName());
        System.err.println("Message: " + ex.getMessage());
        System.err.println("Stack Trace: " + java.util.Arrays.toString(ex.getStackTrace()));
        System.err.println("==========================");
    }
}