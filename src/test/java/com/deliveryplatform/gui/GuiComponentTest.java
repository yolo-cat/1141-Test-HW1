package com.deliveryplatform.gui;

import com.deliveryplatform.exceptions.*;
import com.deliveryplatform.models.*;
import com.deliveryplatform.services.*;
import com.deliveryplatform.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GUI 組件測試類
 * 驗證 GUI 應用程式的核心業務邏輯是否正常工作
 */
class GuiComponentTest {

    private OrderService orderService;
    private RestaurantService restaurantService;
    private DeliveryService deliveryService;
    private OrderLoggingService loggingService;
    private InMemoryOrderRepository orderRepository;
    private InMemoryRestaurantRepository restaurantRepository;

    @BeforeEach
    void setUp() {
        // 初始化與 GUI 應用程式相同的組件
        orderRepository = new InMemoryOrderRepository();
        restaurantRepository = new InMemoryRestaurantRepository();
        loggingService = new OrderLoggingService();
        
        orderService = new OrderService(orderRepository, loggingService);
        restaurantService = new RestaurantServiceImpl(orderRepository, restaurantRepository, loggingService);
        deliveryService = new DeliveryService(orderRepository, loggingService);
        
        // 設置測試資料
        setupTestData();
    }

    private void setupTestData() {
        // 創建測試餐廳
        Restaurant testRestaurant = new Restaurant(
            "REST-001", 
            "測試披薩店", 
            LocalTime.of(9, 0), 
            LocalTime.of(22, 0), 
            5
        );
        restaurantRepository.save(testRestaurant);
        
        Restaurant unavailableRestaurant = new Restaurant(
            "REST-CLOSED", 
            "已關閉餐廳", 
            LocalTime.of(9, 0), 
            LocalTime.of(17, 0), 
            0
        );
        unavailableRestaurant.setOpen(false);
        restaurantRepository.save(unavailableRestaurant);
    }

    @Test
    void testOrderCreationScenario() throws OrderValidationException {
        // 模擬 GUI 中的訂單創建場景
        List<OrderItem> items = Arrays.asList(
            new OrderItem("ITEM-001", "瑪格莉特披薩", 2, new BigDecimal("15.99")),
            new OrderItem("ITEM-002", "可樂", 1, new BigDecimal("2.99"))
        );
        
        Order order = orderService.createOrder(
            "CUST-001", 
            "REST-001", 
            items, 
            "台北市大安區信義路四段1號"
        );
        
        assertNotNull(order);
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals("CUST-001", order.getCustomerId());
        assertEquals("REST-001", order.getRestaurantId());
        assertEquals(0, new BigDecimal("34.97").compareTo(order.getTotalAmount()));
        
        System.out.println("✅ 訂單創建測試通過: " + order.getOrderId());
    }

    @Test
    void testRestaurantOperationsScenario() throws Exception {
        // 創建測試訂單
        List<OrderItem> items = Arrays.asList(
            new OrderItem("ITEM-001", "測試餐點", 1, new BigDecimal("10.00"))
        );
        Order order = orderService.createOrder("CUST-002", "REST-001", items, "測試地址");
        String orderId = order.getOrderId();
        
        // 測試餐廳操作流程
        restaurantService.acceptOrder(orderId, "REST-001");
        Order acceptedOrder = orderRepository.findById(orderId).orElseThrow();
        assertEquals(OrderStatus.ACCEPTED, acceptedOrder.getStatus());
        
        restaurantService.startPreparingOrder(orderId, "REST-001");
        Order preparingOrder = orderRepository.findById(orderId).orElseThrow();
        assertEquals(OrderStatus.PREPARING, preparingOrder.getStatus());
        
        restaurantService.markOrderReady(orderId, "REST-001");
        Order readyOrder = orderRepository.findById(orderId).orElseThrow();
        assertEquals(OrderStatus.READY_FOR_DELIVERY, readyOrder.getStatus());
        
        System.out.println("✅ 餐廳操作測試通過: " + orderId);
    }

