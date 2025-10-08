package com.deliveryplatform.services;

import com.deliveryplatform.exceptions.InvalidOrderStateException;
import com.deliveryplatform.exceptions.RestaurantUnavailableException;
import com.deliveryplatform.models.Order;
import com.deliveryplatform.models.OrderItem;
import com.deliveryplatform.models.OrderStatus;
import com.deliveryplatform.models.Restaurant;
import com.deliveryplatform.repositories.InMemoryOrderRepository;
import com.deliveryplatform.repositories.InMemoryRestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 餐廳服務測試 - 簡化版
 * 測試核心的餐廳營運功能
 */
class RestaurantServiceTest {

    private RestaurantService restaurantService;
    private InMemoryOrderRepository orderRepository;
    private InMemoryRestaurantRepository restaurantRepository;
    
    private Order testOrder;
    private Restaurant testRestaurant;

    @BeforeEach
    void setUp() {
        orderRepository = new InMemoryOrderRepository();
        restaurantRepository = new InMemoryRestaurantRepository();
        
        // Initialize service (simplified - no OrderLoggingService)
        restaurantService = new RestaurantServiceImpl(orderRepository, restaurantRepository);
        
        // Set up test data
        setupTestData();
    }

    private void setupTestData() {
        // Create test restaurant
        testRestaurant = new Restaurant("REST-001", "Test Pizza Place", true);
        restaurantRepository.save(testRestaurant);

        // Create test order
        List<OrderItem> items = Arrays.asList(
            new OrderItem("ITEM-001", "瑪格麗特披薩", 1, new BigDecimal("15.99"))
        );
        testOrder = new Order("CUST-001", "REST-001", items, "Test Address");
        orderRepository.save(testOrder);
    }

    @Test
    void testAcceptOrder_Success() throws Exception {
        // 測試成功接受訂單
        restaurantService.acceptOrder(testOrder.getOrderId(), "REST-001");
        
        Order updatedOrder = orderRepository.findById(testOrder.getOrderId()).orElse(null);
        assertNotNull(updatedOrder);
        assertEquals(OrderStatus.ACCEPTED, updatedOrder.getStatus());
    }

    @Test
    void testAcceptOrder_RestaurantNotFound() {
        // 測試餐廳不存在的情況
        assertThrows(RestaurantUnavailableException.class, () -> {
            restaurantService.acceptOrder(testOrder.getOrderId(), "NONEXISTENT-REST");
        });
    }

    @Test
    void testAcceptOrder_OrderNotFound() {
        // 測試訂單不存在的情況
        assertThrows(RuntimeException.class, () -> {
            restaurantService.acceptOrder("NONEXISTENT-ORDER", "REST-001");
        });
    }

    @Test
    void testStartPreparingOrder_Success() throws Exception {
        // 先接受訂單
        restaurantService.acceptOrder(testOrder.getOrderId(), "REST-001");
        
        // 開始準備
        restaurantService.startPreparingOrder(testOrder.getOrderId(), "REST-001");
        
        Order updatedOrder = orderRepository.findById(testOrder.getOrderId()).orElse(null);
        assertEquals(OrderStatus.PREPARING, updatedOrder.getStatus());
    }

    @Test
    void testStartPreparingOrder_InvalidState() {
        // 嘗試在未接受訂單的情況下開始準備
        assertThrows(InvalidOrderStateException.class, () -> {
            restaurantService.startPreparingOrder(testOrder.getOrderId(), "REST-001");
        });
    }

    @Test
    void testMarkOrderReady_Success() throws Exception {
        // 推進訂單到準備狀態
        restaurantService.acceptOrder(testOrder.getOrderId(), "REST-001");
        restaurantService.startPreparingOrder(testOrder.getOrderId(), "REST-001");
        
        // 標記準備完成
        restaurantService.markOrderReady(testOrder.getOrderId(), "REST-001");
        
        Order updatedOrder = orderRepository.findById(testOrder.getOrderId()).orElse(null);
        assertEquals(OrderStatus.READY_FOR_DELIVERY, updatedOrder.getStatus());
    }

    @Test
    void testRejectOrder_Success() throws Exception {
        // 測試拒絕訂單
        String rejectionReason = "餐廳太忙";
        restaurantService.rejectOrder(testOrder.getOrderId(), "REST-001", rejectionReason);
        
        Order updatedOrder = orderRepository.findById(testOrder.getOrderId()).orElse(null);
        assertEquals(OrderStatus.REJECTED, updatedOrder.getStatus());
    }

    @Test
    void testRejectOrder_AlreadyAccepted() throws Exception {
        // 先接受訂單
        restaurantService.acceptOrder(testOrder.getOrderId(), "REST-001");
        
        // 嘗試拒絕已接受的訂單應該失敗
        assertThrows(InvalidOrderStateException.class, () -> {
            restaurantService.rejectOrder(testOrder.getOrderId(), "REST-001", "Changed mind");
        });
    }

    @Test
    void testCompleteOrderWorkflow() throws Exception {
        // 測試完整的餐廳工作流程
        assertEquals(OrderStatus.PENDING, testOrder.getStatus());
        
        // 1. 接受訂單
        restaurantService.acceptOrder(testOrder.getOrderId(), "REST-001");
        Order order = orderRepository.findById(testOrder.getOrderId()).orElse(null);
        assertEquals(OrderStatus.ACCEPTED, order.getStatus());
        
        // 2. 開始準備
        restaurantService.startPreparingOrder(testOrder.getOrderId(), "REST-001");
        order = orderRepository.findById(testOrder.getOrderId()).orElse(null);
        assertEquals(OrderStatus.PREPARING, order.getStatus());
        
        // 3. 標記完成
        restaurantService.markOrderReady(testOrder.getOrderId(), "REST-001");
        order = orderRepository.findById(testOrder.getOrderId()).orElse(null);
        assertEquals(OrderStatus.READY_FOR_DELIVERY, order.getStatus());
    }

    @Test
    void testRestaurantMismatch() {
        // 測試餐廳ID不匹配的情況
        assertThrows(RestaurantUnavailableException.class, () -> {
            restaurantService.acceptOrder(testOrder.getOrderId(), "WRONG-REST");
        });
    }
}