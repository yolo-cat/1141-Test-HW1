package com.deliveryplatform.exceptions;

import com.deliveryplatform.models.OrderStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomExceptionsTest {

    @Test
    void testDeliveryPlatformExceptionBasicFields() {
        // Create a concrete implementation for testing
        DeliveryPlatformException exception = new OrderValidationException("Test error");
        
        assertEquals("ORDER_VALIDATION_FAILED", exception.getErrorCode());
        assertNotNull(exception.getTimestamp());
        assertTrue(exception.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertEquals("Order validation failed: Test error", exception.getMessage());
    }

    @Test
    void testRestaurantUnavailableExceptionBasicConstructor() {
        String restaurantId = "rest123";
        RestaurantUnavailableException exception = new RestaurantUnavailableException(restaurantId);
        
        assertEquals("RESTAURANT_UNAVAILABLE", exception.getErrorCode());
        assertEquals(restaurantId, exception.getRestaurantId());
        assertEquals("Restaurant rest123 is currently unavailable", exception.getMessage());
        assertNotNull(exception.getTimestamp());
    }

    @Test
    void testRestaurantUnavailableExceptionWithReason() {
        String restaurantId = "rest456";
        String reason = "Temporarily closed for maintenance";
        RestaurantUnavailableException exception = new RestaurantUnavailableException(restaurantId, reason);
        
        assertEquals("RESTAURANT_UNAVAILABLE", exception.getErrorCode());
        assertEquals(restaurantId, exception.getRestaurantId());
        assertEquals("Restaurant rest456 is currently unavailable: Temporarily closed for maintenance", 
                    exception.getMessage());
    }

    @Test
    void testRestaurantUnavailableExceptionWithCause() {
        String restaurantId = "rest789";
        String reason = "Database connection failed";
        Throwable cause = new RuntimeException("Connection timeout");
        
        RestaurantUnavailableException exception = new RestaurantUnavailableException(restaurantId, reason, cause);
        
        assertEquals("RESTAURANT_UNAVAILABLE", exception.getErrorCode());
        assertEquals(restaurantId, exception.getRestaurantId());
        assertEquals(cause, exception.getCause());
        assertTrue(exception.getMessage().contains("Database connection failed"));
    }

    @Test
    void testInvalidOrderStateExceptionBasicConstructor() {
        String orderId = "order123";
        OrderStatus currentStatus = OrderStatus.DELIVERED;
        OrderStatus attemptedStatus = OrderStatus.PREPARING;
        
        InvalidOrderStateException exception = new InvalidOrderStateException(orderId, currentStatus, attemptedStatus);
        
        assertEquals("INVALID_STATE_TRANSITION", exception.getErrorCode());
        assertEquals(orderId, exception.getOrderId());
        assertEquals(currentStatus, exception.getCurrentStatus());
        assertEquals(attemptedStatus, exception.getAttemptedStatus());
        assertEquals("Cannot transition order order123 from DELIVERED to PREPARING", exception.getMessage());
    }

    @Test
    void testInvalidOrderStateExceptionWithCustomMessage() {
        String orderId = "order456";
        OrderStatus currentStatus = OrderStatus.PENDING;
        OrderStatus attemptedStatus = OrderStatus.IN_DELIVERY;
        String customMessage = "Order must be accepted first";
        
        InvalidOrderStateException exception = new InvalidOrderStateException(
                orderId, currentStatus, attemptedStatus, customMessage);
        
        assertTrue(exception.getMessage().contains("Order must be accepted first"));
        assertEquals(orderId, exception.getOrderId());
        assertEquals(currentStatus, exception.getCurrentStatus());
        assertEquals(attemptedStatus, exception.getAttemptedStatus());
    }

    @Test
    void testInvalidOrderStateExceptionWithCause() {
        String orderId = "order789";
        OrderStatus currentStatus = OrderStatus.ACCEPTED;
        OrderStatus attemptedStatus = OrderStatus.DELIVERED;
        Throwable cause = new IllegalStateException("Invalid transition");
        
        InvalidOrderStateException exception = new InvalidOrderStateException(
                orderId, currentStatus, attemptedStatus, cause);
        
        assertEquals(cause, exception.getCause());
        assertEquals("INVALID_STATE_TRANSITION", exception.getErrorCode());
    }

    @Test
    void testDeliveryAssignmentExceptionBasicConstructor() {
        String orderId = "order123";
        String failureReason = "No drivers in the area";
        
        DeliveryAssignmentException exception = new DeliveryAssignmentException(orderId, failureReason);
        
        assertEquals("DELIVERY_ASSIGNMENT_FAILED", exception.getErrorCode());
        assertEquals(orderId, exception.getOrderId());
        assertEquals(failureReason, exception.getFailureReason());
        assertEquals("Failed to assign delivery for order order123: No drivers in the area", 
                    exception.getMessage());
    }

    @Test
    void testDeliveryAssignmentExceptionWithCause() {
        String orderId = "order456";
        String failureReason = "System error";
        Throwable cause = new RuntimeException("Database unavailable");
        
        DeliveryAssignmentException exception = new DeliveryAssignmentException(orderId, failureReason, cause);
        
        assertEquals(cause, exception.getCause());
        assertEquals(orderId, exception.getOrderId());
        assertEquals(failureReason, exception.getFailureReason());
    }

    @Test
    void testDeliveryAssignmentExceptionNoDriversAvailable() {
        String orderId = "order789";
        
        DeliveryAssignmentException exception = DeliveryAssignmentException.noDriversAvailable(orderId);
        
        assertEquals("DELIVERY_ASSIGNMENT_FAILED", exception.getErrorCode());
        assertEquals(orderId, exception.getOrderId());
        assertTrue(exception.getFailureReason().contains("No drivers available"));
    }

    @Test
    void testDeliveryAssignmentExceptionSystemFailure() {
        String orderId = "order101";
        Throwable systemError = new RuntimeException("Network timeout");
        
        DeliveryAssignmentException exception = DeliveryAssignmentException.systemFailure(orderId, systemError);
        
        assertEquals("DELIVERY_ASSIGNMENT_FAILED", exception.getErrorCode());
        assertEquals(orderId, exception.getOrderId());
        assertEquals(systemError, exception.getCause());
        assertTrue(exception.getFailureReason().contains("System failure"));
    }

    @Test
    void testOrderValidationExceptionSingleError() {
        String validationError = "Customer ID cannot be null";
        
        OrderValidationException exception = new OrderValidationException(validationError);
        
        assertEquals("ORDER_VALIDATION_FAILED", exception.getErrorCode());
        assertEquals(1, exception.getErrorCount());
        assertEquals(List.of(validationError), exception.getValidationErrors());
        assertTrue(exception.getMessage().contains(validationError));
    }

    @Test
    void testOrderValidationExceptionMultipleErrors() {
        List<String> validationErrors = Arrays.asList(
                "Customer ID cannot be null",
                "Order total must be greater than 0",
                "Delivery address is required"
        );
        
        OrderValidationException exception = new OrderValidationException(validationErrors);
        
        assertEquals("ORDER_VALIDATION_FAILED", exception.getErrorCode());
        assertEquals(3, exception.getErrorCount());
        assertEquals(validationErrors, exception.getValidationErrors());
        
        for (String error : validationErrors) {
            assertTrue(exception.getMessage().contains(error));
        }
    }

    @Test
    void testOrderValidationExceptionWithCause() {
        String validationError = "Invalid order format";
        Throwable cause = new IllegalArgumentException("JSON parsing failed");
        
        OrderValidationException exception = new OrderValidationException(validationError, cause);
        
        assertEquals(cause, exception.getCause());
        assertEquals(1, exception.getErrorCount());
        assertTrue(exception.getMessage().contains(validationError));
    }

    @Test
    void testOrderValidationExceptionMissingRequiredFields() {
        List<String> missingFields = Arrays.asList("customerId", "restaurantId", "deliveryAddress");
        
        OrderValidationException exception = OrderValidationException.missingRequiredFields(missingFields);
        
        assertEquals("ORDER_VALIDATION_FAILED", exception.getErrorCode());
        assertEquals(3, exception.getErrorCount());
        
        for (String field : missingFields) {
            assertTrue(exception.getValidationErrors().stream()
                    .anyMatch(error -> error.contains("Missing required field: " + field)));
        }
    }

    @Test
    void testOrderValidationExceptionInvalidFieldValue() {
        String fieldName = "totalAmount";
        Object invalidValue = -10.5;
        String requirement = "Must be positive";
        
        OrderValidationException exception = OrderValidationException.invalidFieldValue(
                fieldName, invalidValue, requirement);
        
        assertEquals("ORDER_VALIDATION_FAILED", exception.getErrorCode());
        assertEquals(1, exception.getErrorCount());
        
        String errorMessage = exception.getValidationErrors().get(0);
        assertTrue(errorMessage.contains(fieldName));
        assertTrue(errorMessage.contains(invalidValue.toString()));
        assertTrue(errorMessage.contains(requirement));
    }

    @Test
    void testExceptionToStringFormat() {
        OrderValidationException exception = new OrderValidationException("Test error");
        String toStringResult = exception.toString();
        
        assertTrue(toStringResult.contains("OrderValidationException"));
        assertTrue(toStringResult.contains("ORDER_VALIDATION_FAILED"));
        assertTrue(toStringResult.contains("Test error"));
        assertNotNull(exception.getTimestamp());
    }
}