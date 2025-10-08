package com.deliveryplatform.demo;

import com.deliveryplatform.exceptions.*;
import com.deliveryplatform.models.*;
import com.deliveryplatform.repositories.*;
import com.deliveryplatform.services.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/**
 * 核心需求演示程式
 * 
 * 模擬美食外送伺服端功能：
 * 1. 收到顧客訂單
 * 2. 餐廳收單 (acceptOrder)
 * 3. 外送員接單
 * 4. 過程中產生各種例外
 * 5. 使用 Log4j2 記錄不同級別的日誌
 * 6. 設計 Checked Exception (業務邏輯錯誤)
 * 7. 處理 Unchecked Exception (程式碼邏輯或環境錯誤)
 * 8. 透過 Enum 設計訂單狀態 (PENDING 等)
 * 9. Order 訂單類別設計
 * 10. 客製化 Exception 設計
 */
public class CoreRequirementsDemo {
    
    private static final Logger logger = LogManager.getLogger(CoreRequirementsDemo.class);
    
    private OrderService orderService;
    private RestaurantServiceImpl restaurantService;
    private DeliveryService deliveryService;
    
    public static void main(String[] args) {
        logger.info("=== 美食外送伺服端功能演示開始 ===");
        
        CoreRequirementsDemo demo = new CoreRequirementsDemo();
        demo.initializeServices();
        
        // 演示各項核心需求
        demo.demonstrateCustomerOrder();           // 1. 顧客訂單
        demo.demonstrateRestaurantAcceptOrder();   // 2. 餐廳收單
        demo.demonstrateDeliveryAssignment();      // 3. 外送員接單
        demo.demonstrateExceptionHandling();      // 4. 例外處理
        demo.demonstrateLogLevels();              // 5. Log4j2 日誌級別
        
        logger.info("=== 美食外送伺服端功能演示結束 ===");
    }
    
    private void initializeServices() {
        logger.info("初始化服務組件...");
        
        // 建立資料庫
        InMemoryOrderRepository orderRepository = new InMemoryOrderRepository();
        InMemoryRestaurantRepository restaurantRepository = new InMemoryRestaurantRepository();
        
        // 建立服務 (已簡化，移除 OrderLoggingService)
        orderService = new OrderService(orderRepository);
        restaurantService = new RestaurantServiceImpl(orderRepository, restaurantRepository);
        deliveryService = new DeliveryService(orderRepository);
        
        // 建立測試餐廳 (24小時營業)
        Restaurant restaurant = new Restaurant(
                "REST-001", 
                "美味披薩店", 
                LocalTime.of(0, 0), 
                LocalTime.of(23, 59), 
                10
        );
        restaurant.setOpen(true);
        restaurantRepository.save(restaurant);
        
        // 建立關閉的餐廳
        Restaurant closedRestaurant = new Restaurant(
                "REST-002", 
                "已關閉的餐廳", 
                LocalTime.of(9, 0), 
                LocalTime.of(22, 0), 
                5
        );
        closedRestaurant.setOpen(false);
        restaurantRepository.save(closedRestaurant);
        
        logger.info("服務組件初始化完成");
    }
    
