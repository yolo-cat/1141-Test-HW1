package com.deliveryplatform.models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OrderStatusTest {

    @Test
    void testValidTransitionsFromPending() {
        OrderStatus pending = OrderStatus.PENDING;
        
        assertTrue(pending.canTransitionTo(OrderStatus.ACCEPTED));
        assertTrue(pending.canTransitionTo(OrderStatus.REJECTED));
        assertTrue(pending.canTransitionTo(OrderStatus.CANCELLED));
        
        assertFalse(pending.canTransitionTo(OrderStatus.PREPARING));
        assertFalse(pending.canTransitionTo(OrderStatus.READY_FOR_DELIVERY));
        assertFalse(pending.canTransitionTo(OrderStatus.IN_DELIVERY));
        assertFalse(pending.canTransitionTo(OrderStatus.DELIVERED));
        assertFalse(pending.canTransitionTo(OrderStatus.PENDING));
    }

    @Test
    void testValidTransitionsFromAccepted() {
        OrderStatus accepted = OrderStatus.ACCEPTED;
        
        assertTrue(accepted.canTransitionTo(OrderStatus.PREPARING));
        assertTrue(accepted.canTransitionTo(OrderStatus.CANCELLED));
        
        assertFalse(accepted.canTransitionTo(OrderStatus.PENDING));
        assertFalse(accepted.canTransitionTo(OrderStatus.ACCEPTED));
        assertFalse(accepted.canTransitionTo(OrderStatus.REJECTED));
        assertFalse(accepted.canTransitionTo(OrderStatus.READY_FOR_DELIVERY));
        assertFalse(accepted.canTransitionTo(OrderStatus.IN_DELIVERY));
        assertFalse(accepted.canTransitionTo(OrderStatus.DELIVERED));
    }

    @Test
    void testValidTransitionsFromPreparing() {
        OrderStatus preparing = OrderStatus.PREPARING;
        
        assertTrue(preparing.canTransitionTo(OrderStatus.READY_FOR_DELIVERY));
        assertTrue(preparing.canTransitionTo(OrderStatus.CANCELLED));
        
        assertFalse(preparing.canTransitionTo(OrderStatus.PENDING));
        assertFalse(preparing.canTransitionTo(OrderStatus.ACCEPTED));
        assertFalse(preparing.canTransitionTo(OrderStatus.PREPARING));
        assertFalse(preparing.canTransitionTo(OrderStatus.REJECTED));
        assertFalse(preparing.canTransitionTo(OrderStatus.IN_DELIVERY));
        assertFalse(preparing.canTransitionTo(OrderStatus.DELIVERED));
    }

    @Test
    void testValidTransitionsFromReadyForDelivery() {
        OrderStatus readyForDelivery = OrderStatus.READY_FOR_DELIVERY;
        
        assertTrue(readyForDelivery.canTransitionTo(OrderStatus.IN_DELIVERY));
        assertTrue(readyForDelivery.canTransitionTo(OrderStatus.CANCELLED));
        
        assertFalse(readyForDelivery.canTransitionTo(OrderStatus.PENDING));
        assertFalse(readyForDelivery.canTransitionTo(OrderStatus.ACCEPTED));
        assertFalse(readyForDelivery.canTransitionTo(OrderStatus.PREPARING));
        assertFalse(readyForDelivery.canTransitionTo(OrderStatus.READY_FOR_DELIVERY));
        assertFalse(readyForDelivery.canTransitionTo(OrderStatus.REJECTED));
        assertFalse(readyForDelivery.canTransitionTo(OrderStatus.DELIVERED));
    }

    @Test
    void testValidTransitionsFromInDelivery() {
        OrderStatus inDelivery = OrderStatus.IN_DELIVERY;
        
        assertTrue(inDelivery.canTransitionTo(OrderStatus.DELIVERED));
        assertTrue(inDelivery.canTransitionTo(OrderStatus.CANCELLED));
        
        assertFalse(inDelivery.canTransitionTo(OrderStatus.PENDING));
        assertFalse(inDelivery.canTransitionTo(OrderStatus.ACCEPTED));
        assertFalse(inDelivery.canTransitionTo(OrderStatus.PREPARING));
        assertFalse(inDelivery.canTransitionTo(OrderStatus.READY_FOR_DELIVERY));
        assertFalse(inDelivery.canTransitionTo(OrderStatus.IN_DELIVERY));
        assertFalse(inDelivery.canTransitionTo(OrderStatus.REJECTED));
    }

    @ParameterizedTest
    @EnumSource(names = {"DELIVERED", "CANCELLED", "REJECTED"})
    void testTerminalStatesCannotTransitionToAnyState(OrderStatus terminalStatus) {
        for (OrderStatus targetStatus : OrderStatus.values()) {
            assertFalse(terminalStatus.canTransitionTo(targetStatus), 
                       String.format("Terminal status %s should not transition to %s", terminalStatus, targetStatus));
        }
    }

    @Test
    void testCanTransitionToWithNullParameter() {
        OrderStatus pending = OrderStatus.PENDING;
        assertFalse(pending.canTransitionTo(null));
    }

    @Test
    void testGetValidTransitionsFromPending() {
        Set<OrderStatus> validTransitions = OrderStatus.PENDING.getValidTransitions();
        
        assertEquals(3, validTransitions.size());
        assertTrue(validTransitions.contains(OrderStatus.ACCEPTED));
        assertTrue(validTransitions.contains(OrderStatus.REJECTED));
        assertTrue(validTransitions.contains(OrderStatus.CANCELLED));
    }

    @Test
    void testGetValidTransitionsFromAccepted() {
        Set<OrderStatus> validTransitions = OrderStatus.ACCEPTED.getValidTransitions();
        
        assertEquals(2, validTransitions.size());
        assertTrue(validTransitions.contains(OrderStatus.PREPARING));
        assertTrue(validTransitions.contains(OrderStatus.CANCELLED));
    }

    @ParameterizedTest
    @EnumSource(names = {"DELIVERED", "CANCELLED", "REJECTED"})
    void testGetValidTransitionsFromTerminalStates(OrderStatus terminalStatus) {
        Set<OrderStatus> validTransitions = terminalStatus.getValidTransitions();
        assertTrue(validTransitions.isEmpty(), 
                  String.format("Terminal status %s should have no valid transitions", terminalStatus));
    }

    @ParameterizedTest
    @EnumSource(names = {"DELIVERED", "CANCELLED", "REJECTED"})
    void testIsTerminalForTerminalStates(OrderStatus terminalStatus) {
        assertTrue(terminalStatus.isTerminal(), 
                  String.format("Status %s should be identified as terminal", terminalStatus));
    }

    @ParameterizedTest
    @EnumSource(names = {"PENDING", "ACCEPTED", "PREPARING", "READY_FOR_DELIVERY", "IN_DELIVERY"})
    void testIsTerminalForNonTerminalStates(OrderStatus nonTerminalStatus) {
        assertFalse(nonTerminalStatus.isTerminal(), 
                   String.format("Status %s should not be identified as terminal", nonTerminalStatus));
    }

    @ParameterizedTest
    @CsvSource({
        "PENDING, ACCEPTED, true",
        "PENDING, PREPARING, false",
        "ACCEPTED, PREPARING, true",
        "ACCEPTED, READY_FOR_DELIVERY, false",
        "PREPARING, READY_FOR_DELIVERY, true",
        "PREPARING, IN_DELIVERY, false",
        "READY_FOR_DELIVERY, IN_DELIVERY, true",
        "READY_FOR_DELIVERY, DELIVERED, false",
        "IN_DELIVERY, DELIVERED, true",
        "IN_DELIVERY, PENDING, false",
        "DELIVERED, CANCELLED, false",
        "CANCELLED, DELIVERED, false",
        "REJECTED, PENDING, false"
    })
    void testStateTransitionValidation(OrderStatus fromStatus, OrderStatus toStatus, boolean expected) {
        assertEquals(expected, fromStatus.canTransitionTo(toStatus),
                    String.format("Transition from %s to %s should return %s", fromStatus, toStatus, expected));
    }
}