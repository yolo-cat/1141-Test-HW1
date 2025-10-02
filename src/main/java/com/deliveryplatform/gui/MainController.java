package com.deliveryplatform.gui;

import com.deliveryplatform.exceptions.*;
import com.deliveryplatform.models.*;
import com.deliveryplatform.services.*;
import com.deliveryplatform.repositories.*;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.concurrent.Task;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * JavaFX 主控制器
 * 處理 GUI 事件並演示異常情況
 */
public class MainController implements Initializable {

    // 服務組件
    private OrderService orderService;
    private RestaurantService restaurantService;
    private DeliveryService deliveryService;
    private OrderLoggingService loggingService;
    
    // 資料庫模擬
    private InMemoryOrderRepository orderRepository;
    private InMemoryRestaurantRepository restaurantRepository;
    
    // GUI 組件
    @FXML private TabPane mainTabPane;
    
    // 訂單創建標籤
    @FXML private TextField customerIdField;
    @FXML private TextField restaurantIdField;
    @FXML private TextField deliveryAddressField;
    @FXML private VBox orderItemsContainer;
    @FXML private Button createOrderBtn;
    
    // 餐廳操作標籤
    @FXML private TextField orderIdField;
    @FXML private TextField restaurantOperationIdField;
    @FXML private TextArea rejectionReasonArea;
    @FXML private Button acceptOrderBtn;
    @FXML private Button rejectOrderBtn;
    @FXML private Button startPreparingBtn;
    @FXML private Button markReadyBtn;
    
    // 配送操作標籤
    @FXML private TextField deliveryOrderIdField;
    @FXML private TextField driverIdField;
    @FXML private Button assignDriverBtn;
    @FXML private Button completeDeliveryBtn;
    
    // 異常測試標籤
    @FXML private ComboBox<String> exceptionTypeCombo;
    @FXML private TextField testOrderIdField;
    @FXML private TextField testRestaurantIdField;
    @FXML private Button triggerExceptionBtn;
    
    // 日誌顯示
    @FXML private TextArea logDisplayArea;
    @FXML private Button clearLogBtn;
    @FXML private Button refreshLogBtn;
    