    /**
     * 1. 演示顧客訂單功能
     * - Order 訂單類別設計
     * - Enum 設計訂單狀態 (PENDING 等)
     */
    private void demonstrateCustomerOrder() {
        logger.info("=== 1. 演示顧客訂單功能 ===");
        
        try {
            // 建立訂單項目
            List<OrderItem> items = Arrays.asList(
                new OrderItem("ITEM-001", "瑪格麗特披薩", 2, new BigDecimal("18.90")),
                new OrderItem("ITEM-002", "可樂", 1, new BigDecimal("3.50"))
            );
            
            // 顧客下單 - 使用 Order 類別和 PENDING 狀態
            Order order = orderService.createOrder(
                "CUST-001", 
                "REST-001", 
                items, 
                "台北市信義區信義路五段7號"
            );
            
            // INFO 級別日誌 - 正常業務操作
            logger.info("顧客訂單建立成功: orderId={}, status={}, amount=${}", 
                       order.getOrderId(), order.getStatus(), order.getTotalAmount());
            
            // 展示 Enum 狀態轉換驗證
            logger.info("目前訂單狀態: {}", order.getStatus());
            logger.info("可轉換到的狀態: {}", order.getStatus().getValidTransitions());
            
        } catch (OrderValidationException e) {
            // WARN 級別日誌 - 業務邏輯錯誤 (Checked Exception)
            logger.warn("顧客訂單驗證失敗: errorCode={}, message={}", 
                       e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            // ERROR 級別日誌 - 系統錯誤 (Unchecked Exception)
            logger.error("顧客訂單建立發生系統錯誤", e);
        }
    }
    
    /**
     * 2. 演示餐廳收單功能 (acceptOrder)
     * - 客製化 Exception 設計
     * - Checked Exception (業務邏輯錯誤)
     */
    private void demonstrateRestaurantAcceptOrder() {
        logger.info("=== 2. 演示餐廳 acceptOrder() 功能 ===");
        
        try {
            // 先建立一個測試訂單
            List<OrderItem> items = Arrays.asList(
                new OrderItem("ITEM-003", "夏威夷披薩", 1, new BigDecimal("21.90"))
            );
            
            Order order = orderService.createOrder(
                "CUST-002", 
                "REST-001", 
                items, 
                "台北市大安區復興南路一段390號"
            );
            
            // 餐廳接單 - 可能拋出 RestaurantUnavailableException
            restaurantService.acceptOrder(order.getOrderId(), "REST-001");
            
            // INFO 級別日誌 - 正常業務操作
            logger.info("餐廳成功接單: orderId={}, restaurantId={}", 
                       order.getOrderId(), "REST-001");
            
            // 展示狀態轉換: PENDING → ACCEPTED
            // 注意：在實際應用中，我們會查詢更新後的訂單狀態
            logger.info("訂單狀態已更新: {} → {}", 
                       OrderStatus.PENDING, OrderStatus.ACCEPTED);
            
        } catch (RestaurantUnavailableException e) {
            // WARN 級別日誌 - 業務邏輯錯誤 (Checked Exception)
            logger.warn("餐廳無法接單: restaurantId={}, errorCode={}, message={}", 
                       e.getRestaurantId(), e.getErrorCode(), e.getMessage());
        } catch (InvalidOrderStateException e) {
            // WARN 級別日誌 - 業務邏輯錯誤 (Checked Exception)  
            logger.warn("訂單狀態轉換錯誤: orderId={}, currentStatus={}, attemptedStatus={}", 
                       e.getOrderId(), e.getCurrentStatus(), e.getAttemptedStatus());
        } catch (Exception e) {
            // ERROR 級別日誌 - 系統錯誤 (Unchecked Exception)
            logger.error("餐廳接單過程發生系統錯誤", e);
        }
    }
    
    /**
     * 3. 演示外送員接單功能
     * - DeliveryAssignmentException 設計
     */
    private void demonstrateDeliveryAssignment() {
        logger.info("=== 3. 演示外送員接單功能 ===");
        
        try {
            // 建立並完成訂單準備流程
            List<OrderItem> items = Arrays.asList(
                new OrderItem("ITEM-004", "義大利肉醬麵", 1, new BigDecimal("16.50"))
            );
            
            Order order = orderService.createOrder("CUST-003", "REST-001", items, 
                                                  "台北市中山區南京東路二段123號");
            
            // 餐廳處理流程
            restaurantService.acceptOrder(order.getOrderId(), "REST-001");
            restaurantService.startPreparingOrder(order.getOrderId(), "REST-001");
            restaurantService.markOrderReady(order.getOrderId(), "REST-001");
            
            // 新增外送員
            deliveryService.addDriver("DRIVER-001");
            
            // 外送員接單
            String assignedDriver = deliveryService.assignDriver(order.getOrderId());
            
            // INFO 級別日誌 - 正常業務操作
            logger.info("外送員成功接單: orderId={}, driverId={}", 
                       order.getOrderId(), assignedDriver);
            
            // 完成配送
            deliveryService.completeDelivery(order.getOrderId(), assignedDriver);
            
            // 展示完整狀態轉換鏈
            logger.info("訂單完整生命週期: PENDING → ACCEPTED → PREPARING → READY_FOR_DELIVERY → IN_DELIVERY → DELIVERED");
            
        } catch (DeliveryAssignmentException e) {
            // WARN 級別日誌 - 業務邏輯錯誤 (Checked Exception)
            logger.warn("外送員分配失敗: orderId={}, reason={}, errorCode={}", 
                       e.getOrderId(), e.getFailureReason(), e.getErrorCode());
        } catch (Exception e) {
            // ERROR 級別日誌 - 系統錯誤 (Unchecked Exception)
            logger.error("外送員接單過程發生系統錯誤", e);
        }
    }
    
    /**
     * 4. 演示各種例外處理
     * - Checked Exception vs Unchecked Exception
     * - 客製化 Exception 設計
     */
    private void demonstrateExceptionHandling() {
        logger.info("=== 4. 演示各種例外處理 ===");
        
        // 4.1 OrderValidationException (Checked Exception - 業務邏輯錯誤)
        try {
            logger.info("4.1 測試 OrderValidationException (Checked Exception)");
            orderService.createOrder(null, "", Arrays.asList(), null);
            
        } catch (OrderValidationException e) {
            logger.warn("捕獲 OrderValidationException: errorCode={}, errorCount={}", 
                       e.getErrorCode(), e.getErrorCount());
            logger.warn("驗證錯誤詳情: {}", e.getValidationErrors());
        }
        
        // 4.2 RestaurantUnavailableException (Checked Exception - 業務邏輯錯誤)
        try {
            logger.info("4.2 測試 RestaurantUnavailableException (Checked Exception)");
            restaurantService.acceptOrder("FAKE-ORDER", "REST-002"); // 關閉的餐廳
            
        } catch (RestaurantUnavailableException e) {
            logger.warn("捕獲 RestaurantUnavailableException: restaurantId={}, errorCode={}", 
                       e.getRestaurantId(), e.getErrorCode());
        } catch (Exception e) {
            logger.error("其他系統錯誤", e);
        }
        
        // 4.3 InvalidOrderStateException (Checked Exception - 業務邏輯錯誤)
        try {
            logger.info("4.3 測試 InvalidOrderStateException (Checked Exception)");
            restaurantService.startPreparingOrder("NONEXISTENT-ORDER", "REST-001");
            
        } catch (InvalidOrderStateException e) {
            logger.warn("捕獲 InvalidOrderStateException: orderId={}, currentStatus={}, attemptedStatus={}", 
                       e.getOrderId(), e.getCurrentStatus(), e.getAttemptedStatus());
        } catch (IllegalArgumentException e) {
            // Unchecked Exception - 程式碼邏輯錯誤
            logger.error("捕獲 IllegalArgumentException (Unchecked Exception): {}", e.getMessage());
        } catch (Exception e) {
            logger.error("其他系統錯誤", e);
        }
        
        // 4.4 DeliveryAssignmentException (Checked Exception - 業務邏輯錯誤)
        try {
            logger.info("4.4 測試 DeliveryAssignmentException (Checked Exception)");
            // 移除所有司機
            for (int i = 1; i <= 5; i++) {
                try {
                    deliveryService.removeDriver("DRIVER-00" + i);
                } catch (Exception ignored) {}
            }
            deliveryService.assignDriver("FAKE-ORDER");
            
        } catch (DeliveryAssignmentException e) {
            logger.warn("捕獲 DeliveryAssignmentException: orderId={}, reason={}", 
                       e.getOrderId(), e.getFailureReason());
        } catch (Exception e) {
            logger.error("其他系統錯誤", e);
        }
        
        // 4.5 模擬 Unchecked Exception (程式碼邏輯或環境錯誤)
        try {
            logger.info("4.5 模擬 Unchecked Exception (程式碼邏輯錯誤)");
            String nullString = null;
            int length = nullString.length(); // 故意觸發 NullPointerException
            
        } catch (NullPointerException e) {
            logger.error("捕獲 NullPointerException (Unchecked Exception): 程式碼邏輯錯誤", e);
        } catch (RuntimeException e) {
            logger.error("捕獲 RuntimeException (Unchecked Exception): 執行期錯誤", e);
        }
    }
    
    /**
     * 5. 演示 Log4j2 的不同日誌級別使用
     * - INFO: 正常業務操作記錄
     * - WARN: 業務邏輯錯誤 (Checked Exception)
     * - ERROR: 程式碼邏輯或環境錯誤 (Unchecked Exception)
     */
    private void demonstrateLogLevels() {
        logger.info("=== 5. 演示 Log4j2 日誌級別 ===");
        
        // INFO 級別 - 正常業務操作
        logger.info("INFO 級別: 記錄正常的業務操作流程");
        logger.info("INFO 級別: 用戶登入 userId={}", "USER-123");
        logger.info("INFO 級別: 訂單狀態更新 orderId={}, newStatus={}", "ORDER-456", "DELIVERED");
        
        // WARN 級別 - 業務邏輯錯誤 (需要注意但不影響系統運行)
        logger.warn("WARN 級別: 記錄業務邏輯錯誤 (Checked Exception)");
        logger.warn("WARN 級別: 餐廳容量已滿 restaurantId={}, currentOrders={}", "REST-001", 10);
        logger.warn("WARN 級別: 訂單驗證失敗 orderId={}, reason={}", "ORDER-789", "無效的地址格式");
        
        // ERROR 級別 - 系統錯誤 (程式碼邏輯或環境錯誤)
        logger.error("ERROR 級別: 記錄系統錯誤 (Unchecked Exception)");
        logger.error("ERROR 級別: 資料庫連線失敗");
        logger.error("ERROR 級別: 外部 API 呼叫超時 service={}, timeout={}ms", "PaymentService", 5000);
        
        // 總結日誌級別使用原則
        logger.info("日誌級別使用原則總結:");
        logger.info("- INFO: 記錄正常業務操作，用於追蹤系統執行流程");
        logger.info("- WARN: 記錄 Checked Exception (業務邏輯錯誤)，需要關注但不影響系統運行");
        logger.info("- ERROR: 記錄 Unchecked Exception (程式碼邏輯或環境錯誤)，嚴重問題需要立即處理");
        
        logger.info("透過這些日誌，我們可以發現後端執行過程中發生的各種問題！");
    }
}