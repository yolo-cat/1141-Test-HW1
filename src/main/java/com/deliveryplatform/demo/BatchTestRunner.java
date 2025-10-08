package com.deliveryplatform.demo;

import com.deliveryplatform.exceptions.*;
import com.deliveryplatform.models.*;
import com.deliveryplatform.repositories.*;
import com.deliveryplatform.services.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 批次測試執行器 - 無互動自動測試
 * 適合 CI/CD 環境和自動化測試
 */
public class BatchTestRunner {
    
    private OrderService orderService;
    private RestaurantService restaurantService;
    private DeliveryService deliveryService;
    
    private InMemoryOrderRepository orderRepository;
    private InMemoryRestaurantRepository restaurantRepository;
    private OrderLoggingService loggingService;
    
    private AtomicInteger testCounter = new AtomicInteger(0);
    private AtomicInteger passedCounter = new AtomicInteger(0);
    private AtomicInteger failedCounter = new AtomicInteger(0);
    
    public static void main(String[] args) {
        System.out.println("🤖 批次測試執行器 - 開始執行");
        System.out.println("=" .repeat(60));
        
        BatchTestRunner runner = new BatchTestRunner();
        boolean allPassed = runner.runAllTests();
        
        System.out.println("=" .repeat(60));
        if (allPassed) {
            System.out.println("🎉 所有測試通過！");
            System.exit(0);
        } else {
            System.out.println("❌ 有測試失敗！");
            System.exit(1);
        }
    }
    
    private void initialize() {
        // 初始化服務組件
        orderRepository = new InMemoryOrderRepository();
        restaurantRepository = new InMemoryRestaurantRepository();
        loggingService = new OrderLoggingService();
        
        orderService = new OrderService(orderRepository, loggingService);
        restaurantService = new RestaurantServiceImpl(orderRepository, restaurantRepository, loggingService);
        deliveryService = new DeliveryService(orderRepository, loggingService);
        
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
        
        // 建立關閉的餐廳
        Restaurant closedRestaurant = new Restaurant(
                "REST-CLOSED", 
                "Closed Restaurant", 
                LocalTime.of(9, 0), 
                LocalTime.of(22, 0), 
                5
        );
        closedRestaurant.setOpen(false);
        restaurantRepository.save(closedRestaurant);
    }
    
    public boolean runAllTests() {
        initialize();
        
        // 執行所有測試套件
        testOrderValidationExceptions();
        testRestaurantUnavailableExceptions(); 
        testInvalidOrderStateExceptions();
        testDeliveryAssignmentExceptions();
        testCompleteOrderLifecycle();
        testEdgeCases();
        
        // 顯示測試統計
        printTestResults();
        
        return failedCounter.get() == 0;
    }
    
    private void testOrderValidationExceptions() {
        System.out.println("\n1️⃣ 測試 OrderValidationException");
        System.out.println("-".repeat(40));
        
        // 測試案例 1.1: 空客戶ID
        runTest("空客戶ID", () -> {
            orderService.createOrder(null, "REST-001", 
                Arrays.asList(new OrderItem("ITEM-001", "Pizza", 1, new BigDecimal("10"))), 
                "Address");
        }, OrderValidationException.class);
        
        // 測試案例 1.2: 空餐廳ID  
        runTest("空餐廳ID", () -> {
            orderService.createOrder("CUST-001", null,
                Arrays.asList(new OrderItem("ITEM-001", "Pizza", 1, new BigDecimal("10"))),
                "Address");
        }, OrderValidationException.class);
        
        // 測試案例 1.3: 空訂單項目
        runTest("空訂單項目", () -> {
            orderService.createOrder("CUST-001", "REST-001", Arrays.asList(), "Address");
        }, OrderValidationException.class);
        
        // 測試案例 1.4: 空地址
        runTest("空地址", () -> {
            orderService.createOrder("CUST-001", "REST-001",
                Arrays.asList(new OrderItem("ITEM-001", "Pizza", 1, new BigDecimal("10"))),
                null);
        }, OrderValidationException.class);
    }
    
    private void testRestaurantUnavailableExceptions() {
        System.out.println("\n2️⃣ 測試 RestaurantUnavailableException");
        System.out.println("-".repeat(40));
        
        // 測試案例 2.1: 關閉的餐廳
        runTest("關閉的餐廳", () -> {
            restaurantService.acceptOrder("FAKE-ORDER", "REST-CLOSED");
        }, RestaurantUnavailableException.class);
        
        // 測試案例 2.2: 不存在的餐廳
        runTest("不存在的餐廳", () -> {
            restaurantService.acceptOrder("FAKE-ORDER", "NONEXISTENT");
        }, Exception.class); // 可能是其他異常類型
    }
    
    private void testInvalidOrderStateExceptions() {
        System.out.println("\n3️⃣ 測試 InvalidOrderStateException"); 
        System.out.println("-".repeat(40));
        
        // 測試案例 3.1: 不存在的訂單狀態轉換
        runTest("不存在的訂單", () -> {
            restaurantService.startPreparingOrder("NONEXISTENT-ORDER", "REST-001");
        }, Exception.class);
        
        // 測試案例 3.2: 無效的狀態轉換
        runTest("跳過狀態轉換", () -> {
            // 先建立一個訂單但不接受就直接準備
            List<OrderItem> items = Arrays.asList(
                new OrderItem("ITEM-001", "Pizza", 1, new BigDecimal("10"))
            );
            Order order = orderService.createOrder("CUST-001", "REST-001", items, "Address");
            // 直接準備而不先接受
            restaurantService.startPreparingOrder(order.getOrderId(), "REST-001");
        }, InvalidOrderStateException.class);
    }
    
