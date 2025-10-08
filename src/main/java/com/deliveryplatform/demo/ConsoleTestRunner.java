package com.deliveryplatform.demo;

import com.deliveryplatform.controllers.GlobalExceptionHandler;
import com.deliveryplatform.exceptions.*;
import com.deliveryplatform.models.*;
import com.deliveryplatform.repositories.*;
import com.deliveryplatform.services.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * 控制台互動測試執行器
 * 無 GUI 環境下的完整測試方案
 */
public class ConsoleTestRunner {
    
    private OrderService orderService;
    private RestaurantService restaurantService;
    private DeliveryService deliveryService;
    private GlobalExceptionHandler exceptionHandler;
    private OrderLoggingService loggingService;
    
    private InMemoryOrderRepository orderRepository;
    private InMemoryRestaurantRepository restaurantRepository;
    
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("🧪 外賣平台異常處理測試系統 (控制台版)");
        System.out.println("=".repeat(60));
        System.out.println();
        
        ConsoleTestRunner runner = new ConsoleTestRunner();
        runner.initialize();
        runner.showMainMenu();
    }
    
    private void initialize() {
        System.out.println("🔧 正在初始化測試環境...");
        
        // 初始化資料庫
        orderRepository = new InMemoryOrderRepository();
        restaurantRepository = new InMemoryRestaurantRepository();
        loggingService = new OrderLoggingService();
        exceptionHandler = new GlobalExceptionHandler(loggingService);
        
        // 初始化服務
        orderService = new OrderService(orderRepository, loggingService);
        restaurantService = new RestaurantServiceImpl(orderRepository, restaurantRepository, loggingService);
        deliveryService = new DeliveryService(orderRepository, loggingService);
        
        // 建立測試資料
        setupTestData();
        
        System.out.println("✅ 初始化完成！");
        System.out.println();
    }
    
    private void setupTestData() {
        // 建立測試餐廳 (24小時營業，避免時間問題)
        Restaurant restaurant1 = new Restaurant(
                "REST-001", 
                "Pizza Palace", 
                LocalTime.of(0, 0), 
                LocalTime.of(23, 59), 
                10
        );
        restaurant1.setOpen(true);
        restaurantRepository.save(restaurant1);
        
        // 建立關閉的測試餐廳
        Restaurant restaurant2 = new Restaurant(
                "REST-CLOSED", 
                "Closed Restaurant", 
                LocalTime.of(9, 0), 
                LocalTime.of(22, 0), 
                5
        );
        restaurant2.setOpen(false);
        restaurantRepository.save(restaurant2);
        
        System.out.println("📦 測試資料已建立:");
        System.out.println("   - REST-001: Pizza Palace (24小時營業)");
        System.out.println("   - REST-CLOSED: Closed Restaurant (已關閉)");
    }
    
    private void showMainMenu() {
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("📋 主選單 - 請選擇測試類型:");
            System.out.println("=".repeat(50));
            System.out.println("1. 🎯 異常處理測試");
            System.out.println("2. 📦 完整訂單流程測試");
            System.out.println("3. 🔄 自動化測試套件");
            System.out.println("4. 📊 系統狀態檢查");
            System.out.println("5. 📝 查看系統日誌");
            System.out.println("0. 🚪 退出程式");
            System.out.println("=".repeat(50));
            System.out.print("請輸入選項 (0-5): ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1" -> runExceptionTests();
                case "2" -> runOrderLifecycleTest();
                case "3" -> runAutomatedTestSuite();
                case "4" -> showSystemStatus();
                case "5" -> showSystemLogs();
                case "0" -> {
                    System.out.println("\n👋 感謝使用！程式已退出。");
                    return;
                }
                default -> System.out.println("❌ 無效選項，請重新輸入！");
            }
        }
    }
    
    private void runExceptionTests() {
        System.out.println("\n🎯 異常處理測試");
        System.out.println("-".repeat(40));
        
        // 測試1: OrderValidationException
        System.out.println("\n1️⃣ 測試 OrderValidationException");
        try {
            orderService.createOrder(null, "", Arrays.asList(), null);
        } catch (OrderValidationException e) {
            System.out.println("✅ 成功觸發 OrderValidationException");
            System.out.println("   錯誤代碼: " + e.getErrorCode());
            System.out.println("   錯誤數量: " + e.getErrorCount());
            e.getValidationErrors().forEach(error -> 
                System.out.println("   ❌ " + error));
            exceptionHandler.handleOrderValidationException(e);
        }
        
        // 測試2: RestaurantUnavailableException
        System.out.println("\n2️⃣ 測試 RestaurantUnavailableException");
        try {
            restaurantService.acceptOrder("FAKE-ORDER", "REST-CLOSED");
        } catch (RestaurantUnavailableException e) {
            System.out.println("✅ 成功觸發 RestaurantUnavailableException");
            System.out.println("   餐廳ID: " + e.getRestaurantId());
            System.out.println("   錯誤訊息: " + e.getMessage());
            exceptionHandler.handleRestaurantUnavailableException(e);
        } catch (Exception e) {
            System.out.println("🔍 觸發了其他異常: " + e.getClass().getSimpleName());
            System.out.println("   訊息: " + e.getMessage());
        }
        
        // 測試3: InvalidOrderStateException
        System.out.println("\n3️⃣ 測試 InvalidOrderStateException");
        try {
            restaurantService.startPreparingOrder("FAKE-ORDER", "REST-001");
        } catch (InvalidOrderStateException e) {
            System.out.println("✅ 成功觸發 InvalidOrderStateException");
            System.out.println("   訂單ID: " + e.getOrderId());
            System.out.println("   當前狀態: " + e.getCurrentStatus());
            System.out.println("   嘗試狀態: " + e.getAttemptedStatus());
            exceptionHandler.handleInvalidOrderStateException(e);
        } catch (Exception e) {
            System.out.println("🔍 觸發了其他異常: " + e.getClass().getSimpleName());
            System.out.println("   訊息: " + e.getMessage());
        }
        
        // 測試4: DeliveryAssignmentException
        System.out.println("\n4️⃣ 測試 DeliveryAssignmentException");
        // 移除所有司機
        for (int i = 1; i <= 5; i++) {
            deliveryService.removeDriver("DRIVER-00" + i);
        }
        try {
            deliveryService.assignDriver("FAKE-ORDER");
        } catch (DeliveryAssignmentException e) {
            System.out.println("✅ 成功觸發 DeliveryAssignmentException");
            System.out.println("   訂單ID: " + e.getOrderId());
            System.out.println("   失敗原因: " + e.getFailureReason());
            exceptionHandler.handleDeliveryAssignmentException(e);
        } catch (Exception e) {
            System.out.println("🔍 觸發了其他異常: " + e.getClass().getSimpleName());
            System.out.println("   訊息: " + e.getMessage());
        }
        
        System.out.println("\n🎉 異常處理測試完成！");
    }
    
    private void runOrderLifecycleTest() {
        System.out.println("\n📦 完整訂單流程測試");
        System.out.println("-".repeat(40));
        
        try {
            // 步驟1: 建立訂單
            System.out.println("1️⃣ 建立測試訂單...");
            List<OrderItem> items = Arrays.asList(
                new OrderItem("ITEM-001", "Margherita Pizza", 1, new BigDecimal("15.99")),
                new OrderItem("ITEM-002", "Coca Cola", 2, new BigDecimal("2.99"))
            );
            
            Order order = orderService.createOrder(
                "CUST-001", 
                "REST-001", 
                items, 
                "123 Test Street, Test City"
            );
            
            System.out.println("✅ 訂單建立成功!");
            System.out.println("   訂單ID: " + order.getOrderId());
            System.out.println("   總金額: $" + order.getTotalAmount());
            System.out.println("   狀態: " + order.getStatus());
            
            // 步驟2: 餐廳接受訂單
            System.out.println("\n2️⃣ 餐廳接受訂單...");
            try {
                restaurantService.acceptOrder(order.getOrderId(), "REST-001");
                System.out.println("✅ 訂單已被餐廳接受!");
            } catch (Exception e) {
                System.out.println("❌ 接受訂單時發生錯誤: " + e.getMessage());
                throw e;
            }
            
            // 步驟3: 開始準備
            System.out.println("\n3️⃣ 開始準備訂單...");
            try {
                restaurantService.startPreparingOrder(order.getOrderId(), "REST-001");
                System.out.println("✅ 訂單準備中!");
            } catch (Exception e) {
                System.out.println("❌ 準備訂單時發生錯誤: " + e.getMessage());
                throw e;
            }
            
            // 步驟4: 標記準備完成
            System.out.println("\n4️⃣ 標記訂單準備完成...");
            try {
                restaurantService.markOrderReady(order.getOrderId(), "REST-001");
                System.out.println("✅ 訂單準備完成，等待配送!");
            } catch (Exception e) {
                System.out.println("❌ 標記訂單完成時發生錯誤: " + e.getMessage());
                throw e;
            }
            
            // 步驟5: 分配司機
            System.out.println("\n5️⃣ 分配配送司機...");
            // 先確保有可用司機
            deliveryService.addDriver("DRIVER-001");
            try {
                String assignedDriver = deliveryService.assignDriver(order.getOrderId());
                System.out.println("✅ 司機分配成功!");
                System.out.println("   分配司機: " + assignedDriver);
                
                // 步驟6: 完成配送
                System.out.println("\n6️⃣ 完成配送...");
                deliveryService.completeDelivery(order.getOrderId(), assignedDriver);
                System.out.println("✅ 配送完成!");
            } catch (Exception e) {
                System.out.println("❌ 配送過程中發生錯誤: " + e.getMessage());
                throw e;
            }
            
            // 查看最終狀態
            Order finalOrder = orderRepository.findById(order.getOrderId()).orElse(null);
            System.out.println("\n📊 訂單最終狀態: " + (finalOrder != null ? finalOrder.getStatus() : "未找到"));
            
        } catch (Exception e) {
            System.out.println("❌ 測試過程中發生異常:");
            System.out.println("   異常類型: " + e.getClass().getSimpleName());
            System.out.println("   異常訊息: " + e.getMessage());
            
            if (e instanceof DeliveryPlatformException) {
                DeliveryPlatformException dpe = (DeliveryPlatformException) e;
                System.out.println("   錯誤代碼: " + dpe.getErrorCode());
                System.out.println("   時間戳: " + dpe.getTimestamp());
            }
        }
        
        System.out.println("\n🎉 訂單流程測試完成！");
    }
    
    private void runAutomatedTestSuite() {
        System.out.println("\n🔄 自動化測試套件");
        System.out.println("-".repeat(40));
        
        int totalTests = 0;
        int passedTests = 0;
        int failedTests = 0;
        
        // 測試組1: 基本功能測試
        System.out.println("🧪 執行基本功能測試...");
        
        // 測試案例1: 有效訂單建立
        totalTests++;
        try {
            List<OrderItem> items = Arrays.asList(
                new OrderItem("ITEM-001", "Test Pizza", 1, new BigDecimal("10.99"))
            );
            Order order = orderService.createOrder("CUST-TEST", "REST-001", items, "Test Address");
            if (order != null && order.getStatus() == OrderStatus.PENDING) {
                passedTests++;
                System.out.println("  ✅ 有效訂單建立 - 通過");
            } else {
                failedTests++;
                System.out.println("  ❌ 有效訂單建立 - 失敗");
            }
        } catch (Exception e) {
            failedTests++;
            System.out.println("  ❌ 有效訂單建立 - 異常: " + e.getMessage());
        }
        
        // 測試案例2: 無效訂單驗證
        totalTests++;
        try {
            orderService.createOrder(null, null, Arrays.asList(), null);
            failedTests++;
            System.out.println("  ❌ 無效訂單驗證 - 失敗 (應該拋出異常)");
        } catch (OrderValidationException e) {
            passedTests++;
            System.out.println("  ✅ 無效訂單驗證 - 通過");
        } catch (Exception e) {
            failedTests++;
            System.out.println("  ❌ 無效訂單驗證 - 異常類型錯誤: " + e.getClass().getSimpleName());
        }
        
        // 測試案例3: 餐廳狀態檢查
        totalTests++;
        try {
            restaurantService.acceptOrder("FAKE-ORDER", "REST-CLOSED");
            failedTests++;
            System.out.println("  ❌ 餐廳狀態檢查 - 失敗 (應該拋出異常)");
        } catch (RestaurantUnavailableException e) {
            passedTests++;
            System.out.println("  ✅ 餐廳狀態檢查 - 通過");
        } catch (Exception e) {
            failedTests++;
            System.out.println("  ❌ 餐廳狀態檢查 - 異常類型錯誤: " + e.getClass().getSimpleName());
        }
        
        // 顯示測試結果
        System.out.println("\n📊 測試結果統計:");
        System.out.println("   總測試數: " + totalTests);
        System.out.println("   通過: " + passedTests + " ✅");
        System.out.println("   失敗: " + failedTests + " ❌");
        System.out.println("   成功率: " + String.format("%.1f", (double) passedTests / totalTests * 100) + "%");
        
        if (failedTests == 0) {
            System.out.println("\n🎉 所有測試通過！");
        } else {
            System.out.println("\n⚠️ 有 " + failedTests + " 個測試失敗，需要檢查。");
        }
    }
    
    private void showSystemStatus() {
        System.out.println("\n📊 系統狀態檢查");
        System.out.println("-".repeat(40));
        
        // 餐廳狀態
        System.out.println("🏪 餐廳狀態:");
        restaurantRepository.findAll().forEach(restaurant -> {
            System.out.println("   " + restaurant.getRestaurantId() + ": " + 
                             restaurant.getName() + " - " + 
                             (restaurant.isOpen() ? "營業中 ✅" : "已關閉 ❌"));
            System.out.println("      營業時間: " + restaurant.getOpenTime() + 
                             " - " + restaurant.getCloseTime());
            System.out.println("      訂單容量: " + restaurant.getCurrentOrderCount() + 
                             "/" + restaurant.getMaxConcurrentOrders());
        });
        
        // 訂單狀態
        System.out.println("\n📦 訂單統計:");
        // 簡化顯示
        System.out.println("   系統中有訂單資料 (詳細數據請查看日誌)");
        
        // 司機狀態  
        System.out.println("\n🚗 配送司機狀態:");
        System.out.println("   可用司機數: " + deliveryService.getAvailableDriverCount());
        
        System.out.println("\n✅ 系統狀態檢查完成！");
    }
    
    private void showSystemLogs() {
        System.out.println("\n📝 系統日誌");
        System.out.println("-".repeat(40));
        System.out.println("💡 提示: 系統日誌會即時輸出到控制台");
        System.out.println("📋 主要日誌類別:");
        System.out.println("   - INFO: 正常操作記錄");
        System.out.println("   - WARN: 業務異常警告");
        System.out.println("   - ERROR: 系統錯誤");
        System.out.println("\n🔍 請查看上方控制台輸出的日誌資訊");
    }
}