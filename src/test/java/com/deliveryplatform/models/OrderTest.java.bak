package com.deliveryplatform.models;

import com.deliveryplatform.exceptions.InvalidOrderStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private Order order;
    private List<OrderItem> testItems;

    @BeforeEach
    void setUp() {
        // Create test order items
        OrderItem item1 = new OrderItem("item1", "Pizza Margherita", 2, new BigDecimal("15.99"));
        OrderItem item2 = new OrderItem("item2", "Coca Cola", 1, new BigDecimal("2.99"));
        testItems = Arrays.asList(item1, item2);

        // Create test order
        order = new Order("customer123", "restaurant456", testItems, "123 Main St, City");
    }

    @Test
    void testOrderCreationWithValidData() {
        assertNotNull(order.getOrderId());
        assertTrue(order.getOrderId().startsWith("ORD-"));
        assertEquals("customer123", order.getCustomerId());
        assertEquals("restaurant456", order.getRestaurantId());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals("123 Main St, City", order.getDeliveryAddress());
        assertNotNull(order.getCreatedAt());
        assertNotNull(order.getUpdatedAt());
        assertEquals(2, order.getItems().size());
        
        // Test total amount calculation (2 * 15.99 + 1 * 2.99 = 34.97)
        BigDecimal expectedTotal = new BigDecimal("34.97");
        assertEquals(0, expectedTotal.compareTo(order.getTotalAmount()));
    }

    @Test
    void testOrderIdGeneration() {
        Order order1 = new Order();
        Order order2 = new Order();
        
        assertNotEquals(order1.getOrderId(), order2.getOrderId());
        assertTrue(order1.getOrderId().startsWith("ORD-"));
        assertTrue(order2.getOrderId().startsWith("ORD-"));
    }

    @Test
    void testAcceptOrder() throws InvalidOrderStateException {
        order.accept("Restaurant confirmed order");
        
        assertEquals(OrderStatus.ACCEPTED, order.getStatus());
        assertTrue(order.getUpdatedAt().isAfter(order.getCreatedAt()));
    }

    @Test
    void testAcceptOrderWithoutReason() throws InvalidOrderStateException {
        order.accept(null);
        
        assertEquals(OrderStatus.ACCEPTED, order.getStatus());
    }

    @Test
    void testRejectOrder() throws InvalidOrderStateException {
        String rejectionReason = "Restaurant is closed";
        order.reject(rejectionReason);
        
        assertEquals(OrderStatus.REJECTED, order.getStatus());
        assertEquals(rejectionReason, order.getRejectionReason());
    }

    @Test
    void testRejectOrderWithoutReason() {
        assertThrows(IllegalArgumentException.class, () -> order.reject(null));
        assertThrows(IllegalArgumentException.class, () -> order.reject(""));
        assertThrows(IllegalArgumentException.class, () -> order.reject("   "));
    }

    @Test
    void testCancelOrder() throws InvalidOrderStateException {
        String cancellationReason = "Customer requested cancellation";
        order.cancel(cancellationReason);
        
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(cancellationReason, order.getCancellationReason());
    }

    @Test
    void testCancelOrderWithoutReason() {
        assertThrows(IllegalArgumentException.class, () -> order.cancel(null));
        assertThrows(IllegalArgumentException.class, () -> order.cancel(""));
    }

    @Test
    void testCompleteOrderLifecycle() throws InvalidOrderStateException {
        // Start with PENDING
        assertEquals(OrderStatus.PENDING, order.getStatus());
        
        // Accept order
        order.accept("Restaurant confirmed");
        assertEquals(OrderStatus.ACCEPTED, order.getStatus());
        
        // Start preparing
        order.startPreparing();
        assertEquals(OrderStatus.PREPARING, order.getStatus());
        
        // Mark ready for delivery
        order.markReadyForDelivery();
        assertEquals(OrderStatus.READY_FOR_DELIVERY, order.getStatus());
        
        // Assign driver
        order.assignDriver("driver789");
        assertEquals(OrderStatus.IN_DELIVERY, order.getStatus());
        assertEquals("driver789", order.getDriverId());
        
        // Mark delivered
        LocalDateTime beforeDelivery = LocalDateTime.now();
        order.markDelivered();
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        assertNotNull(order.getActualDeliveryTime());
        assertTrue(order.getActualDeliveryTime().isAfter(beforeDelivery));
    }

    @Test
    void testInvalidStateTransitions() throws InvalidOrderStateException {
        // Try to go directly from PENDING to PREPARING (should fail)
        assertThrows(InvalidOrderStateException.class, () -> order.startPreparing());
        
        // Accept the order first
        order.accept("Restaurant confirmed");
        
        // Try to mark as delivered without going through proper states
        assertThrows(InvalidOrderStateException.class, () -> order.markDelivered());
    }

    @Test
    void testAssignDriverValidation() {
        assertThrows(IllegalArgumentException.class, () -> order.assignDriver(null));
        assertThrows(IllegalArgumentException.class, () -> order.assignDriver(""));
        assertThrows(IllegalArgumentException.class, () -> order.assignDriver("   "));
    }

    @Test
    void testAddItemToPendingOrder() {
        OrderItem newItem = new OrderItem("item3", "French Fries", 1, new BigDecimal("4.99"));
        BigDecimal originalTotal = order.getTotalAmount();
        
        order.addItem(newItem);
        
        assertEquals(3, order.getItems().size());
        assertTrue(order.getTotalAmount().compareTo(originalTotal) > 0);
        assertEquals(0, new BigDecimal("39.96").compareTo(order.getTotalAmount())); // 34.97 + 4.99
    }

    @Test
    void testRemoveItemFromPendingOrder() {
        boolean removed = order.removeItem("item1");
        
        assertTrue(removed);
        assertEquals(1, order.getItems().size());
        assertEquals(0, new BigDecimal("2.99").compareTo(order.getTotalAmount()));
    }

    @Test
    void testRemoveNonExistentItem() {
        boolean removed = order.removeItem("nonexistent");
        
        assertFalse(removed);
        assertEquals(2, order.getItems().size());
    }

    @Test
    void testCannotModifyItemsAfterAcceptance() throws InvalidOrderStateException {
        order.accept("Restaurant confirmed");
        
        OrderItem newItem = new OrderItem("item3", "French Fries", 1, new BigDecimal("4.99"));
        
        assertThrows(IllegalStateException.class, () -> order.addItem(newItem));
        assertThrows(IllegalStateException.class, () -> order.removeItem("item1"));
    }

    @Test
    void testCanModifyItemsOnlyInPendingStatus() throws InvalidOrderStateException {
        // PENDING - should be able to modify
        assertTrue(order.canModifyItems());
        
        // ACCEPTED - should not be able to modify
        order.accept("Restaurant confirmed");
        assertFalse(order.canModifyItems());
    }

    @ParameterizedTest
    @EnumSource(names = {"DELIVERED", "CANCELLED", "REJECTED"})
    void testTerminalStates(OrderStatus terminalStatus) throws InvalidOrderStateException {
        // Transition to terminal state
        switch (terminalStatus) {
            case DELIVERED -> {
                order.accept("Restaurant confirmed");
                order.startPreparing();
                order.markReadyForDelivery();
                order.assignDriver("driver123");
                order.markDelivered();
            }
            case CANCELLED -> order.cancel("Customer cancelled");
            case REJECTED -> order.reject("Restaurant rejected");
        }
        
        assertTrue(order.isTerminal());
        assertEquals(terminalStatus, order.getStatus());
    }

    @ParameterizedTest
    @EnumSource(names = {"PENDING", "ACCEPTED", "PREPARING", "READY_FOR_DELIVERY", "IN_DELIVERY"})
    void testNonTerminalStates(OrderStatus nonTerminalStatus) throws InvalidOrderStateException {
        // Transition to the specified non-terminal state
        switch (nonTerminalStatus) {
            case PENDING -> { /* Already in PENDING */ }
            case ACCEPTED -> order.accept("Restaurant confirmed");
            case PREPARING -> {
                order.accept("Restaurant confirmed");
                order.startPreparing();
            }
            case READY_FOR_DELIVERY -> {
                order.accept("Restaurant confirmed");
                order.startPreparing();
                order.markReadyForDelivery();
            }
            case IN_DELIVERY -> {
                order.accept("Restaurant confirmed");
                order.startPreparing();
                order.markReadyForDelivery();
                order.assignDriver("driver123");
            }
        }
        
        assertFalse(order.isTerminal());
        assertEquals(nonTerminalStatus, order.getStatus());
    }

    @Test
    void testEstimatedDeliveryTimeCalculation() throws InvalidOrderStateException {
        // Before acceptance, estimated time should be null
        assertNull(order.getEstimatedDeliveryTime());
        
        // After acceptance, should calculate estimated time
        LocalDateTime beforeAcceptance = LocalDateTime.now();
        order.accept("Restaurant confirmed");
        
        LocalDateTime estimatedTime = order.getEstimatedDeliveryTime();
        assertNotNull(estimatedTime);
        assertTrue(estimatedTime.isAfter(beforeAcceptance.plusMinutes(40))); // Should be around 45 minutes
        assertTrue(estimatedTime.isBefore(beforeAcceptance.plusMinutes(50)));
    }

    @Test
    void testSetEstimatedDeliveryTime() {
        LocalDateTime customTime = LocalDateTime.now().plusHours(1);
        order.setEstimatedDeliveryTime(customTime);
        
        assertEquals(customTime, order.getEstimatedDeliveryTime());
    }

    @Test
    void testRecalculateTotalAmount() {
        BigDecimal originalTotal = order.getTotalAmount();
        
        // Get the items list (which is a copy) and modify it properly
        List<OrderItem> items = order.getItems();
        items.get(0).setQuantity(3); // Change pizza quantity from 2 to 3
        
        // Set the items back (which triggers recalculation)
        order.setItems(items);
        
        // Total should now be different: 3 * 15.99 + 1 * 2.99 = 50.96
        assertEquals(0, new BigDecimal("50.96").compareTo(order.getTotalAmount()));
        assertNotEquals(0, originalTotal.compareTo(order.getTotalAmount()));
    }

    @Test
    void testSetItems() {
        OrderItem newItem1 = new OrderItem("new1", "Burger", 1, new BigDecimal("12.99"));
        OrderItem newItem2 = new OrderItem("new2", "Fries", 2, new BigDecimal("3.99"));
        List<OrderItem> newItems = Arrays.asList(newItem1, newItem2);
        
        order.setItems(newItems);
        
        assertEquals(2, order.getItems().size());
        // Total should be 12.99 + (2 * 3.99) = 20.97
        assertEquals(0, new BigDecimal("20.97").compareTo(order.getTotalAmount()));
    }

    @Test
    void testSpecialInstructions() {
        String instructions = "Please ring doorbell twice";
        order.setSpecialInstructions(instructions);
        
        assertEquals(instructions, order.getSpecialInstructions());
    }

    @Test
    void testOrderEqualsAndHashCode() {
        Order order1 = new Order("cust1", "rest1", testItems, "address1");
        Order order2 = new Order("cust2", "rest2", testItems, "address2");
        
        // Orders with different IDs should not be equal
        assertNotEquals(order1, order2);
        assertNotEquals(order1.hashCode(), order2.hashCode());
        
        // Order should be equal to itself
        assertEquals(order1, order1);
        
        // Orders with same ID should be equal
        Order order3 = new Order();
        order3.setOrderId(order1.getOrderId());
        assertEquals(order1, order3);
    }

    @Test
    void testOrderToString() {
        String orderString = order.toString();
        
        assertTrue(orderString.contains(order.getOrderId()));
        assertTrue(orderString.contains(order.getCustomerId()));
        assertTrue(orderString.contains(order.getRestaurantId()));
        assertTrue(orderString.contains(order.getStatus().toString()));
        assertTrue(orderString.contains(order.getTotalAmount().toString()));
    }
}