    // 系統狀態
    @FXML private Label statusLabel;
    @FXML private ProgressBar operationProgress;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeServices();
        initializeGUI();
        initializeTestData();
        startLogMonitoring();
    }
    
    /**
     * 初始化服務組件
     */
    private void initializeServices() {
        // 初始化資料庫
        orderRepository = new InMemoryOrderRepository();
        restaurantRepository = new InMemoryRestaurantRepository();
        loggingService = new OrderLoggingService();
        
        // 初始化服務
        orderService = new OrderService(orderRepository, loggingService);
        restaurantService = new RestaurantServiceImpl(orderRepository, restaurantRepository, loggingService);
        deliveryService = new DeliveryService(orderRepository, loggingService);
    }
    
    /**
     * 初始化 GUI 組件
     */
    private void initializeGUI() {
        // 設置異常類型選擇器 (如果存在)
        if (exceptionTypeCombo != null) {
            exceptionTypeCombo.getItems().addAll(
                "OrderValidationException - 訂單驗證錯誤",
                "RestaurantUnavailableException - 餐廳不可用",
                "InvalidOrderStateException - 無效狀態轉換",
                "DeliveryAssignmentException - 配送分配失敗"
            );
            exceptionTypeCombo.setValue(exceptionTypeCombo.getItems().get(0));
        }

        // 設置進度條 (如果存在)
        if (operationProgress != null) {
            operationProgress.setVisible(false);
        }

        // 設置狀態標籤 (如果存在)
        if (statusLabel != null) {
            updateStatus("系統已就緒", false);
        }

        // 設置日誌區域 (如果存在)
        if (logDisplayArea != null) {
            logDisplayArea.setEditable(false);
            logDisplayArea.setWrapText(true);
        }
    }
    
    /**
     * 初始化測試資料
     */
    private void initializeTestData() {
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
            0 // 設置容量為 0，模擬滿載
        );
        unavailableRestaurant.setOpen(false);
        restaurantRepository.save(unavailableRestaurant);
        
        appendLog("測試資料初始化完成");
        updateStatus("測試資料已載入", false);
    }
    
    /**
     * 啟動日誌監控
     */
    private void startLogMonitoring() {
        // 粗野主義風格的啟動日誌
        appendLog("███████████████████████████████████████████████████");
        appendLog("██  外賣平台異常演示系統 - 新粗野主義風格版本  ██");
        appendLog("███████████████████████████████████████████████████");
        appendLog("啟動時間: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        appendLog("系統狀態: 就緒");
        appendLog("可用異常類型: 4 種");
        appendLog("GUI 風格: Neo-Brutalism (新粗野主義)");
        appendLog("═══════════════════════════════════════════════════");
    }

    // ========== 訂單創建相關方法 ==========
    
    @FXML
    private void onCreateOrder() {
        runAsyncOperation("創建訂單", () -> {
            try {
                // 獲取輸入資料
                String customerId = customerIdField.getText().trim();
                String restaurantId = restaurantIdField.getText().trim();
                String deliveryAddress = deliveryAddressField.getText().trim();
                
                // 創建測試訂單項目
                List<OrderItem> items = Arrays.asList(
                    new OrderItem("ITEM-001", "瑪格莉特披薩", 2, new BigDecimal("15.99")),
                    new OrderItem("ITEM-002", "可樂", 1, new BigDecimal("2.99"))
                );
                
                appendLog("\n--- 開始創建訂單 ---");
                appendLog("客戶ID: " + customerId);
                appendLog("餐廳ID: " + restaurantId);
                appendLog("配送地址: " + deliveryAddress);
                
                // 創建訂單
                Order order = orderService.createOrder(customerId, restaurantId, items, deliveryAddress);
                
                appendLog("✅ 訂單創建成功!");
                appendLog("訂單ID: " + order.getOrderId());
                appendLog("總金額: $" + order.getTotalAmount());
                appendLog("狀態: " + order.getStatus());
                
                // 更新 GUI
                Platform.runLater(() -> {
                    orderIdField.setText(order.getOrderId());
                    deliveryOrderIdField.setText(order.getOrderId());
                    testOrderIdField.setText(order.getOrderId());
                });
                
                return "訂單創建成功: " + order.getOrderId();
                
            } catch (OrderValidationException e) {
                appendLog("❌ 訂單驗證失敗!");
                appendLog("錯誤代碼: " + e.getErrorCode());
                appendLog("錯誤訊息: " + e.getMessage());
                appendLog("驗證錯誤列表:");
                e.getValidationErrors().forEach(error -> appendLog("  - " + error));
                throw e;
            }
        });
    }
    
    @FXML
    private void onAddOrderItem() {
        // 添加訂單項目 (簡化實現)
        appendLog("添加訂單項目功能 (演示用)");
    }

    // ========== 餐廳操作相關方法 ==========
    
    @FXML
    private void onAcceptOrder() {
        runAsyncOperation("接受訂單", () -> {
            try {
                String orderId = orderIdField.getText().trim();
                String restaurantId = restaurantOperationIdField.getText().trim();
                
                appendLog("\n--- 餐廳接受訂單 ---");
                appendLog("訂單ID: " + orderId);
                appendLog("餐廳ID: " + restaurantId);
                
                restaurantService.acceptOrder(orderId, restaurantId);
                
                appendLog("✅ 訂單接受成功!");
                return "訂單接受成功";
                
            } catch (RestaurantUnavailableException e) {
                appendLog("❌ 餐廳不可用!");
                appendLog("錯誤代碼: " + e.getErrorCode());
                appendLog("餐廳ID: " + e.getRestaurantId());
                appendLog("錯誤訊息: " + e.getMessage());
                throw e;
            } catch (InvalidOrderStateException e) {
                appendLog("❌ 無效的訂單狀態轉換!");
                appendLog("錯誤代碼: " + e.getErrorCode());
                appendLog("訂單ID: " + e.getOrderId());
                appendLog("當前狀態: " + e.getCurrentStatus());
                appendLog("嘗試狀態: " + e.getAttemptedStatus());
                throw e;
            }
        });
    }
    
    @FXML
    private void onRejectOrder() {
        runAsyncOperation("拒絕訂單", () -> {
            try {
                String orderId = orderIdField.getText().trim();
                String restaurantId = restaurantOperationIdField.getText().trim();
                String reason = rejectionReasonArea.getText().trim();
                
                appendLog("\n--- 餐廳拒絕訂單 ---");
                appendLog("訂單ID: " + orderId);
                appendLog("餐廳ID: " + restaurantId);
                appendLog("拒絕原因: " + reason);
                
                restaurantService.rejectOrder(orderId, restaurantId, reason);
                
                appendLog("✅ 訂單拒絕成功!");
                return "訂單拒絕成功";
                
            } catch (InvalidOrderStateException e) {
                appendLog("❌ 無效的訂單狀態轉換!");
                appendLog("錯誤代碼: " + e.getErrorCode());
                appendLog("訂單ID: " + e.getOrderId());
                appendLog("當前狀態: " + e.getCurrentStatus());
                appendLog("嘗試狀態: " + e.getAttemptedStatus());
                throw e;
            }
        });
    }
    
    @FXML
    private void onStartPreparing() {
        runAsyncOperation("開始準備", () -> {
            try {
                String orderId = orderIdField.getText().trim();
                String restaurantId = restaurantOperationIdField.getText().trim();
                
                appendLog("\n--- 開始準備訂單 ---");
                restaurantService.startPreparingOrder(orderId, restaurantId);
                appendLog("✅ 開始準備訂單!");
                return "開始準備訂單";
                
            } catch (InvalidOrderStateException e) {
                appendLog("❌ 無效的訂單狀態轉換!");
                appendLog("當前狀態: " + e.getCurrentStatus());
                appendLog("嘗試狀態: " + e.getAttemptedStatus());
                throw e;
            }
        });
    }
    
    @FXML
    private void onMarkReady() {
        runAsyncOperation("標記就緒", () -> {
            try {
                String orderId = orderIdField.getText().trim();
                String restaurantId = restaurantOperationIdField.getText().trim();
                
                appendLog("\n--- 標記訂單就緒 ---");
                restaurantService.markOrderReady(orderId, restaurantId);
                appendLog("✅ 訂單已就緒，等待配送!");
                return "訂單標記就緒";
                
            } catch (InvalidOrderStateException e) {
                appendLog("❌ 無效的訂單狀態轉換!");
                throw e;
            }
        });
    }

    // ========== 配送操作相關方法 ==========
    
    @FXML
    private void onAssignDriver() {
        runAsyncOperation("分配司機", () -> {
            try {
                String orderId = deliveryOrderIdField.getText().trim();
                
                appendLog("\n--- 分配配送司機 ---");
                appendLog("訂單ID: " + orderId);
                
                String driverId = deliveryService.assignDriver(orderId);
                
                appendLog("✅ 司機分配成功!");
                appendLog("司機ID: " + driverId);
                
                Platform.runLater(() -> driverIdField.setText(driverId));
                return "司機分配成功: " + driverId;
                
            } catch (DeliveryAssignmentException e) {
                appendLog("❌ 配送分配失敗!");
                appendLog("錯誤代碼: " + e.getErrorCode());
                appendLog("訂單ID: " + e.getOrderId());
                appendLog("失敗原因: " + e.getFailureReason());
                throw e;
            } catch (InvalidOrderStateException e) {
                appendLog("❌ 無效的訂單狀態!");
                appendLog("當前狀態: " + e.getCurrentStatus());
                throw e;
            }
        });
    }
    
    @FXML
    private void onCompleteDelivery() {
        runAsyncOperation("完成配送", () -> {
            try {
                String orderId = deliveryOrderIdField.getText().trim();
                String driverId = driverIdField.getText().trim();
                
                appendLog("\n--- 完成配送 ---");
                appendLog("訂單ID: " + orderId);
                appendLog("司機ID: " + driverId);
                
                deliveryService.completeDelivery(orderId, driverId);
                
                appendLog("✅ 配送完成!");
                return "配送完成";
                
            } catch (InvalidOrderStateException e) {
                appendLog("❌ 無效的訂單狀態!");
                throw e;
            }
        });
    }

    // ========== 異常測試相關方法 ==========
    
    @FXML
    private void onTriggerException() {
        String selectedType = exceptionTypeCombo.getValue();
        String orderId = testOrderIdField.getText().trim();
        String restaurantId = testRestaurantIdField.getText().trim();
        
        appendLog("\n=== 觸發異常測試 ===");
        appendLog("異常類型: " + selectedType);
        
        if (selectedType.contains("OrderValidationException")) {
            triggerOrderValidationException();
        } else if (selectedType.contains("RestaurantUnavailableException")) {
            triggerRestaurantUnavailableException(restaurantId);
        } else if (selectedType.contains("InvalidOrderStateException")) {
            triggerInvalidOrderStateException(orderId);
        } else if (selectedType.contains("DeliveryAssignmentException")) {
            triggerDeliveryAssignmentException(orderId);
        }
    }
    
    private void triggerOrderValidationException() {
        runAsyncOperation("觸發訂單驗證異常", () -> {
            try {
                // 故意傳入無效資料
                appendLog("嘗試創建無效訂單 (缺少必要欄位)...");
                orderService.createOrder(null, "", List.of(), null);
                return "不應該到達這裡";
            } catch (OrderValidationException e) {
                appendLog("🎯 成功觸發 OrderValidationException!");
                appendLog("錯誤代碼: " + e.getErrorCode());
                appendLog("錯誤數量: " + e.getErrorCount());
                appendLog("詳細錯誤:");
                e.getValidationErrors().forEach(error -> appendLog("  ❌ " + error));
                return "OrderValidationException 已觸發";
            }
        });
    }
    
    private void triggerRestaurantUnavailableException(String restaurantId) {
        runAsyncOperation("觸發餐廳不可用異常", () -> {
            try {
                // 使用關閉的餐廳
                appendLog("嘗試向關閉的餐廳分配訂單...");
                restaurantService.acceptOrder("TEST-ORDER", "REST-CLOSED");
                return "不應該到達這裡";
            } catch (RestaurantUnavailableException e) {
                appendLog("🎯 成功觸發 RestaurantUnavailableException!");
                appendLog("錯誤代碼: " + e.getErrorCode());
                appendLog("餐廳ID: " + e.getRestaurantId());
                appendLog("錯誤訊息: " + e.getMessage());
                return "RestaurantUnavailableException 已觸發";
            } catch (Exception e) {
                appendLog("觸發了其他異常: " + e.getClass().getSimpleName());
                return e.getMessage();
            }
        });
    }
    
    private void triggerInvalidOrderStateException(String orderId) {
        runAsyncOperation("觸發無效狀態轉換異常", () -> {
            try {
                // 嘗試無效的狀態轉換
                appendLog("嘗試在訂單未接受時直接開始準備...");
                final String finalOrderId = orderId.isEmpty() ? "FAKE-ORDER-ID" : orderId;
                restaurantService.startPreparingOrder(finalOrderId, "REST-001");
                return "不應該到達這裡";
            } catch (InvalidOrderStateException e) {
                appendLog("🎯 成功觸發 InvalidOrderStateException!");
                appendLog("錯誤代碼: " + e.getErrorCode());
                appendLog("訂單ID: " + e.getOrderId());
                appendLog("當前狀態: " + e.getCurrentStatus());
                appendLog("嘗試狀態: " + e.getAttemptedStatus());
                return "InvalidOrderStateException 已觸发";
            } catch (Exception e) {
                appendLog("觸發了其他異常: " + e.getClass().getSimpleName());
                appendLog("錯誤: " + e.getMessage());
                return e.getMessage();
            }
        });
    }
    
    private void triggerDeliveryAssignmentException(String orderId) {
        runAsyncOperation("觸發配送分配異常", () -> {
            try {
                // 移除所有司機來觸發異常
                appendLog("移除所有可用司機...");
                for (int i = 1; i <= 5; i++) {
                    deliveryService.removeDriver("DRIVER-00" + i);
                }
                
                final String finalOrderId = orderId.isEmpty() ? "FAKE-ORDER" : orderId;
                appendLog("嘗試分配司機給訂單 (無可用司機)...");
                deliveryService.assignDriver(finalOrderId);
                return "不應該到達這裡";
            } catch (DeliveryAssignmentException e) {
                appendLog("🎯 成功觸發 DeliveryAssignmentException!");
                appendLog("錯誤代碼: " + e.getErrorCode());
                appendLog("訂單ID: " + e.getOrderId());
                appendLog("失敗原因: " + e.getFailureReason());
                
                // 重新添加司機
                appendLog("重新添加司機以供後續測試...");
                for (int i = 1; i <= 5; i++) {
                    deliveryService.addDriver("DRIVER-00" + i);
                }
                
                return "DeliveryAssignmentException 已觸發";
            } catch (Exception e) {
                appendLog("觸發了其他異常: " + e.getClass().getSimpleName());
                return e.getMessage();
            }
        });
    }

    // ========== 日誌相關方法 ==========
    
    @FXML
    private void onClearLog() {
        logDisplayArea.clear();
        updateStatus("日誌已清除", false);
    }
    
    @FXML
    private void onRefreshLog() {
        appendLog("=== 日誌刷新 ===");
        appendLog("時間: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        updateStatus("日誌已刷新", false);
    }

    // ========== 工具方法 ==========
    
    /**
     * 在新線程中運行異步操作
     */
    private void runAsyncOperation(String operationName, java.util.concurrent.Callable<String> operation) {
        Task<String> task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                return operation.call();
            }
        };
        
        task.setOnRunning(e -> {
            Platform.runLater(() -> {
                operationProgress.setVisible(true);
                updateStatus("正在執行: " + operationName, true);
            });
        });
        
        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                operationProgress.setVisible(false);
                updateStatus("完成: " + operationName, false);
            });
        });
        
        task.setOnFailed(e -> {
            Throwable exception = task.getException();
            Platform.runLater(() -> {
                operationProgress.setVisible(false);
                updateStatus("失敗: " + operationName + " - " + exception.getClass().getSimpleName(), true);
                
                if (exception instanceof DeliveryPlatformException) {
                    appendLog("異常時間戳: " + ((DeliveryPlatformException) exception).getTimestamp());
                }
            });
        });
        
        new Thread(task).start();
    }
    
    /**
     * 添加日誌訊息
     */
    private void appendLog(String message) {
        if (logDisplayArea != null) {
            Platform.runLater(() -> {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
                logDisplayArea.appendText("[" + timestamp + "] " + message + "\n");

                // 自動滾動到底部
                logDisplayArea.setScrollTop(Double.MAX_VALUE);
            });
        } else {
            // 如果沒有日誌區域，則輸出到控制台
            System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")) + "] " + message);
        }
    }
    
    /**
     * 更新狀態標籤
     */
    private void updateStatus(String message, boolean isError) {
        if (statusLabel != null) {
            Platform.runLater(() -> {
                statusLabel.setText(message);
                if (isError) {
                    statusLabel.setStyle("-fx-text-fill: red;");
                } else {
                    statusLabel.setStyle("-fx-text-fill: green;");
                }
            });
        } else {
            // 如果沒有狀態標籤，則輸出到控制台
            System.out.println("Status: " + message + (isError ? " (ERROR)" : ""));
        }
    }

    // ========== 新增的異常觸發方法 ==========
    
    @FXML
    private void onTriggerValidationException() {
        runAsyncOperation("觸發訂單驗證異常", () -> {
            try {
                appendLog("\n🎯 === 觸發 OrderValidationException ===");
                appendLog("嘗試創建無效訂單 (所有必要欄位為空或無效)...");
                
                // 故意傳入無效資料來觸發 OrderValidationException
                orderService.createOrder(null, "", List.of(), null);
                return "不應該到達這裡";
            } catch (OrderValidationException e) {
                appendLog("✅ 成功觸發 OrderValidationException!");
                appendLog("錯誤代碼: " + e.getErrorCode());
                appendLog("錯誤數量: " + e.getErrorCount());
                appendLog("時間戳: " + e.getTimestamp());
                appendLog("詳細錯誤:");
                e.getValidationErrors().forEach(error -> appendLog("  ❌ " + error));
                return "OrderValidationException 已觸發";
            }
        });
    }
    
    @FXML
    private void onTriggerRestaurantException() {
        runAsyncOperation("觸發餐廳不可用異常", () -> {
            try {
                appendLog("\n🎯 === 觸發 RestaurantUnavailableException ===");
                appendLog("嘗試向關閉的餐廳分配訂單...");
                
                // 使用關閉的餐廳來觸發 RestaurantUnavailableException
                restaurantService.acceptOrder("TEST-ORDER", "REST-CLOSED");
                return "不應該到達這裡";
            } catch (RestaurantUnavailableException e) {
                appendLog("✅ 成功觸發 RestaurantUnavailableException!");
                appendLog("錯誤代碼: " + e.getErrorCode());
                appendLog("餐廳ID: " + e.getRestaurantId());
                appendLog("時間戳: " + e.getTimestamp());
                appendLog("錯誤訊息: " + e.getMessage());
                return "RestaurantUnavailableException 已觸發";
            } catch (Exception e) {
                appendLog("觸發了其他異常: " + e.getClass().getSimpleName());
                appendLog("錯誤訊息: " + e.getMessage());
                return e.getMessage();
            }
        });
    }
    
    @FXML
    private void onTriggerStateException() {
        runAsyncOperation("觸發無效狀態轉換異常", () -> {
            try {
                appendLog("\n🎯 === 觸發 InvalidOrderStateException ===");
                appendLog("嘗試在訂單未接受時直接開始準備...");
                
                // 嘗試無效的狀態轉換來觸發 InvalidOrderStateException
                restaurantService.startPreparingOrder("FAKE-ORDER-ID", "REST-001");
                return "不應該到達這裡";
            } catch (InvalidOrderStateException e) {
                appendLog("✅ 成功觸發 InvalidOrderStateException!");
                appendLog("錯誤代碼: " + e.getErrorCode());
                appendLog("訂單ID: " + e.getOrderId());
                appendLog("當前狀態: " + e.getCurrentStatus());
                appendLog("嘗試狀態: " + e.getAttemptedStatus());
                appendLog("時間戳: " + e.getTimestamp());
                return "InvalidOrderStateException 已觸發";
            } catch (Exception e) {
                appendLog("觸發了其他異常: " + e.getClass().getSimpleName());
                appendLog("錯誤訊息: " + e.getMessage());
                return e.getMessage();
            }
        });
    }
    
    @FXML
    private void onTriggerDeliveryException() {
        runAsyncOperation("觸發配送分配異常", () -> {
            try {
                appendLog("\n🎯 === 觸發 DeliveryAssignmentException ===");
                appendLog("移除所有可用司機...");
                
                // 移除所有司機來觸發異常
                for (int i = 1; i <= 5; i++) {
                    deliveryService.removeDriver("DRIVER-00" + i);
                }
                
                appendLog("嘗試分配司機給訂單 (無可用司機)...");
                deliveryService.assignDriver("FAKE-ORDER-ID");
                return "不應該到達這裡";
            } catch (DeliveryAssignmentException e) {
                appendLog("✅ 成功觸發 DeliveryAssignmentException!");
                appendLog("錯誤代碼: " + e.getErrorCode());
                appendLog("訂單ID: " + e.getOrderId());
                appendLog("失敗原因: " + e.getFailureReason());
                appendLog("時間戳: " + e.getTimestamp());
                
                // 重新添加司機以供後續測試
                appendLog("重新添加司機以供後續測試...");
                for (int i = 1; i <= 5; i++) {
                    deliveryService.addDriver("DRIVER-00" + i);
                }
                
                return "DeliveryAssignmentException 已觸發";
            } catch (Exception e) {
                appendLog("觸發了其他異常: " + e.getClass().getSimpleName());
                appendLog("錯誤訊息: " + e.getMessage());
                return e.getMessage();
            }
        });
    }
}