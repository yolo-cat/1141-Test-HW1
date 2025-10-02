package com.deliveryplatform.exceptions;

import java.time.LocalDateTime;

/**
 * Base checked exception for all business logic errors in the delivery platform.
 * All custom business exceptions should extend this class.
 */
public abstract class DeliveryPlatformException extends Exception {
    
    private final String errorCode;
    private final LocalDateTime timestamp;

    /**
     * Constructor for creating a delivery platform exception.
     * 
     * @param message the detailed error message
     * @param errorCode the unique error code for this exception type
     */
    protected DeliveryPlatformException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor for creating a delivery platform exception with a cause.
     * 
     * @param message the detailed error message
     * @param errorCode the unique error code for this exception type
     * @param cause the underlying cause of this exception
     */
    protected DeliveryPlatformException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Gets the unique error code for this exception.
     * 
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Gets the timestamp when this exception was created.
     * 
     * @return the exception timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("%s{errorCode='%s', timestamp=%s, message='%s'}", 
                           getClass().getSimpleName(), errorCode, timestamp, getMessage());
    }
}