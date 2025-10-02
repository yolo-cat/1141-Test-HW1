package com.deliveryplatform.controllers;

import com.deliveryplatform.exceptions.DeliveryAssignmentException;
import com.deliveryplatform.exceptions.DeliveryPlatformException;
import com.deliveryplatform.exceptions.InvalidOrderStateException;
import com.deliveryplatform.exceptions.OrderValidationException;
import com.deliveryplatform.exceptions.RestaurantUnavailableException;
import com.deliveryplatform.services.OrderLoggingService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Global exception handler for the food delivery platform.
 * Handles both business logic exceptions and system errors with proper logging and HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);
    private static final Pattern ORDER_ID_PATTERN = Pattern.compile("order\\s+(\\w+-\\w+)", Pattern.CASE_INSENSITIVE);

    private final OrderLoggingService loggingService;

    @Autowired
    public GlobalExceptionHandler(OrderLoggingService loggingService) {
        this.loggingService = loggingService;
    }

    /**
     * Handles order validation exceptions.
     */
    @ExceptionHandler(OrderValidationException.class)
    public ResponseEntity<ErrorResponse> handleOrderValidationException(OrderValidationException ex) {
        String orderId = extractOrderId(ex.getMessage());
        loggingService.logBusinessException(ex, orderId);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .message("Order validation failed")
                .details(ex.getValidationErrors())
                .timestamp(ex.getTimestamp())
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handles restaurant unavailable exceptions.
     */
    @ExceptionHandler(RestaurantUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleRestaurantUnavailableException(RestaurantUnavailableException ex) {
        String orderId = extractOrderId(ex.getMessage());
        loggingService.logBusinessException(ex, orderId, "restaurantId=" + ex.getRestaurantId());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .timestamp(ex.getTimestamp())
                .addDetail("restaurantId", ex.getRestaurantId())
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    /**
     * Handles invalid order state exceptions.
     */
    @ExceptionHandler(InvalidOrderStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrderStateException(InvalidOrderStateException ex) {
        loggingService.logBusinessException(ex, ex.getOrderId(), 
                                          String.format("currentStatus=%s, attemptedStatus=%s", 
                                                       ex.getCurrentStatus(), ex.getAttemptedStatus()));

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .timestamp(ex.getTimestamp())
                .addDetail("orderId", ex.getOrderId())
                .addDetail("currentStatus", ex.getCurrentStatus().toString())
                .addDetail("attemptedStatus", ex.getAttemptedStatus().toString())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handles delivery assignment exceptions.
     */
    @ExceptionHandler(DeliveryAssignmentException.class)
    public ResponseEntity<ErrorResponse> handleDeliveryAssignmentException(DeliveryAssignmentException ex) {
        loggingService.logBusinessException(ex, ex.getOrderId(), "failureReason=" + ex.getFailureReason());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .timestamp(ex.getTimestamp())
                .addDetail("orderId", ex.getOrderId())
                .addDetail("failureReason", ex.getFailureReason())
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    /**
     * Handles generic delivery platform exceptions.
     */
    @ExceptionHandler(DeliveryPlatformException.class)
    public ResponseEntity<ErrorResponse> handleDeliveryPlatformException(DeliveryPlatformException ex) {
        String orderId = extractOrderId(ex.getMessage());
        loggingService.logBusinessException(ex, orderId);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .timestamp(ex.getTimestamp())
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handles illegal argument exceptions.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        String orderId = extractOrderId(ex.getMessage());
        
        logger.warn("Invalid request parameters: message={}, orderId={}", ex.getMessage(), orderId);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("INVALID_REQUEST")
                .message("Invalid request parameters: " + ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handles runtime exceptions (system errors).
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        String orderId = extractOrderId(ex.getMessage());
        loggingService.logSystemError(ex, "Unexpected runtime error", orderId);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("SYSTEM_ERROR")
                .message("An unexpected error occurred. Please try again later.")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Handles all other exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        String orderId = extractOrderId(ex.getMessage());
        loggingService.logSystemError(ex, "Unexpected system error", orderId);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("SYSTEM_ERROR")
                .message("An unexpected system error occurred. Please contact support.")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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

    /**
     * Error response data transfer object.
     */
    public static class ErrorResponse {
        private String errorCode;
        private String message;
        private LocalDateTime timestamp;
        private Object details;
        private Map<String, Object> additionalDetails;

        // Private constructor for builder pattern
        private ErrorResponse() {
            this.additionalDetails = new HashMap<>();
        }

        public static ErrorResponseBuilder builder() {
            return new ErrorResponseBuilder();
        }

        // Getters
        public String getErrorCode() {
            return errorCode;
        }

        public String getMessage() {
            return message;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public Object getDetails() {
            return details;
        }

        public Map<String, Object> getAdditionalDetails() {
            return additionalDetails;
        }

        /**
         * Builder for ErrorResponse.
         */
        public static class ErrorResponseBuilder {
            private final ErrorResponse response;

            private ErrorResponseBuilder() {
                this.response = new ErrorResponse();
            }

            public ErrorResponseBuilder errorCode(String errorCode) {
                response.errorCode = errorCode;
                return this;
            }

            public ErrorResponseBuilder message(String message) {
                response.message = message;
                return this;
            }

            public ErrorResponseBuilder timestamp(LocalDateTime timestamp) {
                response.timestamp = timestamp;
                return this;
            }

            public ErrorResponseBuilder details(Object details) {
                response.details = details;
                return this;
            }

            public ErrorResponseBuilder addDetail(String key, Object value) {
                response.additionalDetails.put(key, value);
                return this;
            }

            public ErrorResponse build() {
                if (response.timestamp == null) {
                    response.timestamp = LocalDateTime.now();
                }
                return response;
            }
        }
    }
}