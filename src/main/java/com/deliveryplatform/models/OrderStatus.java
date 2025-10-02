package com.deliveryplatform.models;

import java.util.Set;
import java.util.EnumSet;
import java.util.Map;
import java.util.HashMap;

/**
 * Order status enum with state transition validation logic.
 * Defines all possible order states and validates transitions between them.
 */
public enum OrderStatus {
    PENDING,
    ACCEPTED,
    PREPARING,
    READY_FOR_DELIVERY,
    IN_DELIVERY,
    DELIVERED,
    CANCELLED,
    REJECTED;

    // Define valid state transitions
    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = new HashMap<>();

    static {
        // PENDING can transition to ACCEPTED, REJECTED, or CANCELLED
        VALID_TRANSITIONS.put(PENDING, EnumSet.of(ACCEPTED, REJECTED, CANCELLED));
        
        // ACCEPTED can transition to PREPARING or CANCELLED
        VALID_TRANSITIONS.put(ACCEPTED, EnumSet.of(PREPARING, CANCELLED));
        
        // PREPARING can transition to READY_FOR_DELIVERY or CANCELLED
        VALID_TRANSITIONS.put(PREPARING, EnumSet.of(READY_FOR_DELIVERY, CANCELLED));
        
        // READY_FOR_DELIVERY can transition to IN_DELIVERY or CANCELLED
        VALID_TRANSITIONS.put(READY_FOR_DELIVERY, EnumSet.of(IN_DELIVERY, CANCELLED));
        
        // IN_DELIVERY can transition to DELIVERED or CANCELLED
        VALID_TRANSITIONS.put(IN_DELIVERY, EnumSet.of(DELIVERED, CANCELLED));
        
        // Terminal states (DELIVERED, CANCELLED, REJECTED) cannot transition to any other state
        VALID_TRANSITIONS.put(DELIVERED, EnumSet.noneOf(OrderStatus.class));
        VALID_TRANSITIONS.put(CANCELLED, EnumSet.noneOf(OrderStatus.class));
        VALID_TRANSITIONS.put(REJECTED, EnumSet.noneOf(OrderStatus.class));
    }

    /**
     * Validates if a transition from this status to the new status is allowed.
     * 
     * @param newStatus the target status to transition to
     * @return true if the transition is valid, false otherwise
     */
    public boolean canTransitionTo(OrderStatus newStatus) {
        if (newStatus == null) {
            return false;
        }
        
        Set<OrderStatus> allowedTransitions = VALID_TRANSITIONS.get(this);
        return allowedTransitions != null && allowedTransitions.contains(newStatus);
    }

    /**
     * Gets all valid transition states from the current status.
     * 
     * @return set of valid target statuses, empty set if no transitions are allowed
     */
    public Set<OrderStatus> getValidTransitions() {
        return Set.copyOf(VALID_TRANSITIONS.getOrDefault(this, EnumSet.noneOf(OrderStatus.class)));
    }

    /**
     * Checks if this status represents a terminal state (no further transitions allowed).
     * 
     * @return true if this is a terminal status
     */
    public boolean isTerminal() {
        Set<OrderStatus> allowedTransitions = VALID_TRANSITIONS.get(this);
        return allowedTransitions == null || allowedTransitions.isEmpty();
    }
}