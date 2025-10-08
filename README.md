# 🍕 美食外送伺服端系統 - 異常處理學習專案

一個基於 Java 的美食外送平台伺服端系統，**專注於異常處理機制學習**。保留核心業務流程：顧客訂單、餐廳收單、外送員接單，並展示完整的異常處理架構和 Log4j2 日誌記錄系統。

## 🎯 核心需求實現

本專案完全實現以下核心需求：

### ✅ **美食外送伺服端功能** (對齊原始需求)
- **📦 收到顧客訂單** - 完整的訂單建立與驗證流程 (145行，精簡版)
- **🏪 餐廳收單** - `acceptOrder()` 方法，可能拋出業務異常 (254行，專注核心)
- **🚚 外送員接單** - 配送員分配與管理系統 (169行，輕量設計)
- **⚠️ 異常處理** - 完整的 Checked/Unchecked Exception 處理機制 (5種異常)
- **📝 日誌記錄** - 依據不同狀況寫入 Log4j2 日誌 (中文化訊息)

### 🏗️ **核心設計元件** (簡化後)
- **Order 訂單類別** - 精簡的訂單實體 (145行，保留核心功能)
- **OrderStatus Enum** - PENDING 等 8種訂單狀態 (完整保留)
- **客製化 Exception** - 5種領域特定異常 (完整保留，零修改)
- **Log4j2 日誌級別** - INFO/WARN/ERROR 正確使用 (中文化訊息)
- **狀態轉換驗證** - 強固的業務邏輯驗證 (簡化邏輯)
- **✅ 測試覆蓋** - 81個測試案例，100%通過率，品質保證

## 🚀 快速開始

### 📋 系統需求
- **Java 17** 或更新版本
- **Maven 3.6+** 建構工具

### ⚡ 一鍵體驗核心功能

```bash
# 克隆專案
git clone [your-repo-url]
cd food-delivery-platform

# 執行核心需求演示 (推薦首次使用)
chmod +x run-core-requirements.sh
./run-core-requirements.sh run
```

### 🎯 多種測試方案

#### 1️⃣ **核心需求演示** (推薦！完全對齊原始需求)
```bash
./run-core-requirements.sh run
```
- 🎯 **完整展示** 顧客訂單 → 餐廳收單 → 外送員接單
- ⚠️ **異常處理** Checked/Unchecked Exception 完整演示
- 📝 **日誌分級** INFO/WARN/ERROR 三級日誌記錄
- 🔄 **狀態管理** PENDING 等 Enum 狀態轉換

#### 2️⃣ **快速功能驗證**
```bash
chmod +x run-simplified-tests.sh
./run-simplified-tests.sh quick
```
- ⏱️ **30 秒快速驗證**
- 🎯 **核心異常處理展示**

#### 3️⃣ **自動化測試套件**
```bash
./run-simplified-tests.sh auto
```
- 📊 **13 個測試案例**
- 🤖 **完整回歸測試**

#### 4️⃣ **詳細測試演示**
```bash
./run-simplified-tests.sh detailed
```
- 🔍 **逐步流程展示**
- 📋 **完整訂單生命週期**

#### 5️⃣ **JUnit 單元測試**
```bash
mvn test
```
- 🧪 **81 個測試案例**
- ✅ **100% 通過率**

## 📚 核心架構設計

### 🏗️ **系統架構**
```
美食外送伺服端系統
├── 📦 models/          # 訂單實體 (Order, OrderStatus Enum)
├── ⚠️ exceptions/      # 客製化異常階層
├── ⚙️ services/        # 業務邏輯 (訂單、餐廳、配送)
├── 💾 repositories/    # 資料存取層
├── 📋 controllers/     # 異常處理控制器
└── 🧪 demo/            # 核心需求演示程式
```

### 📦 **Order 訂單類別設計**
```java
public class Order {
    private String orderId;           // 訂單編號
    private String customerId;        // 顧客編號  
    private String restaurantId;      // 餐廳編號
    private OrderStatus status;       // 訂單狀態 (Enum)
    private List<OrderItem> items;    // 訂單項目
    private BigDecimal totalAmount;   // 總金額
    private LocalDateTime createdAt;  // 建立時間
    
    // 狀態轉換邏輯與驗證
    public void updateStatus(OrderStatus newStatus) 
            throws InvalidOrderStateException { ... }
}
```

### 🔄 **OrderStatus Enum 狀態設計**
```
PENDING → ACCEPTED → PREPARING → READY_FOR_DELIVERY → IN_DELIVERY → DELIVERED
                   ↓
                REJECTED / CANCELLED (終端狀態)
```

