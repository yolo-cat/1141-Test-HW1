package com.deliveryplatform.integration;

import com.deliveryplatform.exceptions.*;
import com.deliveryplatform.models.*;
import com.deliveryplatform.repositories.*;
import com.deliveryplatform.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 整合測試 - 簡化版
 * 測試核心的訂單生命週期功能
 */
class OrderLifecycleIntegrationTest {

    private OrderService orderService;
    private RestaurantService restaurantService;
    private DeliveryService deliveryService;
    
    private InMemoryOrderRepository orderRepository;
    private InMemoryRestaurantRepository restaurantRepository;
    
    private Restaurant testRestaurant;
    private List<OrderItem> testItems;

    @BeforeEach
    void setUp() {
        // Initialize repositories
        orderRepository = new InMemoryOrderRepository();
        restaurantRepository = new InMemoryRestaurantRepository();
        
        // Initialize services (simplified - no OrderLoggingService)
        orderService = new OrderService(orderRepository);
        restaurantService = new RestaurantServiceImpl(orderRepository, restaurantRepository);
        deliveryService = new DeliveryService(orderRepository);
        
        // Set up test data
        setupTestData();
    }

    private void setupTestData() {
        // Create test restaurant - simplified
        testRestaurant = new Restaurant("REST-001", "Test Pizza Place", true);
        restaurantRepository.save(testRestaurant);

        // Create test items
        testItems = Arrays.asList(
            new OrderItem("ITEM-001", "瑪格麗特披薩", 1, new BigDecimal("15.99")),
            new OrderItem("ITEM-002", "可樂", 2, new BigDecimal("2.50"))
        );

        // Add some drivers to delivery service
        deliveryService.addDriver("DRIVER-001");
        deliveryService.addDriver("DRIVER-002");
    }

    /**
     * 測試完整的訂單生命週期：建立 → 接受 → 準備 → 配送 → 完成
     */
    @Test
    void testCompleteOrderLifecycle() throws Exception {
        // 1. 顧客建立訂單
        Order order = orderService.createOrder(
            "CUST-001", 
            "REST-001", 
            testItems, 
            "台北市信義區信義路五段7號"
        );
        assertEquals(OrderStatus.PENDING, order.getStatus());

        // 2. 餐廳接受訂單
        restaurantService.acceptOrder(order.getOrderId(), "REST-001");
        Order updatedOrder = orderRepository.findById(order.getOrderId()).orElse(null);
        assertNotNull(updatedOrder);
        assertEquals(OrderStatus.ACCEPTED, updatedOrder.getStatus());

        // 3. 餐廳開始準備
        restaurantService.startPreparingOrder(order.getOrderId(), "REST-001");
        updatedOrder = orderRepository.findById(order.getOrderId()).orElse(null);
        assertEquals(OrderStatus.PREPARING, updatedOrder.getStatus());

        // 4. 餐廳標記完成
        restaurantService.markOrderReady(order.getOrderId(), "REST-001");
        updatedOrder = orderRepository.findById(order.getOrderId()).orElse(null);
        assertEquals(OrderStatus.READY_FOR_DELIVERY, updatedOrder.getStatus());

        // 5. 分配外送員
        String driverId = deliveryService.assignDriver(order.getOrderId());
        assertNotNull(driverId);
        updatedOrder = orderRepository.findById(order.getOrderId()).orElse(null);
        assertEquals(OrderStatus.IN_DELIVERY, updatedOrder.getStatus());

        // 6. 完成配送
        deliveryService.completeDelivery(order.getOrderId(), driverId);
        updatedOrder = orderRepository.findById(order.getOrderId()).orElse(null);
        assertEquals(OrderStatus.DELIVERED, updatedOrder.getStatus());
    }

    /**
     * 測試餐廳拒絕訂單的情況
     */
    @Test
    void testRestaurantRejectOrder() throws Exception {
        // 建立訂單
        Order order = orderService.createOrder("CUST-002", "REST-001", testItems, "Test Address");
        
        // 餐廳拒絕訂單
        restaurantService.rejectOrder(order.getOrderId(), "REST-001", "餐廳太忙");
        
        Order updatedOrder = orderRepository.findById(order.getOrderId()).orElse(null);
        assertNotNull(updatedOrder);
        assertEquals(OrderStatus.REJECTED, updatedOrder.getStatus());
    }

    /**
     * 測試訂單驗證異常
     */
    @Test
    void testOrderValidationException() {
        // 測試空的項目列表
        assertThrows(OrderValidationException.class, () -> {
            orderService.createOrder("CUST-003", "REST-001", Arrays.asList(), "Test Address");
        });

        // 測試空的地址
        assertThrows(OrderValidationException.class, () -> {
            orderService.createOrder("CUST-003", "REST-001", testItems, null);
        });
    }

    /**
     * 測試餐廳不可用異常
     */
    @Test
    void testRestaurantUnavailableException() throws Exception {
        // 建立訂單
        Order order = orderService.createOrder("CUST-004", "REST-001", testItems, "Test Address");
        
        // 嘗試讓不存在的餐廳接單
        assertThrows(RestaurantUnavailableException.class, () -> {
            restaurantService.acceptOrder(order.getOrderId(), "NONEXISTENT-REST");
        });
    }

    /**
     * 測試無效狀態轉換異常
     */
    @Test
    void testInvalidOrderStateException() throws Exception {
        // 建立訂單
        Order order = orderService.createOrder("CUST-005", "REST-001", testItems, "Test Address");
        
        // 嘗試在未接受訂單的情況下開始準備
        assertThrows(InvalidOrderStateException.class, () -> {
            restaurantService.startPreparingOrder(order.getOrderId(), "REST-001");
        });
    }

    /**
     * 測試配送分配異常
     */
    @Test
    void testDeliveryAssignmentException() throws Exception {
        // 建立並推進到準備配送狀態
        Order order = orderService.createOrder("CUST-006", "REST-001", testItems, "Test Address");
        restaurantService.acceptOrder(order.getOrderId(), "REST-001");
        restaurantService.startPreparingOrder(order.getOrderId(), "REST-001");
        restaurantService.markOrderReady(order.getOrderId(), "REST-001");
        
        // 清空所有司機
        DeliveryService emptyDeliveryService = new DeliveryService(orderRepository);
        // Remove all drivers that were automatically added
        for (int i = 0; i < 10; i++) {  // Remove enough drivers to empty the pool
            emptyDeliveryService.removeDriver("DRIVER-00" + (i + 1));
        }
        
        // 嘗試分配司機應該失敗
        assertThrows(DeliveryAssignmentException.class, () -> {
            emptyDeliveryService.assignDriver(order.getOrderId());
        });
    }
}