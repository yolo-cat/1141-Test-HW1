# 🍕 美食外送伺服端異常處理系統 - 作業報告

## 📋 題目

**模擬美食外送伺服端功能的異常處理機制**

設計並實作一個美食外送平台的伺服端系統，該系統需要處理：
- 顧客訂單請求
- 餐廳收單處理 
- 外送員接單分配
- 在過程中產生的各種異常情況，並依據不同狀況記錄到日誌中

**核心技術要求：**
1. 設計並拋出 **Checked Exception** (業務邏輯錯誤)
2. 處理並記錄 **Unchecked Exception** (程式碼邏輯或環境錯誤)
3. 正確使用 **Log4j2** 的不同日誌級別 (INFO, WARN, ERROR)
4. 透過 **Enum** 設計不同的訂單狀態，例如 PENDING 等狀態
5. 設計 **Order** 的訂單類別
6. 設計客製化 **Exception** 階層
7. 設計餐廳的 **acceptOrder()** 功能，可能拋出例外
8. 最終透過 **log** 發現後端執行過程中發生的問題

---

## 🏗️ 設計方法概述

### 系統架構設計

本系統採用 **分層架構 (Layered Architecture)** 設計，將系統職責清晰分離：

```
美食外送伺服端系統
├── 📦 models/          # 領域模型層 (Order, OrderStatus Enum)
├── ⚠️ exceptions/      # 異常處理層 (客製化異常階層)
├── ⚙️ services/        # 業務邏輯層 (訂單、餐廳、配送服務)
├── 💾 repositories/    # 資料存取層 (記憶體模擬資料庫)
├── 📋 controllers/     # 控制層 (全域異常處理)
└── 🧪 demo/            # 演示程式層 (核心需求展示)
```

### 核心設計模式

1. **狀態模式 (State Pattern)** - OrderStatus Enum 管理訂單狀態轉換
2. **異常鏈 (Exception Chaining)** - 建立清晰的異常階層
3. **依賴注入 (Dependency Injection)** - 服務間鬆耦合設計
4. **策略模式 (Strategy Pattern)** - 不同類型的異常處理策略

### 異常處理架構

#### Checked Exception 設計 (業務邏輯錯誤)
```java
DeliveryPlatformException (基礎異常)
├── OrderValidationException      # 訂單驗證異常
├── RestaurantUnavailableException # 餐廳不可用異常  
├── InvalidOrderStateException    # 狀態轉換異常
└── DeliveryAssignmentException   # 配送分配異常
```

#### Log4j2 日誌分級策略
- **INFO**: 正常業務操作流程追蹤
- **WARN**: Checked Exception (業務邏輯錯誤)
- **ERROR**: Unchecked Exception (系統層級錯誤)

---

## 💻 程式設計與實作

### 1. 核心領域模型設計

#### OrderStatus Enum - 狀態管理
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
    
    // 狀態轉換驗證邏輯
    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = new HashMap<>();

    static {
        // PENDING 可轉換到 ACCEPTED, REJECTED, CANCELLED
        VALID_TRANSITIONS.put(PENDING, EnumSet.of(ACCEPTED, REJECTED, CANCELLED));
        // ACCEPTED 可轉換到 PREPARING, CANCELLED
        VALID_TRANSITIONS.put(ACCEPTED, EnumSet.of(PREPARING, CANCELLED));
        // ... 其他轉換規則
    }

    public boolean canTransitionTo(OrderStatus newStatus) {
        Set<OrderStatus> allowedTransitions = VALID_TRANSITIONS.get(this);
        return allowedTransitions != null && allowedTransitions.contains(newStatus);
    }
}
```

#### Order 訂單類別設計
```java
public class Order {
    private String orderId;           // 訂單編號
    private String customerId;        // 顧客編號  
    private String restaurantId;      // 餐廳編號
    private OrderStatus status;       // 訂單狀態 (Enum)
    private List<OrderItem> items;    // 訂單項目
    private BigDecimal totalAmount;   // 總金額
    private LocalDateTime createdAt;  // 建立時間
    
    // 狀態轉換與驗證
    public void updateStatus(OrderStatus newStatus) throws InvalidOrderStateException {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new InvalidOrderStateException(
                String.format("Cannot transition order %s from %s to %s", 
                             orderId, this.status, newStatus),
                "INVALID_STATUS_TRANSITION",
                orderId, this.status, newStatus
            );
        }
        
        OrderStatus oldStatus = this.status;
        this.status = newStatus;
        
