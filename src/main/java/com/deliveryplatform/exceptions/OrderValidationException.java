package com.deliveryplatform.exceptions;

import java.util.List;

/**
 * Exception thrown when order validation fails.
 * This occurs when order data is invalid or doesn't meet business requirements.
 */
public class OrderValidationException extends DeliveryPlatformException {

    private static final String ERROR_CODE = "ORDER_VALIDATION_FAILED";
    
    private final List<String> validationErrors;

    /**
     * Creates a new OrderValidationException with a single validation error.
     * 
     * @param validationError the validation error message
     */
    public OrderValidationException(String validationError) {
        super("Order validation failed: " + validationError, ERROR_CODE);
        this.validationErrors = List.of(validationError);
    }

    /**
     * Creates a new OrderValidationException with multiple validation errors.
     * 
     * @param validationErrors list of validation error messages
     */
    public OrderValidationException(List<String> validationErrors) {
        super("Order validation failed: " + String.join(", ", validationErrors), ERROR_CODE);
        this.validationErrors = List.copyOf(validationErrors);
    }

    /**
     * Creates a new OrderValidationException with a cause.
     * 
     * @param validationError the validation error message
     * @param cause the underlying cause
     */
    public OrderValidationException(String validationError, Throwable cause) {
        super("Order validation failed: " + validationError, ERROR_CODE, cause);
        this.validationErrors = List.of(validationError);
    }

    /**
     * Convenience method for creating an exception for missing required fields.
     * 
     * @param fieldNames the names of the missing fields
     * @return a new OrderValidationException
     */
    public static OrderValidationException missingRequiredFields(List<String> fieldNames) {
        List<String> errors = fieldNames.stream()
                .map(field -> "Missing required field: " + field)
                .toList();
        return new OrderValidationException(errors);
    }

    /**
     * Convenience method for creating an exception for invalid field values.
     * 
     * @param fieldName the name of the field
     * @param invalidValue the invalid value
     * @param requirement the requirement that wasn't met
     * @return a new OrderValidationException
     */
    public static OrderValidationException invalidFieldValue(String fieldName, Object invalidValue, String requirement) {
        String error = String.format("Invalid value for field '%s': %s. Requirement: %s", 
                                    fieldName, invalidValue, requirement);
        return new OrderValidationException(error);
    }

    /**
     * Gets the list of validation errors.
     * 
     * @return immutable list of validation error messages
     */
    public List<String> getValidationErrors() {
        return validationErrors;
    }

    /**
     * Gets the number of validation errors.
     * 
     * @return the count of validation errors
     */
    public int getErrorCount() {
        return validationErrors.size();
    }
}