    private void testDeliveryAssignmentExceptions() {
        System.out.println("\n4️⃣ 測試 DeliveryAssignmentException");
        System.out.println("-".repeat(40));
        
        // 確保沒有可用司機
        for (int i = 1; i <= 10; i++) {
            try {
                deliveryService.removeDriver("DRIVER-00" + i);
            } catch (Exception ignored) {}
        }
        
        // 測試案例 4.1: 無可用司機
        runTest("無可用司機", () -> {
            deliveryService.assignDriver("FAKE-ORDER");
        }, DeliveryAssignmentException.class);
    }
    
    private void testCompleteOrderLifecycle() {
        System.out.println("\n5️⃣ 測試完整訂單流程");
        System.out.println("-".repeat(40));
        
        runTest("完整訂單流程", () -> {
            // 確保有司機可用
            deliveryService.addDriver("DRIVER-001");
            
            // 1. 建立訂單
            List<OrderItem> items = Arrays.asList(
                new OrderItem("ITEM-001", "Pizza", 1, new BigDecimal("15.99"))
            );
            Order order = orderService.createOrder("CUST-001", "REST-001", items, "Test Address");
            
            // 2. 餐廳接受
            restaurantService.acceptOrder(order.getOrderId(), "REST-001");
            
            // 3. 開始準備
            restaurantService.startPreparingOrder(order.getOrderId(), "REST-001");
            
            // 4. 標記準備完成
            restaurantService.markOrderReady(order.getOrderId(), "REST-001");
            
            // 5. 分配司機
            deliveryService.assignDriver(order.getOrderId());
            
            // 6. 完成配送
            deliveryService.completeDelivery(order.getOrderId(), "DRIVER-001");
            
            // 驗證最終狀態
            Order finalOrder = orderRepository.findById(order.getOrderId()).orElse(null);
            if (finalOrder == null || finalOrder.getStatus() != OrderStatus.DELIVERED) {
                throw new RuntimeException("訂單狀態不正確: " + 
                    (finalOrder != null ? finalOrder.getStatus() : "null"));
            }
            
        }, null); // 期望沒有異常
    }
    
    private void testEdgeCases() {
        System.out.println("\n6️⃣ 測試邊界案例");
        System.out.println("-".repeat(40));
        
        // 測試案例 6.1: 負數商品價格
        runTest("負數商品價格", () -> {
            List<OrderItem> items = Arrays.asList(
                new OrderItem("ITEM-001", "Pizza", 1, new BigDecimal("-10"))
            );
            orderService.createOrder("CUST-001", "REST-001", items, "Address");
        }, OrderValidationException.class);
        
        // 測試案例 6.2: 零數量商品
        runTest("零數量商品", () -> {
            List<OrderItem> items = Arrays.asList(
                new OrderItem("ITEM-001", "Pizza", 0, new BigDecimal("10"))
            );
            orderService.createOrder("CUST-001", "REST-001", items, "Address");
        }, OrderValidationException.class);
        
        // 測試案例 6.3: 空商品名稱
        runTest("空商品名稱", () -> {
            List<OrderItem> items = Arrays.asList(
                new OrderItem("ITEM-001", "", 1, new BigDecimal("10"))
            );
            orderService.createOrder("CUST-001", "REST-001", items, "Address");
        }, OrderValidationException.class);
    }
    
    private void runTest(String testName, TestRunnable test, Class<? extends Exception> expectedException) {
        int testNum = testCounter.incrementAndGet();
        System.out.printf("  %d. %s ... ", testNum, testName);
        
        try {
            test.run();
            
            if (expectedException == null) {
                // 期望沒有異常
                passedCounter.incrementAndGet();
                System.out.println("✅ 通過");
            } else {
                // 期望有異常但沒有拋出
                failedCounter.incrementAndGet();
                System.out.println("❌ 失敗 (期望異常但沒有拋出)");
            }
            
        } catch (Exception e) {
            if (expectedException != null && expectedException.isAssignableFrom(e.getClass())) {
                // 拋出了期望的異常
                passedCounter.incrementAndGet();
                System.out.println("✅ 通過 (" + e.getClass().getSimpleName() + ")");
            } else if (expectedException == Exception.class) {
                // 期望任何異常
                passedCounter.incrementAndGet();
                System.out.println("✅ 通過 (" + e.getClass().getSimpleName() + ")");
            } else {
                // 拋出了非期望的異常
                failedCounter.incrementAndGet();
                System.out.println("❌ 失敗 (異常: " + e.getClass().getSimpleName() + 
                    ", 期望: " + (expectedException != null ? expectedException.getSimpleName() : "無") + ")");
                System.out.println("     訊息: " + e.getMessage());
            }
        }
    }
    
    private void printTestResults() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📊 測試結果統計");
        System.out.println("=".repeat(60));
        System.out.println("總測試數: " + testCounter.get());
        System.out.println("通過數: " + passedCounter.get() + " ✅");
        System.out.println("失敗數: " + failedCounter.get() + " ❌");
        System.out.printf("成功率: %.1f%%\n", 
            (double) passedCounter.get() / testCounter.get() * 100);
        
        if (failedCounter.get() == 0) {
            System.out.println("\n🎉 所有測試都通過了！系統運行正常。");
        } else {
            System.out.println("\n⚠️  有 " + failedCounter.get() + " 個測試失敗。");
        }
    }
    
    @FunctionalInterface
    private interface TestRunnable {
        void run() throws Exception;
    }
}