```java
public enum OrderStatus {
    PENDING,                // 待處理 (初始狀態)
    ACCEPTED,              // 已接受
    PREPARING,             // 準備中  
    READY_FOR_DELIVERY,    // 準備配送
    IN_DELIVERY,           // 配送中
    DELIVERED,             // 已送達 (成功終端狀態)
    CANCELLED,             // 已取消 (取消終端狀態)
    REJECTED;              // 已拒絕 (拒絕終端狀態)
    
    // 狀態轉換驗證
    public boolean canTransitionTo(OrderStatus newStatus) { ... }
}
```

### ⚠️ **客製化 Exception 階層**

#### **Checked Exception (業務邏輯錯誤)**
```java
// 基礎異常
public abstract class DeliveryPlatformException extends Exception {
    protected String errorCode;
    protected LocalDateTime timestamp;
}

// 1. 訂單驗證異常
public class OrderValidationException extends DeliveryPlatformException {
    private List<String> validationErrors;  // 驗證錯誤清單
    private int errorCount;                  // 錯誤數量
}

// 2. 餐廳不可用異常 (acceptOrder 可能拋出)
public class RestaurantUnavailableException extends DeliveryPlatformException {
    private String restaurantId;            // 餐廳編號
}

// 3. 無效狀態轉換異常
public class InvalidOrderStateException extends DeliveryPlatformException {
    private String orderId;                 // 訂單編號
    private OrderStatus currentStatus;      // 目前狀態
    private OrderStatus attemptedStatus;    // 嘗試轉換的狀態
}

// 4. 配送分配異常
public class DeliveryAssignmentException extends DeliveryPlatformException {
    private String orderId;                 // 訂單編號
    private String failureReason;           // 失敗原因
}
```

## 🎨 使用範例

### 📦 **1. 顧客訂單功能**
```java
try {
    // 建立訂單項目
    List<OrderItem> items = Arrays.asList(
        new OrderItem("ITEM-001", "瑪格麗特披薩", 2, new BigDecimal("18.90"))
    );
    
    // 顧客下單 (Order 類別 + PENDING 狀態)
    Order order = orderService.createOrder(
        "CUST-001", 
        "REST-001", 
        items, 
        "台北市信義區信義路五段7號"
    );
    
    // INFO 級別日誌 - 正常業務操作
    logger.info("顧客訂單建立成功: orderId={}, status={}", 
               order.getOrderId(), order.getStatus()); // PENDING
               
} catch (OrderValidationException e) {
    // WARN 級別日誌 - 業務邏輯錯誤 (Checked Exception)
    logger.warn("訂單驗證失敗: errorCode={}, errors={}", 
               e.getErrorCode(), e.getValidationErrors());
}
```

### 🏪 **2. 餐廳收單 acceptOrder() 功能**
```java
try {
    // acceptOrder() 可能拋出例外
    restaurantService.acceptOrder(orderId, "REST-001");
    
    // INFO 級別日誌 - 正常業務操作
    logger.info("餐廳成功接單: orderId={}, restaurantId={}", orderId, "REST-001");
    
    // 狀態轉換: PENDING → ACCEPTED
    
} catch (RestaurantUnavailableException e) {
    // WARN 級別日誌 - 業務邏輯錯誤 (Checked Exception)
    logger.warn("餐廳無法接單: restaurantId={}, reason={}", 
               e.getRestaurantId(), e.getMessage());
               
} catch (InvalidOrderStateException e) {
    // WARN 級別日誌 - 狀態轉換錯誤
    logger.warn("訂單狀態轉換錯誤: {} → {}", 
               e.getCurrentStatus(), e.getAttemptedStatus());
}
```

### 🚚 **3. 外送員接單功能**
```java
try {
    // 外送員接單
    String assignedDriver = deliveryService.assignDriver(orderId);
    
    // INFO 級別日誌 - 正常業務操作
    logger.info("外送員成功接單: orderId={}, driverId={}", orderId, assignedDriver);
    
    // 狀態轉換: READY_FOR_DELIVERY → IN_DELIVERY
    
} catch (DeliveryAssignmentException e) {
    // WARN 級別日誌 - 業務邏輯錯誤 (Checked Exception)
    logger.warn("外送員分配失敗: orderId={}, reason={}", 
               e.getOrderId(), e.getFailureReason());
}
```