        logger.info("Order status transition: orderId={}, from={}, to={}", 
                   orderId, oldStatus, newStatus);
    }
}
```

### 2. 客製化異常階層設計

#### 基礎異常類別
```java
public abstract class DeliveryPlatformException extends Exception {
    private final String errorCode;
    private final LocalDateTime timestamp;

    protected DeliveryPlatformException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }
    
    public String getErrorCode() { return errorCode; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
```

#### 訂單驗證異常 (Checked Exception)
```java
public class OrderValidationException extends DeliveryPlatformException {
    private final List<String> validationErrors;
    private final int errorCount;

    public OrderValidationException(List<String> validationErrors) {
        super(buildMessage(validationErrors), "ORDER_VALIDATION_FAILED");
        this.validationErrors = List.copyOf(validationErrors);
        this.errorCount = validationErrors.size();
    }
    
    private static String buildMessage(List<String> errors) {
        return String.format("Order validation failed with %d errors: %s", 
                           errors.size(), String.join(", ", errors));
    }
}
```

#### 餐廳不可用異常 (Checked Exception)
```java
public class RestaurantUnavailableException extends DeliveryPlatformException {
    private final String restaurantId;

    public RestaurantUnavailableException(String restaurantId, String reason) {
        super(String.format("Restaurant %s is currently unavailable: %s", restaurantId, reason),
              "RESTAURANT_UNAVAILABLE");
        this.restaurantId = restaurantId;
    }
}
```

### 3. 餐廳服務 acceptOrder() 實作

```java
@Override
public void acceptOrder(String orderId, String restaurantId) 
        throws RestaurantUnavailableException, InvalidOrderStateException {
    
    logger.info("餐廳 {} 嘗試接單 {}", restaurantId, orderId);
    
    try {
        // 1. 驗證餐廳是否存在且可用
        Restaurant restaurant = validateRestaurantAvailability(restaurantId);
        
        // 2. 驗證訂單狀態
        Order order = validateOrderForAcceptance(orderId, restaurantId);
        
        // 3. 執行接單邏輯
        order.updateStatus(OrderStatus.ACCEPTED);
        orderRepository.save(order);
        
        logger.info("餐廳 {} 成功接單 {}", restaurantId, orderId);
        
    } catch (RestaurantUnavailableException | InvalidOrderStateException e) {
        // WARN 級別 - 業務邏輯異常 (Checked Exception)
        logger.warn("業務異常 - acceptOrder: orderId={}, restaurantId={}, 錯誤={}", 
                   orderId, restaurantId, e.getMessage());
        throw e;
        
    } catch (Exception e) {
        // ERROR 級別 - 系統異常 (Unchecked Exception)
        logger.error("系統異常 - acceptOrder: orderId={}, restaurantId={}", 
                    orderId, restaurantId, e);
        throw new RuntimeException("接單失敗：系統錯誤", e);
    }
}

private Restaurant validateRestaurantAvailability(String restaurantId) 
        throws RestaurantUnavailableException {
    Restaurant restaurant = restaurantRepository.findById(restaurantId);
    
    if (restaurant == null) {
        throw new RestaurantUnavailableException(restaurantId, "餐廳不存在");
    }
    
    if (!restaurant.isAvailable()) {
        throw new RestaurantUnavailableException(restaurantId, 
            String.format("餐廳營業時間: %s-%s", 
                         restaurant.getOpenTime(), restaurant.getCloseTime()));
    }
    
    return restaurant;
}
```

### 4. Log4j2 配置與使用

#### log4j2.xml 配置
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
    <Appenders>
        <!-- 控制台輸出 -->
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
        </Console>
        
        <!-- 檔案輸出 -->
        <File name="FileAppender" fileName="logs/delivery-platform.log">
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
        </File>
    </Appenders>
    
    <Loggers>
        <Root level="info">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="FileAppender"/>
        </Root>
    </Loggers>
</Configuration>
```

#### 日誌使用範例
```java
// INFO 級別 - 正常業務操作
logger.info("顧客訂單建立成功: orderId={}, status={}, amount=${}", 
           order.getOrderId(), order.getStatus(), order.getTotalAmount());

// WARN 級別 - 業務邏輯錯誤 (Checked Exception)
logger.warn("捕獲 RestaurantUnavailableException: restaurantId={}, errorCode={}", 
           e.getRestaurantId(), e.getErrorCode());

// ERROR 級別 - 系統錯誤 (Unchecked Exception)
logger.error("捕獲 NullPointerException (Unchecked Exception): 程式碼邏輯錯誤", e);
```

---

## 🖥️ 執行畫面及說明

### 1. 核心需求演示執行

```bash
$ ./run-core-requirements.sh run

==================================================
🎯 美食外送伺服端核心需求演示
==================================================

📋 演示內容：
✅ 1. 收到顧客訂單 (Order 類別設計)
✅ 2. 餐廳收單 acceptOrder() (可能拋出例外)
✅ 3. 外送員接單 (DeliveryAssignment)
✅ 4. 各種例外處理 (Checked/Unchecked Exception)
✅ 5. Log4j2 不同日誌級別 (INFO/WARN/ERROR)
✅ 6. Enum 訂單狀態設計 (PENDING 等)
✅ 7. 客製化 Exception 設計

🚀 開始執行核心需求演示...
```

#### 執行結果畫面
```
01:18:45.617 [main] INFO  CoreRequirementsDemo - === 美食外送伺服端功能演示開始 ===

=== 1. 演示顧客訂單功能 ===
01:18:45.651 [main] INFO  OrderService - Order created successfully: orderId=ORD-345BF4CE, amount=41.30
01:18:45.652 [main] INFO  CoreRequirementsDemo - 顧客訂單建立成功: orderId=ORD-345BF4CE, status=PENDING, amount=$41.30
01:18:45.652 [main] INFO  CoreRequirementsDemo - 目前訂單狀態: PENDING
01:18:45.652 [main] INFO  CoreRequirementsDemo - 可轉換到的狀態: [ACCEPTED, CANCELLED, REJECTED]

=== 2. 演示餐廳 acceptOrder() 功能 ===
01:18:45.655 [main] INFO  RestaurantServiceImpl - 餐廳 REST-001 嘗試接單 ORD-A123B456
01:18:45.655 [main] INFO  Order - Order status transition: orderId=ORD-A123B456, from=PENDING, to=ACCEPTED
01:18:45.655 [main] INFO  RestaurantServiceImpl - 餐廳 REST-001 成功接單 ORD-A123B456

=== 3. 演示外送員接單功能 ===
01:18:45.659 [main] INFO  DeliveryService - Driver assigned to order: orderId=ORD-C789D012, driverId=DRIVER-003
01:18:45.660 [main] INFO  CoreRequirementsDemo - 外送員成功接單: orderId=ORD-C789D012, driverId=DRIVER-003
01:18:45.660 [main] INFO  CoreRequirementsDemo - 訂單完整生命週期: PENDING → ACCEPTED → PREPARING → READY_FOR_DELIVERY → IN_DELIVERY → DELIVERED

=== 4. 演示各種例外處理 ===
01:18:45.665 [main] WARN  CoreRequirementsDemo - 捕獲 OrderValidationException: errorCode=ORDER_VALIDATION_FAILED, errorCount=4
01:18:45.666 [main] WARN  CoreRequirementsDemo - 捕獲 RestaurantUnavailableException: restaurantId=REST-002, errorCode=RESTAURANT_UNAVAILABLE  
01:18:45.668 [main] WARN  CoreRequirementsDemo - 捕獲 InvalidOrderStateException: orderId=NONEXISTENT-ORDER, currentStatus=null, attemptedStatus=PREPARING
01:18:45.670 [main] ERROR CoreRequirementsDemo - 捕獲 NullPointerException (Unchecked Exception): 程式碼邏輯錯誤

=== 5. 演示 Log4j2 日誌級別 ===
01:18:45.671 [main] INFO  CoreRequirementsDemo - INFO 級別: 記錄正常的業務操作流程
01:18:45.671 [main] WARN  CoreRequirementsDemo - WARN 級別: 記錄業務邏輯錯誤 (Checked Exception)
01:18:45.672 [main] ERROR CoreRequirementsDemo - ERROR 級別: 記錄系統錯誤 (Unchecked Exception)

透過這些日誌，我們可以發現後端執行過程中發生的各種問題！

=== 美食外送伺服端功能演示結束 ===
```

### 2. 單元測試執行結果

```bash
$ mvn test

[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.deliveryplatform.integration.OrderLifecycleIntegrationTest
[INFO] Running com.deliveryplatform.models.OrderTest  
[INFO] Running com.deliveryplatform.exceptions.CustomExceptionsTest
[INFO] Running com.deliveryplatform.services.RestaurantServiceTest
[INFO] Tests run: 81, Failures: 0, Errors: 0, Skipped: 0

[INFO] Results:
[INFO] Tests run: 81, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### 3. 日誌檔案內容

```bash
$ ./run-core-requirements.sh logs

=== 日誌內容預覽 ===
2025-10-09 01:18:45.651 [main] INFO  OrderService - Order created successfully: orderId=ORD-345BF4CE, amount=41.30
2025-10-09 01:18:45.655 [main] INFO  RestaurantServiceImpl - 餐廳 REST-001 成功接單 ORD-A123B456  
2025-10-09 01:18:45.665 [main] WARN  CoreRequirementsDemo - 捕獲 OrderValidationException: errorCode=ORDER_VALIDATION_FAILED, errorCount=4
2025-10-09 01:18:45.670 [main] ERROR CoreRequirementsDemo - 捕獲 NullPointerException (Unchecked Exception): 程式碼邏輯錯誤

完整日誌請查看: logs/delivery-platform.log
```

### 4. 程式架構展示

#### 專案結構
```
src/main/java/com/deliveryplatform/
├── 🧪 demo/
│   ├── CoreRequirementsDemo.java         # 核心需求演示程式
│   ├── SimplifiedOrderDemo.java          # 快速功能演示  
│   ├── BatchTestRunner.java              # 自動化測試
│   └── SimpleConsoleDemo.java            # 詳細測試演示
├── 📦 models/
│   ├── Order.java                        # 訂單實體類別 (145行)
│   ├── OrderItem.java                    # 訂單項目 (71行)
│   ├── OrderStatus.java                  # 訂單狀態 Enum
│   └── Restaurant.java                   # 餐廳實體 (73行)
├── ⚠️ exceptions/
│   ├── DeliveryPlatformException.java    # 基礎異常
│   ├── OrderValidationException.java     # 訂單驗證異常
│   ├── RestaurantUnavailableException.java # 餐廳不可用異常
│   ├── InvalidOrderStateException.java   # 無效狀態轉換異常  
│   └── DeliveryAssignmentException.java  # 配送分配異常
├── ⚙️ services/
│   ├── OrderService.java                 # 訂單服務 (140行)
│   ├── RestaurantServiceImpl.java        # 餐廳服務 (254行，含acceptOrder)
│   └── DeliveryService.java              # 配送服務 (169行)
├── 💾 repositories/
│   ├── OrderRepository.java              # 訂單資料介面
│   ├── InMemoryOrderRepository.java      # 記憶體訂單資料庫
│   ├── RestaurantRepository.java         # 餐廳資料介面
│   └── InMemoryRestaurantRepository.java # 記憶體餐廳資料庫  
└── 📋 controllers/
    └── GlobalExceptionHandler.java       # 全域異常處理器
```

---

## 📚 參考資料與使用工具

### 🛠️ 開發工具與環境
| 工具/技術 | 版本 | 用途 | 使用比例 |
|----------|------|------|----------|
| **Java** | 17 | 主要開發語言 | 100% |
| **Maven** | 3.9.11 | 專案建構管理 | 100% |
| **Log4j2** | 2.20.0 | 日誌記錄系統 | 100% |
| **JUnit 5** | 5.9.3 | 單元測試框架 | 100% |
| **IntelliJ IDEA** | 2024.2 | 整合開發環境 | 90% |
| **Git** | 2.42.0 | 版本控制系統 | 100% |

### 📖 技術參考資料
1. **Java Exception Handling Best Practices**
   - Oracle Java Documentation - Exception Handling
   - "Effective Java" by Joshua Bloch - Chapter 10: Exceptions
   - 使用於設計 Checked vs Unchecked Exception 階層

2. **Log4j2 官方文檔**
   - Apache Log4j 2 Configuration Reference  
   - Log4j2 Appenders and Layouts Guide
   - 用於配置日誌系統和訊息格式化

3. **Design Patterns**
   - "Design Patterns: Elements of Reusable Object-Oriented Software" - GoF
   - State Pattern 用於 OrderStatus 狀態管理
   - Strategy Pattern 用於異常處理策略

4. **Maven 依賴管理**
   - Maven Central Repository
   - Spring Boot Starter 依賴參考 (部分參考)

### 🤖 AI 工具使用說明

#### GitHub Copilot CLI (主要 AI 工具)
- **使用比例**: 約 75%
- **主要用途**:
  - 程式碼結構設計與實作建議
  - 異常處理模式實作
  - 單元測試案例生成
  - 程式碼重構與優化
  - 文檔撰寫與格式化

#### 具體使用場景
1. **異常階層設計** (AI 輔助 80%)
   ```java
   // AI 協助設計異常繼承結構和錯誤碼管理
   public abstract class DeliveryPlatformException extends Exception {
       private final String errorCode;
       private final LocalDateTime timestamp;
       // ...
   }
   ```

2. **狀態轉換邏輯** (AI 輔助 70%) 
   ```java
   // AI 協助設計狀態機驗證邏輯
   private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = new HashMap<>();
   ```

3. **測試案例生成** (AI 輔助 85%)
   ```java
   // AI 自動生成各種邊界條件測試
   @Test
   void shouldThrowExceptionWhenInvalidStateTransition() {
       // 測試案例邏輯
   }
   ```

4. **日誌訊息格式化** (AI 輔助 60%)
   ```java
   // AI 協助設計結構化日誌訊息
   logger.info("Order status transition: orderId={}, from={}, to={}", 
              orderId, oldStatus, newStatus);
   ```

#### 人工撰寫部分 (25%)
- 核心業務邏輯設計概念
- 系統架構整體規劃  
- 異常處理策略制定
- 專案需求分析與解讀
- 效能考量與優化決策

### 🔍 學習資源
1. **Java 異常處理**
   - Oracle Java Tutorials - Lesson: Exceptions
   - Baeldung - Java Exception Handling Best Practices
   
2. **日誌系統設計**  
   - "Java Logging Best Practices" - DZone Articles
   - Structured Logging with Log4j2 - Apache Documentation

3. **軟體架構設計**
   - "Clean Architecture" by Robert C. Martin
   - "Domain-Driven Design" by Eric Evans
   - 用於分層架構和領域模型設計

4. **測試驅動開發**
   - "Test Driven Development: By Example" by Kent Beck
   - JUnit 5 User Guide - Official Documentation

---

## 📊 專案成果總結

### ✅ 核心需求完成度
| 需求項目 | 完成狀態 | 實作細節 |
|---------|----------|----------|
| **美食外送伺服端功能** | ✅ 完成 | 完整的訂單→餐廳→外送員流程 |
| **顧客訂單處理** | ✅ 完成 | Order 類別設計，145行核心邏輯 |
| **餐廳 acceptOrder()** | ✅ 完成 | 可拋出業務異常的完整實作 |
| **外送員接單** | ✅ 完成 | DeliveryService 配送分配邏輯 |
| **Checked Exception** | ✅ 完成 | 5種業務邏輯異常類別 |
| **Unchecked Exception** | ✅ 完成 | 系統層級異常處理機制 |
| **Log4j2 日誌分級** | ✅ 完成 | INFO/WARN/ERROR 正確使用 |
| **OrderStatus Enum** | ✅ 完成 | 8種狀態+轉換驗證邏輯 |
| **客製化 Exception** | ✅ 完成 | 完整異常階層設計 |
| **日誌問題診斷** | ✅ 完成 | 透過日誌發現後端執行問題 |

### 📈 技術指標
- **程式碼行數**: 3,056 行 (精簡優化後)
- **測試覆蓋**: 81 個測試案例，100% 通過率
- **異常類別**: 5 個客製化業務異常
- **日誌級別**: 正確使用 INFO/WARN/ERROR 三級
- **狀態管理**: 8 種訂單狀態，完整轉換驗證

### 🎯 學習成果
1. **深度理解** Java 異常處理機制 (Checked vs Unchecked)
2. **熟練掌握** Log4j2 日誌系統配置與使用
3. **實際應用** Enum 狀態模式設計
4. **系統性學習** 分層架構設計原則
5. **實務經驗** 業務異常與系統異常的區分處理

### 🚀 擴展性設計
- **模組化架構**: 各層職責清晰，易於擴展
- **介面導向**: Repository 和 Service 採用介面設計
- **配置化**: Log4j2 配置檔案化管理
- **測試友好**: 完整的單元測試和整合測試覆蓋

---

## 🎓 結論

本專案成功實現了一個完整的美食外送伺服端異常處理系統，不僅滿足了所有核心技術需求，更在實作過程中深入理解了 Java 異常處理的最佳實務。透過 Log4j2 的結構化日誌記錄，我們能夠有效監控和診斷系統運行中的各種問題，達成了「透過 log 發現後端執行過程中發生的問題」的核心目標。

整個系統採用現代化的軟體開發實務，包括分層架構、測試驅動開發、和清晰的異常處理策略，為未來的維護和擴展奠定了堅實的基礎。