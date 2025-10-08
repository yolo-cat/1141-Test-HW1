package com.deliveryplatform.controllers;

import com.deliveryplatform.exceptions.DeliveryAssignmentException;
import com.deliveryplatform.exceptions.DeliveryPlatformException;
import com.deliveryplatform.exceptions.InvalidOrderStateException;
import com.deliveryplatform.exceptions.OrderValidationException;
import com.deliveryplatform.exceptions.RestaurantUnavailableException;
import com.deliveryplatform.services.OrderLoggingService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Console-based exception handler for the food delivery platform.
 * Handles both business logic exceptions and system errors with proper logging and console output.
 */
public class GlobalExceptionHandler {

    private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);
    private static final Pattern ORDER_ID_PATTERN = Pattern.compile("order\\s+(\\w+-\\w+)", Pattern.CASE_INSENSITIVE);

    private final OrderLoggingService loggingService;

    public GlobalExceptionHandler(OrderLoggingService loggingService) {
        this.loggingService = loggingService;
    }

    /**
     * Handles order validation exceptions.
     */
    public void handleOrderValidationException(OrderValidationException ex) {
        String orderId = extractOrderId(ex.getMessage());
        loggingService.logBusinessException(ex, orderId);

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
        String orderId = extractOrderId(ex.getMessage());
        loggingService.logBusinessException(ex, orderId, "restaurantId=" + ex.getRestaurantId());

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
        loggingService.logBusinessException(ex, ex.getOrderId(), 
                                          String.format("currentStatus=%s, attemptedStatus=%s", 
                                                       ex.getCurrentStatus(), ex.getAttemptedStatus()));

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
        loggingService.logBusinessException(ex, ex.getOrderId(), "failureReason=" + ex.getFailureReason());

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
        String orderId = extractOrderId(ex.getMessage());
        loggingService.logBusinessException(ex, orderId);

        System.err.println("=== DELIVERY PLATFORM EXCEPTION ===");
        System.err.println("Error Code: " + ex.getErrorCode());
        System.err.println("Message: " + ex.getMessage());
        System.err.println("Timestamp: " + ex.getTimestamp());
        System.err.println("===================================");
    }

    /**
     * Handles illegal argument exceptions.
     */
    public void handleIllegalArgumentException(IllegalArgumentException ex) {
        String orderId = extractOrderId(ex.getMessage());
        
        logger.warn("Invalid request parameters: message={}, orderId={}", ex.getMessage(), orderId);

        System.err.println("=== ILLEGAL ARGUMENT EXCEPTION ===");
        System.err.println("Error Code: INVALID_REQUEST");
        System.err.println("Message: Invalid request parameters: " + ex.getMessage());
        System.err.println("Timestamp: " + LocalDateTime.now());
        System.err.println("==================================");
    }

    /**
     * Handles runtime exceptions (system errors).
     */
    public void handleRuntimeException(RuntimeException ex) {
        String orderId = extractOrderId(ex.getMessage());
        loggingService.logSystemError(ex, "Unexpected runtime error", orderId);

        System.err.println("=== RUNTIME EXCEPTION ===");
        System.err.println("Error Code: SYSTEM_ERROR");
        System.err.println("Message: An unexpected error occurred. Please try again later.");
        System.err.println("Timestamp: " + LocalDateTime.now());
        System.err.println("=========================");
    }

    /**
     * Handles all other exceptions.
     */
    public void handleGenericException(Exception ex) {
        String orderId = extractOrderId(ex.getMessage());
        loggingService.logSystemError(ex, "Unexpected system error", orderId);

        System.err.println("=== GENERIC EXCEPTION ===");
        System.err.println("Error Code: SYSTEM_ERROR");
        System.err.println("Message: An unexpected system error occurred. Please contact support.");
        System.err.println("Timestamp: " + LocalDateTime.now());
        System.err.println("=========================");
    }

    /**
     * Extracts order ID from exception message or other context.
     */
    private String extractOrderId(String message) {
        if (message == null) {
            return null;
        }

        Matcher matcher = ORDER_ID_PATTERN.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }
}