    @Test
    void testDeliveryOperationsScenario() throws Exception {
        // 創建並準備一個訂單到配送階段
        List<OrderItem> items = Arrays.asList(
            new OrderItem("ITEM-001", "測試餐點", 1, new BigDecimal("10.00"))
        );
        Order order = orderService.createOrder("CUST-003", "REST-001", items, "測試地址");
        String orderId = order.getOrderId();
        
        // 完成餐廳階段
        restaurantService.acceptOrder(orderId, "REST-001");
        restaurantService.startPreparingOrder(orderId, "REST-001");
        restaurantService.markOrderReady(orderId, "REST-001");
        
        // 測試配送操作
        String driverId = deliveryService.assignDriver(orderId);
        assertNotNull(driverId);
        
        Order inDeliveryOrder = orderRepository.findById(orderId).orElseThrow();
        assertEquals(OrderStatus.IN_DELIVERY, inDeliveryOrder.getStatus());
        assertEquals(driverId, inDeliveryOrder.getDriverId());
        
        deliveryService.completeDelivery(orderId, driverId);
        Order deliveredOrder = orderRepository.findById(orderId).orElseThrow();
        assertEquals(OrderStatus.DELIVERED, deliveredOrder.getStatus());
        
        System.out.println("✅ 配送操作測試通過: " + orderId + " (司機: " + driverId + ")");
    }

    @Test
    void testOrderValidationExceptionScenario() {
        // 測試 GUI 中的 OrderValidationException 觸發場景
        System.out.println("🎯 測試 OrderValidationException 觸發...");
        
        Exception exception = assertThrows(OrderValidationException.class, () -> {
            orderService.createOrder(null, "", List.of(), null);
        });
        
        assertTrue(exception instanceof OrderValidationException);
        OrderValidationException validationException = (OrderValidationException) exception;
        
        assertEquals("ORDER_VALIDATION_FAILED", validationException.getErrorCode());
        assertTrue(validationException.getErrorCount() > 0);
        assertNotNull(validationException.getTimestamp());
        
        System.out.println("   錯誤代碼: " + validationException.getErrorCode());
        System.out.println("   錯誤數量: " + validationException.getErrorCount());
        System.out.println("   錯誤列表: " + validationException.getValidationErrors());
        System.out.println("✅ OrderValidationException 測試通過");
    }

    @Test
    void testRestaurantUnavailableExceptionScenario() throws OrderValidationException {
        // 測試 GUI 中的 RestaurantUnavailableException 觸發場景
        System.out.println("🎯 測試 RestaurantUnavailableException 觸發...");
        
        List<OrderItem> items = Arrays.asList(
            new OrderItem("ITEM-001", "測試餐點", 1, new BigDecimal("10.00"))
        );
        Order order = orderService.createOrder("CUST-004", "REST-CLOSED", items, "測試地址");
        
        Exception exception = assertThrows(RestaurantUnavailableException.class, () -> {
            restaurantService.acceptOrder(order.getOrderId(), "REST-CLOSED");
        });
        
        assertTrue(exception instanceof RestaurantUnavailableException);
        RestaurantUnavailableException unavailableException = (RestaurantUnavailableException) exception;
        
        assertEquals("RESTAURANT_UNAVAILABLE", unavailableException.getErrorCode());
        assertEquals("REST-CLOSED", unavailableException.getRestaurantId());
        assertNotNull(unavailableException.getTimestamp());
        
        System.out.println("   錯誤代碼: " + unavailableException.getErrorCode());
        System.out.println("   餐廳ID: " + unavailableException.getRestaurantId());
        System.out.println("   錯誤訊息: " + unavailableException.getMessage());
        System.out.println("✅ RestaurantUnavailableException 測試通過");
    }

    @Test
    void testInvalidOrderStateExceptionScenario() throws OrderValidationException {
        // 測試 GUI 中的 InvalidOrderStateException 觸發場景
        System.out.println("🎯 測試 InvalidOrderStateException 觸發...");
        
        List<OrderItem> items = Arrays.asList(
            new OrderItem("ITEM-001", "測試餐點", 1, new BigDecimal("10.00"))
        );
        Order order = orderService.createOrder("CUST-005", "REST-001", items, "測試地址");
        String orderId = order.getOrderId();
        
        // 嘗試跳過 ACCEPTED 狀態直接開始準備
        Exception exception = assertThrows(InvalidOrderStateException.class, () -> {
            restaurantService.startPreparingOrder(orderId, "REST-001");
        });
        
        assertTrue(exception instanceof InvalidOrderStateException);
        InvalidOrderStateException stateException = (InvalidOrderStateException) exception;
        
        assertEquals("INVALID_STATE_TRANSITION", stateException.getErrorCode());
        assertEquals(orderId, stateException.getOrderId());
        assertEquals(OrderStatus.PENDING, stateException.getCurrentStatus());
        assertEquals(OrderStatus.PREPARING, stateException.getAttemptedStatus());
        
        System.out.println("   錯誤代碼: " + stateException.getErrorCode());
        System.out.println("   訂單ID: " + stateException.getOrderId());
        System.out.println("   當前狀態: " + stateException.getCurrentStatus());
        System.out.println("   嘗試狀態: " + stateException.getAttemptedStatus());
        System.out.println("✅ InvalidOrderStateException 測試通過");
    }

