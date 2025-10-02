package com.deliveryplatform.services;

import com.deliveryplatform.exceptions.DeliveryPlatformException;
import com.deliveryplatform.models.Order;
import com.deliveryplatform.models.OrderStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service responsible for structured logging of order-related operations.
 * Provides consistent logging across all order lifecycle events with appropriate log levels.
 */
@Service
public class OrderLoggingService {

    private static final Logger logger = LogManager.getLogger(OrderLoggingService.class);
    private static final Logger businessLogger = LogManager.getLogger("BUSINESS_EVENTS");
    private static final Logger errorLogger = LogManager.getLogger("ERROR_EVENTS");

    /**
     * Logs order creation with relevant details.
     * 
     * @param order the created order
     */
    public void logOrderCreated(Order order) {
        if (order == null) {
            logger.warn("Attempted to log null order creation");
            return;
        }

        businessLogger.info("Order created: orderId={}, customerId={}, restaurantId={}, " +
                           "itemCount={}, totalAmount={}, deliveryAddress={}", 
                           order.getOrderId(), 
                           order.getCustomerId(), 
                           order.getRestaurantId(),
                           order.getItems().size(),
                           order.getTotalAmount(), 
                           maskAddress(order.getDeliveryAddress()));
    }

    /**
     * Logs order status changes with context information.
     * 
     * @param orderId the order ID
     * @param oldStatus the previous status
     * @param newStatus the new status
     * @param reason the reason for the status change
     */
    public void logOrderStatusChange(String orderId, OrderStatus oldStatus, OrderStatus newStatus, String reason) {
        if (orderId == null || oldStatus == null || newStatus == null) {
            logger.warn("Invalid parameters for order status change logging: orderId={}, oldStatus={}, newStatus={}",
                       orderId, oldStatus, newStatus);
            return;
        }

        businessLogger.info("Order status changed: orderId={}, transition={}->{}, reason={}", 
                           orderId, oldStatus, newStatus, reason);
        
        // Log additional context for specific status changes
        switch (newStatus) {
            case CANCELLED, REJECTED -> 
                logger.warn("Order termination: orderId={}, finalStatus={}, reason={}", 
                           orderId, newStatus, reason);
            case DELIVERED -> 
                businessLogger.info("Order completed successfully: orderId={}", orderId);
            case IN_DELIVERY -> 
                businessLogger.info("Order out for delivery: orderId={}", orderId);
        }
    }

    /**
     * Logs business exceptions with order context.
     * 
     * @param exception the business exception that occurred
     * @param orderId the related order ID (can be null)
     */
    public void logBusinessException(DeliveryPlatformException exception, String orderId) {
        if (exception == null) {
            logger.warn("Attempted to log null business exception");
            return;
        }

        String orderContext = orderId != null ? "orderId=" + orderId : "orderId=unknown";
        
        logger.warn("Business exception occurred: {}, errorCode={}, message={}, timestamp={}", 
                   orderContext, 
                   exception.getErrorCode(), 
                   exception.getMessage(), 
                   exception.getTimestamp());
    }

    /**
     * Logs business exceptions with additional context information.
     * 
     * @param exception the business exception that occurred
     * @param orderId the related order ID
     * @param additionalContext additional context information
     */
    public void logBusinessException(DeliveryPlatformException exception, String orderId, String additionalContext) {
        if (exception == null) {
            logger.warn("Attempted to log null business exception");
            return;
        }

        String orderContext = orderId != null ? "orderId=" + orderId : "orderId=unknown";
        
        logger.warn("Business exception occurred: {}, errorCode={}, message={}, context={}, timestamp={}", 
                   orderContext, 
                   exception.getErrorCode(), 
                   exception.getMessage(), 
                   additionalContext,
                   exception.getTimestamp());
    }

    /**
     * Logs system errors with full stack traces.
     * 
     * @param exception the system exception
     * @param context the context in which the error occurred
     */
    public void logSystemError(Exception exception, String context) {
        if (exception == null) {
            logger.warn("Attempted to log null system error");
            return;
        }

        errorLogger.error("System error occurred: context={}, errorType={}, message={}", 
                         context, 
                         exception.getClass().getSimpleName(), 
                         exception.getMessage(), 
                         exception);
    }

    /**
     * Logs system errors with order context.
     * 
     * @param exception the system exception
     * @param context the context in which the error occurred
     * @param orderId the related order ID
     */
    public void logSystemError(Exception exception, String context, String orderId) {
        if (exception == null) {
            logger.warn("Attempted to log null system error");
            return;
        }

        String orderContext = orderId != null ? "orderId=" + orderId : "orderId=unknown";
        
        errorLogger.error("System error occurred: context={}, {}, errorType={}, message={}", 
                         context, 
                         orderContext,
                         exception.getClass().getSimpleName(), 
                         exception.getMessage(), 
                         exception);
    }

