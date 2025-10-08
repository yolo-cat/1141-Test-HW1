package com.deliveryplatform.models;

import com.deliveryplatform.exceptions.InvalidOrderStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 訂單模型測試 - 簡化版
 * 測試核心的訂單功能和狀態轉換
 */
class OrderTest {

    private Order order;
    private List<OrderItem> testItems;

    @BeforeEach
    void setUp() {
        testItems = Arrays.asList(
            new OrderItem("ITEM-001", "瑪格麗特披薩", 2, new BigDecimal("15.99")),
            new OrderItem("ITEM-002", "可樂", 1, new BigDecimal("2.50"))
        );
        
        order = new Order(
            "CUST-001", 
            "REST-001", 
            testItems, 
            "台北市信義區信義路五段7號"
        );
    }

    @Test
    void testOrderCreation() {
        assertNotNull(order.getOrderId());
        assertEquals("CUST-001", order.getCustomerId());
        assertEquals("REST-001", order.getRestaurantId());
        assertEquals("台北市信義區信義路五段7號", order.getDeliveryAddress());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertNotNull(order.getCreatedAt());
        
        // 計算總金額: 2 * 15.99 + 1 * 2.50 = 34.48
        BigDecimal expectedTotal = new BigDecimal("34.48");
        assertEquals(0, expectedTotal.compareTo(order.getTotalAmount()));
    }

    @Test
    void testOrderIdGeneration() {
        Order anotherOrder = new Order("CUST-002", "REST-001", testItems, "Another Address");
        assertNotEquals(order.getOrderId(), anotherOrder.getOrderId());
    }

    @Test
    void testAcceptOrder() throws InvalidOrderStateException {
        order.updateStatus(OrderStatus.ACCEPTED);
        assertEquals(OrderStatus.ACCEPTED, order.getStatus());
    }

    @Test
    void testRejectOrder() throws InvalidOrderStateException {
        order.updateStatus(OrderStatus.REJECTED);
        assertEquals(OrderStatus.REJECTED, order.getStatus());
    }

    @Test
    void testCancelOrder() throws InvalidOrderStateException {
        order.updateStatus(OrderStatus.CANCELLED);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void testCompleteOrderLifecycle() throws InvalidOrderStateException {
        // Start with PENDING
        assertEquals(OrderStatus.PENDING, order.getStatus());
        
        // Accept order
        order.updateStatus(OrderStatus.ACCEPTED);
        assertEquals(OrderStatus.ACCEPTED, order.getStatus());
        
        // Start preparing
        order.updateStatus(OrderStatus.PREPARING);
        assertEquals(OrderStatus.PREPARING, order.getStatus());
        
        // Ready for delivery
        order.updateStatus(OrderStatus.READY_FOR_DELIVERY);
        assertEquals(OrderStatus.READY_FOR_DELIVERY, order.getStatus());
        
        // In delivery
        order.updateStatus(OrderStatus.IN_DELIVERY);
        assertEquals(OrderStatus.IN_DELIVERY, order.getStatus());
        
        // Delivered
        order.updateStatus(OrderStatus.DELIVERED);
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    void testInvalidStateTransitions() {
        // Cannot go directly from PENDING to PREPARING
        assertThrows(InvalidOrderStateException.class, () -> 
            order.updateStatus(OrderStatus.PREPARING));
        
        // Cannot go directly from PENDING to DELIVERED
        assertThrows(InvalidOrderStateException.class, () -> 
            order.updateStatus(OrderStatus.DELIVERED));
    }

    @Test
    void testTerminalStates() throws InvalidOrderStateException {
        // Test that terminal states cannot be changed
        // First transition to delivered through valid states
        order.updateStatus(OrderStatus.ACCEPTED);
        order.updateStatus(OrderStatus.PREPARING);
        order.updateStatus(OrderStatus.READY_FOR_DELIVERY);
        order.updateStatus(OrderStatus.IN_DELIVERY);
        order.updateStatus(OrderStatus.DELIVERED);
        
        assertThrows(InvalidOrderStateException.class, () -> 
            order.updateStatus(OrderStatus.CANCELLED));
        
        Order rejectedOrder = new Order("CUST-002", "REST-001", testItems, "Test Address");
        rejectedOrder.updateStatus(OrderStatus.REJECTED);
        assertThrows(InvalidOrderStateException.class, () -> 
            rejectedOrder.updateStatus(OrderStatus.ACCEPTED));
    }

    @Test
    void testValidStateTransitions() {
        assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.ACCEPTED));
        assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.REJECTED));
        assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.CANCELLED));
        
        assertTrue(OrderStatus.ACCEPTED.canTransitionTo(OrderStatus.PREPARING));
        assertTrue(OrderStatus.ACCEPTED.canTransitionTo(OrderStatus.CANCELLED));
        
        assertTrue(OrderStatus.PREPARING.canTransitionTo(OrderStatus.READY_FOR_DELIVERY));
        assertTrue(OrderStatus.READY_FOR_DELIVERY.canTransitionTo(OrderStatus.IN_DELIVERY));
        assertTrue(OrderStatus.IN_DELIVERY.canTransitionTo(OrderStatus.DELIVERED));
    }

    @Test
    void testInvalidStateTransitionValidation() {
        assertFalse(OrderStatus.PENDING.canTransitionTo(OrderStatus.PREPARING));
        assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.CANCELLED));
        assertFalse(OrderStatus.REJECTED.canTransitionTo(OrderStatus.ACCEPTED));
    }

    @Test
    void testOrderItems() {
        assertEquals(2, order.getItems().size());
        
        OrderItem firstItem = order.getItems().get(0);
        assertEquals("ITEM-001", firstItem.getItemId());
        assertEquals("瑪格麗特披薩", firstItem.getName());
        assertEquals(2, firstItem.getQuantity());
        assertEquals(0, new BigDecimal("15.99").compareTo(firstItem.getUnitPrice()));
    }

    @Test
    void testOrderToString() {
        String orderString = order.toString();
        assertNotNull(orderString);
        assertTrue(orderString.contains(order.getOrderId()));
        assertTrue(orderString.contains("CUST-001"));
        assertTrue(orderString.contains("PENDING"));
    }

    @Test
    void testOrderEquality() {
        // Test that two different orders have different IDs
        Order sameOrder = new Order("CUST-001", "REST-001", testItems, "台北市信義區信義路五段7號");
        
        assertNotEquals(order.getOrderId(), sameOrder.getOrderId());
        // Since orderId is different, objects will be different
        assertNotEquals(order, sameOrder);
    }
}