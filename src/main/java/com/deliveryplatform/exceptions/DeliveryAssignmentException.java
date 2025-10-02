package com.deliveryplatform.exceptions;

/**
 * Exception thrown when delivery assignment fails.
 * This can occur when no drivers are available, driver assignment system fails,
 * or there are issues with order-driver matching.
 */
public class DeliveryAssignmentException extends DeliveryPlatformException {

    private static final String ERROR_CODE = "DELIVERY_ASSIGNMENT_FAILED";
    
    private final String orderId;
    private final String failureReason;

    /**
     * Creates a new DeliveryAssignmentException for a failed delivery assignment.
     * 
     * @param orderId the ID of the order that couldn't be assigned
     * @param failureReason the reason why delivery assignment failed
     */
    public DeliveryAssignmentException(String orderId, String failureReason) {
        super(String.format("Failed to assign delivery for order %s: %s", orderId, failureReason), ERROR_CODE);
        this.orderId = orderId;
        this.failureReason = failureReason;
    }

    /**
     * Creates a new DeliveryAssignmentException with a cause.
     * 
     * @param orderId the ID of the order that couldn't be assigned
     * @param failureReason the reason why delivery assignment failed
     * @param cause the underlying cause of the assignment failure
     */
    public DeliveryAssignmentException(String orderId, String failureReason, Throwable cause) {
        super(String.format("Failed to assign delivery for order %s: %s", orderId, failureReason), ERROR_CODE, cause);
        this.orderId = orderId;
        this.failureReason = failureReason;
    }

    /**
     * Convenience constructor for no drivers available scenario.
     * 
     * @param orderId the ID of the order that couldn't be assigned
     */
    public static DeliveryAssignmentException noDriversAvailable(String orderId) {
        return new DeliveryAssignmentException(orderId, "No drivers available for delivery assignment");
    }

    /**
     * Convenience constructor for system failure scenario.
     * 
     * @param orderId the ID of the order that couldn't be assigned
     * @param systemError the system error that occurred
     */
    public static DeliveryAssignmentException systemFailure(String orderId, Throwable systemError) {
        return new DeliveryAssignmentException(orderId, "System failure during assignment", systemError);
    }

    /**
     * Gets the ID of the order that couldn't be assigned for delivery.
     * 
     * @return the order ID
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * Gets the reason why delivery assignment failed.
     * 
     * @return the failure reason
     */
    public String getFailureReason() {
        return failureReason;
    }
}