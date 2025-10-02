package com.deliveryplatform.exceptions;

import com.deliveryplatform.models.OrderStatus;

/**
 * Exception thrown when an invalid order state transition is attempted.
 * This ensures proper order workflow and prevents invalid state changes.
 */
public class InvalidOrderStateException extends DeliveryPlatformException {

    private static final String ERROR_CODE = "INVALID_STATE_TRANSITION";
    
    private final String orderId;
    private final OrderStatus currentStatus;
    private final OrderStatus attemptedStatus;

    /**
     * Creates a new InvalidOrderStateException for an invalid state transition.
     * 
     * @param orderId the ID of the order
     * @param currentStatus the current status of the order
     * @param attemptedStatus the status that was attempted to be set
     */
    public InvalidOrderStateException(String orderId, OrderStatus currentStatus, OrderStatus attemptedStatus) {
        super(String.format("Cannot transition order %s from %s to %s", orderId, currentStatus, attemptedStatus), 
              ERROR_CODE);
        this.orderId = orderId;
        this.currentStatus = currentStatus;
        this.attemptedStatus = attemptedStatus;
    }

    /**
     * Creates a new InvalidOrderStateException with a custom message.
     * 
     * @param orderId the ID of the order
     * @param currentStatus the current status of the order
     * @param attemptedStatus the status that was attempted to be set
     * @param customMessage additional context about the invalid transition
     */
    public InvalidOrderStateException(String orderId, OrderStatus currentStatus, OrderStatus attemptedStatus, 
                                    String customMessage) {
        super(String.format("Cannot transition order %s from %s to %s: %s", 
                           orderId, currentStatus, attemptedStatus, customMessage), ERROR_CODE);
        this.orderId = orderId;
        this.currentStatus = currentStatus;
        this.attemptedStatus = attemptedStatus;
    }

    /**
     * Creates a new InvalidOrderStateException with a cause.
     * 
     * @param orderId the ID of the order
     * @param currentStatus the current status of the order
     * @param attemptedStatus the status that was attempted to be set
     * @param cause the underlying cause
     */
    public InvalidOrderStateException(String orderId, OrderStatus currentStatus, OrderStatus attemptedStatus, 
                                    Throwable cause) {
        super(String.format("Cannot transition order %s from %s to %s", orderId, currentStatus, attemptedStatus), 
              ERROR_CODE, cause);
        this.orderId = orderId;
        this.currentStatus = currentStatus;
        this.attemptedStatus = attemptedStatus;
    }

    /**
     * Gets the ID of the order that had the invalid state transition.
     * 
     * @return the order ID
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * Gets the current status of the order.
     * 
     * @return the current order status
     */
    public OrderStatus getCurrentStatus() {
        return currentStatus;
    }

    /**
     * Gets the status that was attempted to be set.
     * 
     * @return the attempted order status
     */
    public OrderStatus getAttemptedStatus() {
        return attemptedStatus;
    }
}