    /**
     * Logs restaurant operations.
     * 
     * @param restaurantId the restaurant ID
     * @param operation the operation being performed
     * @param orderId the related order ID
     * @param success whether the operation was successful
     */
    public void logRestaurantOperation(String restaurantId, String operation, String orderId, boolean success) {
        if (success) {
            businessLogger.info("Restaurant operation successful: restaurantId={}, operation={}, orderId={}", 
                               restaurantId, operation, orderId);
        } else {
            logger.warn("Restaurant operation failed: restaurantId={}, operation={}, orderId={}", 
                       restaurantId, operation, orderId);
        }
    }

    /**
     * Logs delivery assignment operations.
     * 
     * @param orderId the order ID
     * @param driverId the driver ID (can be null for failed assignments)
     * @param operation the operation being performed
     * @param success whether the operation was successful
     */
    public void logDeliveryOperation(String orderId, String driverId, String operation, boolean success) {
        String driverContext = driverId != null ? "driverId=" + driverId : "driverId=unassigned";
        
        if (success) {
            businessLogger.info("Delivery operation successful: orderId={}, {}, operation={}", 
                               orderId, driverContext, operation);
        } else {
            logger.warn("Delivery operation failed: orderId={}, {}, operation={}", 
                       orderId, driverContext, operation);
        }
    }

    /**
     * Logs performance warnings for slow operations.
     * 
     * @param operation the operation that was slow
     * @param durationMs the duration in milliseconds
     * @param threshold the threshold that was exceeded
     */
    public void logPerformanceWarning(String operation, long durationMs, long threshold) {
        logger.warn("Performance warning: operation={}, duration={}ms, threshold={}ms", 
                   operation, durationMs, threshold);
    }

    /**
     * Logs data integrity issues.
     * 
     * @param issue the integrity issue description
     * @param entityType the type of entity affected
     * @param entityId the ID of the affected entity
     * @param action the corrective action taken
     */
    public void logDataIntegrityIssue(String issue, String entityType, String entityId, String action) {
        errorLogger.error("Data integrity issue: issue={}, entityType={}, entityId={}, action={}", 
                         issue, entityType, entityId, action);
    }

    /**
     * Logs audit events for compliance tracking.
     * 
     * @param event the audit event
     * @param entityType the type of entity
     * @param entityId the entity ID
     * @param userId the user who performed the action
     */
    public void logAuditEvent(String event, String entityType, String entityId, String userId) {
        businessLogger.info("Audit event: event={}, entityType={}, entityId={}, userId={}", 
                           event, entityType, entityId, userId);
    }

    /**
     * Logs order financial information with appropriate masking.
     * 
     * @param orderId the order ID
     * @param totalAmount the total amount
     * @param operation the financial operation
     */
    public void logFinancialEvent(String orderId, BigDecimal totalAmount, String operation) {
        businessLogger.info("Financial event: orderId={}, operation={}, amount=${}", 
                           orderId, operation, totalAmount);
    }

    /**
     * Masks sensitive address information for logging.
     * 
     * @param address the full address
     * @return masked address suitable for logging
     */
    private String maskAddress(String address) {
        if (address == null || address.length() <= 20) {
            return address;
        }
        
        // Show first 10 characters and mask the rest, keeping last 10 characters
        int length = address.length();
        if (length > 20) {
            return address.substring(0, 10) + "***" + address.substring(length - 7);
        }
        return address;
    }

    /**
     * Logs method entry for debugging purposes.
     * 
     * @param methodName the method being entered
     * @param parameters method parameters
     */
    public void logMethodEntry(String methodName, Object... parameters) {
        if (logger.isDebugEnabled()) {
            logger.debug("Entering method: {} with parameters: {}", methodName, java.util.Arrays.toString(parameters));
        }
    }

    /**
     * Logs method exit for debugging purposes.
     * 
     * @param methodName the method being exited
     * @param result the result being returned
     */
    public void logMethodExit(String methodName, Object result) {
        if (logger.isDebugEnabled()) {
            logger.debug("Exiting method: {} with result: {}", methodName, result);
        }
    }

    /**
     * Logs external service calls.
     * 
     * @param serviceName the external service name
     * @param operation the operation being called
     * @param durationMs the call duration
     * @param success whether the call was successful
     */
    public void logExternalServiceCall(String serviceName, String operation, long durationMs, boolean success) {
        if (success) {
            businessLogger.info("External service call: service={}, operation={}, duration={}ms, status=success", 
                               serviceName, operation, durationMs);
        } else {
            logger.warn("External service call failed: service={}, operation={}, duration={}ms, status=failed", 
                       serviceName, operation, durationMs);
        }
    }
}