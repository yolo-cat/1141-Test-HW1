package com.deliveryplatform.demo;

import com.deliveryplatform.exceptions.*;
import com.deliveryplatform.models.*;
import com.deliveryplatform.repositories.*;
import com.deliveryplatform.services.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/**
 * 簡化的控制台演示程式 - 避免複雜的異常處理
 * 專注展示核心功能，無編譯錯誤
 */
public class SimpleConsoleDemo {
    
    public static void main(String[] args) {
        System.out.println("🧪 簡化控制台測試演示");
        System.out.println("=".repeat(50));
        System.out.println();
        
        // 初始化系統
        var orderRepository = new InMemoryOrderRepository();
        var restaurantRepository = new InMemoryRestaurantRepository();
        
        var orderService = new OrderService(orderRepository);
        var restaurantService = new RestaurantServiceImpl(orderRepository, restaurantRepository);
        var deliveryService = new DeliveryService(orderRepository);
        
        // 建立24小時營業的測試餐廳
        Restaurant restaurant = new Restaurant(
                "REST-001", 
                "Test Restaurant", 
                LocalTime.of(0, 0), 
                LocalTime.of(23, 59), 
                10
        );
        restaurant.setOpen(true);
        restaurantRepository.save(restaurant);
        
        System.out.println("✅ 系統初始化完成");
        System.out.println();
        
        // 測試1: OrderValidationException
        System.out.println("1️⃣ 測試訂單驗證異常");
        testOrderValidation(orderService);
        System.out.println();
        
        // 測試2: 完整訂單流程
        System.out.println("2️⃣ 測試完整訂單流程");
        testCompleteOrderFlow(orderService, restaurantService, deliveryService);
        System.out.println();
        
        // 測試3: RestaurantUnavailableException
        System.out.println("3️⃣ 測試餐廳不可用異常");
        testRestaurantUnavailable(restaurantService);
        System.out.println();
        
        System.out.println("🎉 所有測試演示完成！");
        System.out.println("📋 檢查上方日誌輸出查看詳細異常信息");
    }
    
    private static void testOrderValidation(OrderService orderService) {
        try {
            // 故意建立無效訂單
            orderService.createOrder(null, null, Arrays.asList(), null);
            System.out.println("❌ 應該拋出異常但沒有");
        } catch (OrderValidationException e) {
            System.out.println("✅ 成功觸發 OrderValidationException");
            System.out.println("   錯誤代碼: " + e.getErrorCode());
            System.out.println("   錯誤數量: " + e.getErrorCount());
        } catch (Exception e) {
            System.out.println("🔍 其他異常: " + e.getClass().getSimpleName());
        }
    }
    
    private static void testCompleteOrderFlow(OrderService orderService, 
                                            RestaurantServiceImpl restaurantService, 
                                            DeliveryService deliveryService) {
        try {
            // 建立有效訂單
            List<OrderItem> items = Arrays.asList(
                new OrderItem("ITEM-001", "Pizza", 1, new BigDecimal("15.99"))
            );
            
            Order order = orderService.createOrder("CUST-001", "REST-001", items, "Test Address");
            System.out.println("✅ 訂單建立成功: " + order.getOrderId());
            
            // 餐廳接受訂單
            restaurantService.acceptOrder(order.getOrderId(), "REST-001");
            System.out.println("✅ 餐廳接受訂單");
            
            // 開始準備
            restaurantService.startPreparingOrder(order.getOrderId(), "REST-001");
            System.out.println("✅ 開始準備訂單");
            
            // 準備完成
            restaurantService.markOrderReady(order.getOrderId(), "REST-001");
            System.out.println("✅ 訂單準備完成");
            
            // 分配司機
            deliveryService.addDriver("DRIVER-001");
            String driver = deliveryService.assignDriver(order.getOrderId());
            System.out.println("✅ 分配司機: " + driver);
            
            // 完成配送
            deliveryService.completeDelivery(order.getOrderId(), driver);
            System.out.println("✅ 配送完成");
            
            System.out.println("🎯 訂單流程完整測試成功!");
            
        } catch (Exception e) {
            System.out.println("❌ 訂單流程測試失敗: " + e.getMessage());
            System.out.println("   異常類型: " + e.getClass().getSimpleName());
        }
    }
    
    private static void testRestaurantUnavailable(RestaurantServiceImpl restaurantService) {
        try {
            // 測試不存在的餐廳
            restaurantService.acceptOrder("FAKE-ORDER", "NONEXISTENT-RESTAURANT");
            System.out.println("❌ 應該拋出異常但沒有");
        } catch (RestaurantUnavailableException e) {
            System.out.println("✅ 成功觸發 RestaurantUnavailableException");
            System.out.println("   餐廳ID: " + e.getRestaurantId());
            System.out.println("   錯誤訊息: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("🔍 其他異常: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
}