    @Test
    void testDeliveryAssignmentExceptionScenario() throws Exception {
        // 測試 GUI 中的 DeliveryAssignmentException 觸發場景
        System.out.println("🎯 測試 DeliveryAssignmentException 觸發...");
        
        // 移除所有司機
        for (int i = 1; i <= 5; i++) {
            deliveryService.removeDriver("DRIVER-00" + i);
        }
        
        // 創建並準備訂單到配送階段
        List<OrderItem> items = Arrays.asList(
            new OrderItem("ITEM-001", "測試餐點", 1, new BigDecimal("10.00"))
        );
        Order order = orderService.createOrder("CUST-006", "REST-001", items, "測試地址");
        String orderId = order.getOrderId();
        
        restaurantService.acceptOrder(orderId, "REST-001");
        restaurantService.startPreparingOrder(orderId, "REST-001");
        restaurantService.markOrderReady(orderId, "REST-001");
        
        // 嘗試分配司機（應該失敗）
        Exception exception = assertThrows(DeliveryAssignmentException.class, () -> {
            deliveryService.assignDriver(orderId);
        });
        
        assertTrue(exception instanceof DeliveryAssignmentException);
        DeliveryAssignmentException assignmentException = (DeliveryAssignmentException) exception;
        
        assertEquals("DELIVERY_ASSIGNMENT_FAILED", assignmentException.getErrorCode());
        assertEquals(orderId, assignmentException.getOrderId());
        assertTrue(assignmentException.getFailureReason().contains("No drivers available"));
        
        System.out.println("   錯誤代碼: " + assignmentException.getErrorCode());
        System.out.println("   訂單ID: " + assignmentException.getOrderId());
        System.out.println("   失敗原因: " + assignmentException.getFailureReason());
        System.out.println("✅ DeliveryAssignmentException 測試通過");
        
        // 恢復司機以供後續測試
        for (int i = 1; i <= 5; i++) {
            deliveryService.addDriver("DRIVER-00" + i);
        }
    }

    @Test
    void testCompleteWorkflowScenario() throws Exception {
        // 測試完整的工作流程（類似 GUI 中的完整操作）
        System.out.println("🔄 測試完整工作流程...");
        
        // 1. 創建訂單
        List<OrderItem> items = Arrays.asList(
            new OrderItem("ITEM-001", "瑪格莉特披薩", 2, new BigDecimal("15.99")),
            new OrderItem("ITEM-002", "可樂", 1, new BigDecimal("2.99"))
        );
        Order order = orderService.createOrder("CUST-007", "REST-001", items, "完整流程測試地址");
        String orderId = order.getOrderId();
        System.out.println("   1️⃣ 訂單創建: " + orderId);
        
        // 2. 餐廳接受訂單
        restaurantService.acceptOrder(orderId, "REST-001");
        System.out.println("   2️⃣ 餐廳接受訂單");
        
        // 3. 開始準備
        restaurantService.startPreparingOrder(orderId, "REST-001");
        System.out.println("   3️⃣ 開始準備餐點");
        
        // 4. 標記就緒
        restaurantService.markOrderReady(orderId, "REST-001");
        System.out.println("   4️⃣ 餐點準備完成");
        
        // 5. 分配司機
        String driverId = deliveryService.assignDriver(orderId);
        System.out.println("   5️⃣ 分配司機: " + driverId);
        
        // 6. 完成配送
        deliveryService.completeDelivery(orderId, driverId);
        System.out.println("   6️⃣ 配送完成");
        
        // 驗證最終狀態
        Order finalOrder = orderRepository.findById(orderId).orElseThrow();
        assertEquals(OrderStatus.DELIVERED, finalOrder.getStatus());
        assertNotNull(finalOrder.getActualDeliveryTime());
        assertTrue(finalOrder.isTerminal());
        
        System.out.println("✅ 完整工作流程測試通過");
        System.out.println("   最終狀態: " + finalOrder.getStatus());
        System.out.println("   配送時間: " + finalOrder.getActualDeliveryTime());
    }
}