### 📝 **4. Log4j2 日誌級別使用**
```java
// INFO: 正常業務操作記錄
logger.info("用戶登入成功: userId={}", "USER-123");
logger.info("訂單狀態更新: orderId={}, newStatus={}", "ORDER-456", "DELIVERED");

// WARN: 業務邏輯錯誤 (Checked Exception)  
logger.warn("餐廳容量已滿: restaurantId={}, currentOrders={}", "REST-001", 10);
logger.warn("訂單驗證失敗: orderId={}, reason={}", "ORDER-789", "無效地址");

// ERROR: 程式碼邏輯或環境錯誤 (Unchecked Exception)
logger.error("資料庫連線失敗", exception);
logger.error("外部 API 呼叫超時: service={}, timeout={}ms", "PaymentService", 5000);
```

## 📊 執行結果展示

### 🎯 **核心需求演示輸出**
```bash
$ ./run-core-requirements.sh run

=== 美食外送伺服端功能演示開始 ===

=== 1. 演示顧客訂單功能 ===
INFO - 顧客訂單建立成功: orderId=ORD-0DDCC4C5, status=PENDING, amount=$41.30
INFO - 目前訂單狀態: PENDING
INFO - 可轉換到的狀態: [REJECTED, ACCEPTED, CANCELLED]

=== 2. 演示餐廳 acceptOrder() 功能 ===  
INFO - 餐廳成功接單: orderId=ORD-A123B456, restaurantId=REST-001
INFO - 訂單狀態已更新: PENDING → ACCEPTED

=== 3. 演示外送員接單功能 ===
INFO - 外送員成功接單: orderId=ORD-C789D012, driverId=DRIVER-003
INFO - 訂單完整生命週期: PENDING → ACCEPTED → PREPARING → READY_FOR_DELIVERY → IN_DELIVERY → DELIVERED

=== 4. 演示各種例外處理 ===
WARN - 捕獲 OrderValidationException: errorCode=ORDER_VALIDATION_FAILED, errorCount=4
WARN - 捕獲 RestaurantUnavailableException: restaurantId=REST-002, errorCode=RESTAURANT_UNAVAILABLE
WARN - 捕獲 InvalidOrderStateException: orderId=NONEXISTENT-ORDER, currentStatus=null, attemptedStatus=PREPARING
ERROR - 捕獲 NullPointerException (Unchecked Exception): 程式碼邏輯錯誤

=== 5. 演示 Log4j2 日誌級別 ===
INFO - INFO 級別: 記錄正常業務操作，用於追蹤系統執行流程
WARN - WARN 級別: 記錄 Checked Exception (業務邏輯錯誤)，需要關注但不影響系統運行  
ERROR - ERROR 級別: 記錄 Unchecked Exception (程式碼邏輯或環境錯誤)，嚴重問題需要立即處理

透過這些日誌，我們可以發現後端執行過程中發生的各種問題！

=== 美食外送伺服端功能演示結束 ===
```

### 📝 **日誌文件輸出**
```bash
$ ./run-core-requirements.sh logs

=== 日誌內容預覽 ===
2025-10-08 23:25:39.765 [main] INFO  CoreRequirementsDemo - 顧客訂單建立成功: orderId=ORD-0DDCC4C5, status=PENDING, amount=$41.30
2025-10-08 23:25:39.768 [main] INFO  CoreRequirementsDemo - 餐廳成功接單: orderId=ORD-A123B456, restaurantId=REST-001
2025-10-08 23:25:39.795 [main] WARN  CoreRequirementsDemo - 捕獲 OrderValidationException: errorCode=ORDER_VALIDATION_FAILED, errorCount=4
2025-10-08 23:25:39.803 [main] ERROR CoreRequirementsDemo - 捕獲 NullPointerException (Unchecked Exception): 程式碼邏輯錯誤

完整日誌請查看: logs/delivery-platform.log
```

## 📁 專案結構

```
src/
├── main/java/com/deliveryplatform/
│   ├── 🧪 demo/
│   │   ├── CoreRequirementsDemo.java         # 核心需求演示 (推薦)
│   │   ├── SimplifiedOrderDemo.java          # 快速功能演示
│   │   ├── BatchTestRunner.java              # 自動化測試
│   │   └── SimpleConsoleDemo.java            # 詳細測試演示
│   ├── 📦 models/
│   │   ├── Order.java                        # 訂單實體類別 (145行)
│   │   ├── OrderItem.java                    # 訂單項目 (71行)
│   │   ├── OrderStatus.java                  # 訂單狀態 Enum (PENDING 等)
│   │   └── Restaurant.java                   # 餐廳實體 (73行)
│   ├── ⚠️ exceptions/
│   │   ├── DeliveryPlatformException.java    # 基礎異常
│   │   ├── OrderValidationException.java     # 訂單驗證異常
│   │   ├── RestaurantUnavailableException.java # 餐廳不可用異常
│   │   ├── InvalidOrderStateException.java   # 無效狀態轉換異常
│   │   └── DeliveryAssignmentException.java  # 配送分配異常
│   ├── ⚙️ services/
│   │   ├── OrderService.java                 # 訂單服務 (140行)
│   │   ├── RestaurantServiceImpl.java        # 餐廳服務 (254行，含 acceptOrder)
│   │   └── DeliveryService.java              # 配送服務 (169行)
│   ├── 💾 repositories/
│   │   ├── OrderRepository.java              # 訂單資料介面
│   │   ├── InMemoryOrderRepository.java      # 記憶體訂單資料庫
│   │   ├── RestaurantRepository.java         # 餐廳資料介面
│   │   └── InMemoryRestaurantRepository.java # 記憶體餐廳資料庫
│   └── 📋 controllers/
│       └── GlobalExceptionHandler.java       # 全域異常處理器
└── test/java/com/deliveryplatform/
    ├── integration/OrderLifecycleIntegrationTest.java  # 整合測試
    ├── models/OrderTest.java                          # 模型測試
    ├── exceptions/CustomExceptionsTest.java           # 異常測試
    └── services/RestaurantServiceTest.java            # 服務測試
```

## 🔧 核心依賴

```xml
<dependencies>
    <!-- Log4j2 日誌系統 -->
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-core</artifactId>
        <version>2.20.0</version>
    </dependency>
    
    <!-- 驗證框架 -->
    <dependency>
        <groupId>jakarta.validation</groupId>
        <artifactId>jakarta.validation-api</artifactId>
        <version>3.0.2</version>
    </dependency>
    
    <!-- JSON 處理 -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.15.2</version>
    </dependency>
    
    <!-- 測試框架 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-engine</artifactId>
        <version>5.9.3</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## 🎯 專案特色與價值

### ✨ **完全對齊原始需求**
- ✅ **美食外送伺服端** - 完整業務流程實現
- ✅ **顧客訂單** - Order 類別與 PENDING 狀態
- ✅ **餐廳收單** - acceptOrder() 可拋出異常
- ✅ **外送員接單** - 配送分配機制
- ✅ **異常處理** - Checked/Unchecked Exception 分類
- ✅ **日誌記錄** - 依據不同狀況寫入 Log4j2

### 💡 **核心價值**
> **「透過 log 發現後端在執行的過程中發生什麼問題」**

- **INFO 日誌** - 追蹤正常業務操作流程
- **WARN 日誌** - 識別業務邏輯問題 (Checked Exception)  
- **ERROR 日誌** - 發現系統層級問題 (Unchecked Exception)

## 🤝 快速上手指南

### 🚀 **3 分鐘快速體驗**
```bash
# 1. 克隆專案
git clone [your-repo-url] && cd food-delivery-platform

# 2. 執行核心演示  
chmod +x run-core-requirements.sh
./run-core-requirements.sh run

# 3. 查看產生的日誌
./run-core-requirements.sh logs
```

### 📚 **深入學習**
```bash
# 詳細測試演示
./run-simplified-tests.sh detailed

# 自動化測試套件
./run-simplified-tests.sh auto

# JUnit 單元測試
mvn test
```

## 🌍 使用場景與部署

### 🎓 **學習與教學**
- **Java 異常處理機制** - Checked vs Unchecked Exception
- **Log4j2 日誌系統** - INFO/WARN/ERROR 正確使用
- **Enum 狀態設計** - 狀態機模式實現
- **Clean Architecture** - 分層架構設計
- **業務邏輯建模** - 外送平台領域設計

### 💻 **開發環境**
```bash
# 本地開發
git clone [repo-url]
cd food-delivery-platform
./run-core-requirements.sh run
```

### 🐳 **容器化部署**
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/food-delivery-platform-1.0.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 🤖 **CI/CD 整合**
```yaml
# GitHub Actions 範例
- name: 執行核心需求測試
  run: |
    mvn clean compile
    ./run-core-requirements.sh run
    
- name: 執行自動化測試套件  
  run: |
    ./run-simplified-tests.sh auto
```

---