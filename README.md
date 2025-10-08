# 🍕 外賣平台後端系統

一個基於 Java 的輕量化外賣平台後端系統，專注於**異常處理機制**和**訂單生命週期管理**。本專案展示了完整的業務邏輯層設計、自訂異常處理架構，以及結構化日誌記錄系統。

## ✨ 專案特色

### 🎯 核心功能
- **📦 訂單管理** - 完整的訂單生命週期（建立→接受→準備→配送→完成）
- **🏪 餐廳營運** - 訂單接受/拒絕、準備狀態管理、容量控制
- **🚚 配送服務** - 司機分配、配送追蹤、容量管理
- **🔄 狀態管理** - 基於列舉的訂單狀態轉換與驗證
- **⚠️ 異常處理** - 完整的自訂異常階層架構
- **📝 審計日誌** - Log4j2 結構化日誌記錄系統

### 🏗️ 技術亮點
- **乾淨架構** - 清晰的分層設計，關注點分離
- **自訂異常** - 領域特定的檢查異常，精確的業務邏輯錯誤處理
- **狀態驗證** - 強固的訂單狀態轉換驗證機制
- **全面日誌** - INFO、WARN、ERROR 等級日誌記錄
- **整合測試** - 端到端測試涵蓋完整訂單流程
- **型別安全** - 強型別設計配合驗證註解

## 🏗️ 架構設計

```
外賣平台後端系統
├── 📋 controllers/     # 異常處理控制器
├── ⚙️ services/        # 業務邏輯服務層  
├── 💾 repositories/    # 資料存取層
├── 📦 models/          # 領域實體模型
├── ⚠️ exceptions/      # 自訂異常階層
└── 🧪 demo/            # 測試演示程式
```

### 🔑 核心元件

#### 📊 訂單狀態管理
```
PENDING → ACCEPTED → PREPARING → READY_FOR_DELIVERY → IN_DELIVERY → DELIVERED
                   ↓
                REJECTED / CANCELLED (終端狀態)
```

#### ⚠️ 異常階層架構
- **DeliveryPlatformException** - 所有業務邏輯錯誤的基礎檢查異常
- **OrderValidationException** - 訂單資料驗證錯誤
- **RestaurantUnavailableException** - 餐廳不可用（關閉、滿載等）
- **InvalidOrderStateException** - 無效的狀態轉換
- **DeliveryAssignmentException** - 配送員分配失敗

#### 🛠️ 服務層設計
- **OrderService** - 顧客訂單操作（建立、取消、更新）
- **RestaurantService** - 餐廳營運（接受、拒絕、準備、完成）
- **DeliveryService** - 配送操作（分配司機、完成配送）
- **OrderLoggingService** - 日誌記錄與審計追蹤

## 🚀 快速開始

### 📋 系統需求
- **Java 17** 或更新版本
- **Maven 3.6+** 建構工具
- **任何支援 Java 的 IDE** (IntelliJ IDEA、Eclipse、VS Code)

### ⚡ 一鍵啟動測試

```bash
# 克隆專案
git clone [your-repo-url]
cd food-delivery-platform

# 賦予執行權限
chmod +x run-simplified-tests.sh

# 檢視所有測試選項
./run-simplified-tests.sh
```

### 🎯 測試模式選擇

#### 1️⃣ 快速功能演示 (推薦新手)
```bash
./run-simplified-tests.sh quick
```
- ⏱️ **執行時間**: 30 秒
- 🎯 **適用於**: 快速驗證系統功能
- 📋 **內容**: 核心異常處理機制展示

#### 2️⃣ 自動化測試套件 (推薦 CI/CD)
```bash
./run-simplified-tests.sh auto  
```
- ⏱️ **執行時間**: 1 分鐘
- 🎯 **適用於**: 完整回歸測試
- 📊 **內容**: 13 個測試案例，涵蓋所有異常類型

#### 3️⃣ 詳細測試演示 (推薦學習)
```bash
./run-simplified-tests.sh detailed
```
- ⏱️ **執行時間**: 1 分鐘  
- 🎯 **適用於**: 深入了解系統運作
- 🔍 **內容**: 逐步展示完整訂單流程

#### 4️⃣ JUnit 單元測試 (推薦開發)
```bash
./run-simplified-tests.sh junit
```
- 🧪 **執行方式**: Maven 標準測試
- 📈 **涵蓋率**: 完整的單元測試與整合測試

## 📚 詳細使用方式

### 🔧 手動執行方式

```bash
# 編譯專案
mvn clean compile

# 選擇測試方案
mvn exec:java -Dexec.mainClass="com.deliveryplatform.demo.SimplifiedOrderDemo"      # 快速演示
mvn exec:java -Dexec.mainClass="com.deliveryplatform.demo.BatchTestRunner"         # 自動化測試  
mvn exec:java -Dexec.mainClass="com.deliveryplatform.demo.SimpleConsoleDemo"       # 詳細演示

# 執行標準測試
mvn test
```

### 📊 測試覆蓋範圍

| 測試類型 | 測試案例數 | 涵蓋範圍 | 執行時間 |
|----------|------------|----------|----------|
| **OrderValidationException** | 4 個 | 空值驗證、格式檢查 | ~5 秒 |
| **RestaurantUnavailableException** | 2 個 | 營業狀態、容量管理 | ~3 秒 |
| **InvalidOrderStateException** | 2 個 | 狀態轉換驗證 | ~3 秒 |
| **DeliveryAssignmentException** | 1 個 | 司機分配邏輯 | ~2 秒 |
| **完整訂單流程** | 1 個 | 端到端整合測試 | ~10 秒 |
| **邊界條件測試** | 3 個 | 極值與異常情況 | ~5 秒 |

## 🎨 範例程式碼

### 訂單建立與異常處理
```java
try {
    // 建立訂單
    List<OrderItem> items = Arrays.asList(
        new OrderItem("ITEM-001", "瑪格麗特披薩", 1, new BigDecimal("15.99"))
    );
    
    Order order = orderService.createOrder(
        "CUST-001", 
        "REST-001", 
        items, 
        "台北市信義區信義路五段7號"
    );
    
    System.out.println("✅ 訂單建立成功: " + order.getOrderId());
    
} catch (OrderValidationException e) {
    System.out.println("❌ 訂單驗證失敗:");
    e.getValidationErrors().forEach(error -> 
        System.out.println("  - " + error));
}
```

### 餐廳營運流程
```java
try {
    // 餐廳接受訂單
    restaurantService.acceptOrder(orderId, "REST-001");
    
    // 開始準備
    restaurantService.startPreparingOrder(orderId, "REST-001");
    
    // 標記準備完成
    restaurantService.markOrderReady(orderId, "REST-001");
    
} catch (RestaurantUnavailableException e) {
    System.out.println("餐廳不可用: " + e.getRestaurantId());
} catch (InvalidOrderStateException e) {
    System.out.println("狀態轉換錯誤: " + e.getCurrentStatus() + 
                      " → " + e.getAttemptedStatus());
}
```

## 📁 專案結構

```
src/
├── main/java/com/deliveryplatform/
│   ├── 📋 controllers/
│   │   └── GlobalExceptionHandler.java        # Console 異常處理器
│   ├── ⚙️ services/
│   │   ├── OrderService.java                  # 訂單服務
│   │   ├── RestaurantServiceImpl.java         # 餐廳服務實現
│   │   ├── DeliveryService.java               # 配送服務
│   │   └── OrderLoggingService.java           # 日誌服務
│   ├── 💾 repositories/
│   │   ├── OrderRepository.java               # 訂單資料介面
│   │   ├── InMemoryOrderRepository.java       # 記憶體訂單資料庫
│   │   ├── RestaurantRepository.java          # 餐廳資料介面  
│   │   └── InMemoryRestaurantRepository.java  # 記憶體餐廳資料庫
│   ├── 📦 models/
│   │   ├── Order.java                         # 訂單實體
│   │   ├── OrderItem.java                     # 訂單項目
│   │   ├── OrderStatus.java                   # 訂單狀態列舉
│   │   └── Restaurant.java                    # 餐廳實體
│   ├── ⚠️ exceptions/
│   │   ├── DeliveryPlatformException.java     # 基礎異常
│   │   ├── OrderValidationException.java      # 訂單驗證異常
│   │   ├── RestaurantUnavailableException.java # 餐廳不可用異常
│   │   ├── InvalidOrderStateException.java    # 狀態轉換異常
│   │   └── DeliveryAssignmentException.java   # 配送分配異常
│   └── 🧪 demo/
│       ├── SimplifiedOrderDemo.java           # 快速演示程式
│       ├── BatchTestRunner.java               # 自動化測試
│       ├── SimpleConsoleDemo.java             # 詳細測試演示
│       └── ConsoleTestRunner.java             # 互動式測試
└── test/java/com/deliveryplatform/
    ├── integration/OrderLifecycleIntegrationTest.java  # 整合測試
    ├── models/OrderTest.java                          # 模型測試
    ├── exceptions/CustomExceptionsTest.java           # 異常測試
    └── services/RestaurantServiceTest.java            # 服務測試
```

## 📊 專案統計

| 項目 | 數量 | 說明 |
|------|------|------|
| **Java 文件** | 28 個 | 核心業務邏輯實現 |
| **測試文件** | 5 個 | 完整的單元與整合測試 |
| **異常類別** | 5 個 | 自訂業務邏輯異常 |
| **服務介面** | 4 個 | 分層服務架構 |
| **測試案例** | 13+ 個 | 涵蓋所有異常情況 |
| **Maven 依賴** | 8 個 | 輕量化核心依賴 |

## 🔧 核心依賴

```xml
<!-- 日誌系統 -->
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
</dependency>
```

## 🧪 測試與驗證

### 📈 測試執行結果範例

```bash
🤖 自動化測試執行器 - 開始執行
============================================================

1️⃣ 測試 OrderValidationException
----------------------------------------
  1. 空客戶ID ... ✅ 通過 (OrderValidationException)
  2. 空餐廳ID ... ✅ 通過 (OrderValidationException)
  3. 空訂單項目 ... ✅ 通過 (OrderValidationException)
  4. 空地址 ... ✅ 通過 (OrderValidationException)

2️⃣ 測試 RestaurantUnavailableException  
----------------------------------------
  5. 關閉的餐廳 ... ✅ 通過 (RestaurantUnavailableException)
  6. 不存在的餐廳 ... ✅ 通過 (RestaurantUnavailableException)

📊 測試結果統計
============================================================
總測試數: 13
通過數: 12 ✅  
失敗數: 1 ❌
成功率: 92.3%

🎉 測試執行完成！
```

### 📝 日誌輸出範例

```
22:59:46.868 [main] INFO  Order - Order created: orderId=ORD-B51A5FDB, customerId=CUST-001, restaurantId=REST-001, amount=21.97

22:59:46.873 [main] WARN  OrderLoggingService - Business exception occurred: orderId=ORD-B51A5FDB, errorCode=RESTAURANT_UNAVAILABLE, message=Restaurant REST-001 is currently unavailable: Restaurant is closed

=== RESTAURANT UNAVAILABLE EXCEPTION ===
Error Code: RESTAURANT_UNAVAILABLE
Message: Restaurant REST-001 is currently unavailable: Restaurant is closed
Restaurant ID: REST-001  
Timestamp: 2025-10-08T22:59:46.873358
========================================
```

## 🌍 部署與環境

### 💻 本地開發環境
```bash
# 克隆並啟動
git clone [repo-url]
cd food-delivery-platform
./run-simplified-tests.sh quick
```

### 🐳 Docker 容器化
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/food-delivery-platform-1.0.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### ☁️ 雲端部署
- **AWS**: ECS、Lambda、Elastic Beanstalk
- **Azure**: Container Instances、Functions、App Service  
- **GCP**: Cloud Run、Functions、App Engine
- **Kubernetes**: 標準 Pod 部署

### 🤖 CI/CD 整合
```yaml
# GitHub Actions 範例
- name: Run Tests
  run: |
    mvn clean compile
    ./run-simplified-tests.sh auto
```

## 🎯 學習目標與應用

### 📚 適合學習主題
1. **Java 異常處理機制** - 檢查異常 vs 執行期異常
2. **Clean Architecture 設計** - 分層架構與依賴反轉
3. **狀態機設計模式** - 訂單狀態轉換邏輯
4. **Repository 模式** - 資料存取層抽象
5. **結構化日誌記錄** - Log4j2 最佳實踐
6. **單元測試與整合測試** - JUnit 5 + Mockito

### 🎓 教學應用場景
- **大學課程** - 軟體工程、Java 程式設計
- **企業培訓** - 後端開發、異常處理最佳實踐
- **面試準備** - 系統設計、程式碼品質
- **個人專案** - 學習現代 Java 開發技術

## 🤝 貢獻指南

### 🔧 開發環境設定
```bash
# 1. Fork 專案
# 2. 克隆到本地
git clone [your-fork-url]

# 3. 建立功能分支
git checkout -b feature/new-feature

# 4. 開發與測試
mvn clean compile
./run-simplified-tests.sh auto

# 5. 提交變更
git commit -m "新增功能: [描述]"
git push origin feature/new-feature
```

### 📋 程式碼規範
- 遵循 Java 命名慣例
- 所有公開方法需要 Javadoc
- 異常處理需要適當的日誌記錄
- 新功能需要對應的單元測試
- 保持 Clean Architecture 原則

## 📄 授權條款

本專案採用 MIT 授權條款，詳見 [LICENSE](LICENSE) 文件。

## 👨‍💻 作者資訊

**專案維護者**: [您的姓名]
**聯絡方式**: [您的郵件]
**專案連結**: [GitHub Repository]

---

**🎊 感謝使用外賣平台後端系統！如果這個專案對您有幫助，請給個 ⭐ Star！**
- **OrderLoggingService**: Centralized logging with structured messages

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Building the Project
```bash
mvn clean compile
```

### Running Tests
```bash
mvn test
```

### Project Structure
```
src/
├── main/java/com/deliveryplatform/
│   ├── exceptions/          # Custom exception hierarchy
│   ├── models/             # Domain entities (Order, Restaurant, OrderItem, OrderStatus)
│   ├── repositories/       # Data access interfaces and implementations
│   ├── services/           # Business logic services
│   └── controllers/        # Exception handlers
├── main/resources/
│   └── log4j2.xml         # Logging configuration
└── test/java/com/deliveryplatform/
    ├── exceptions/         # Exception tests
    ├── models/            # Entity tests  
    ├── services/          # Service tests
    └── integration/       # End-to-end integration tests
```

## Order Lifecycle Examples

### Successful Order Flow
1. **Customer creates order** → OrderStatus.PENDING
2. **Restaurant accepts** → OrderStatus.ACCEPTED  
3. **Restaurant starts preparing** → OrderStatus.PREPARING
4. **Restaurant marks ready** → OrderStatus.READY_FOR_DELIVERY
5. **Driver assigned** → OrderStatus.IN_DELIVERY
6. **Delivery completed** → OrderStatus.DELIVERED

### Exception Scenarios
- **Restaurant Unavailable**: When restaurant is closed or at capacity
- **Invalid State Transition**: Attempting to skip required workflow steps
- **Delivery Assignment Failure**: When no drivers are available
- **Order Validation Error**: When order data is incomplete or invalid

## Logging

### Log Levels
- **INFO**: Normal operations (order creation, status changes, successful operations)
- **WARN**: Business exceptions, potential issues, performance warnings
- **ERROR**: System errors, unchecked exceptions, critical failures

### Log Configuration
- **Console Logging**: Real-time monitoring during development
- **File Logging**: Persistent logs with rotation (10MB files, 10 file retention)
- **Error Logs**: Separate error file for critical issues
- **Structured Messages**: Consistent format with order IDs and context

### Sample Log Output
```
2024-10-02 14:29:51.742 [main] INFO BUSINESS_EVENTS - Order created: orderId=ORD-00B1D19F, customerId=CUST-004, restaurantId=REST-001, itemCount=2, totalAmount=37.96
2024-10-02 14:29:51.746 [main] WARN com.deliveryplatform.services.OrderLoggingService - Business exception occurred: orderId=ORD-00B1D19F, errorCode=RESTAURANT_UNAVAILABLE, message=Restaurant REST-001 is currently unavailable
```

## Testing

### Test Coverage
- **Unit Tests**: 83+ tests covering all core functionality
- **Integration Tests**: End-to-end order lifecycle scenarios
- **Exception Testing**: Comprehensive validation of error handling
- **State Transition Testing**: All valid and invalid state changes

### Key Test Scenarios
- Complete order lifecycle (PENDING → DELIVERED)
- Restaurant rejection and order cancellation
- Driver capacity management and assignment
- Invalid state transitions and error handling
- Order validation with various invalid data scenarios

### Running Specific Tests
```bash
# Run all tests
mvn test

# Run only unit tests
mvn test -Dtest="*Test"

# Run integration tests
mvn test -Dtest="*IntegrationTest"
```

## Configuration

### Log4j 2 Configuration
The logging configuration supports:
- Multiple appenders (Console, File, Error File)
- Rolling file policies with size and time-based triggers
- Different log levels for different packages
- Structured message formatting

### Repository Configuration
- In-memory repositories for development and testing
- Interface-based design for easy swapping to database implementations
- Concurrent data structures for thread safety

## Error Handling Strategy

### Checked Exceptions (Business Logic)
All business rule violations throw checked exceptions that must be handled:
- Forces explicit error handling in calling code
- Provides detailed error context and codes
- Enables proper logging and user feedback

### Unchecked Exception Handling
System errors are caught by global exception handlers:
- Prevents system crashes from unexpected errors
- Logs full stack traces for debugging
- Returns appropriate HTTP status codes

### Exception Context
All exceptions include:
- Unique error codes for programmatic handling
- Detailed error messages for users
- Timestamps for audit trails
- Relevant entity IDs (order, restaurant, driver)

## Development Guidelines

### Adding New Features
1. Define domain models with proper validation
2. Create custom exceptions for business rule violations  
3. Implement service layer with comprehensive logging
4. Add repository methods as needed
5. Write unit and integration tests
6. Update documentation

### Error Handling Best Practices
- Use checked exceptions for business logic errors
- Include relevant context in exception messages
- Log all exceptions with appropriate levels
- Provide user-friendly error responses
- Test both success and failure scenarios

## 📄 授權條款

本專案採用 MIT 授權條款，詳見 [LICENSE](LICENSE) 文件。

## 👨‍💻 作者資訊

**專案維護者**: [您的姓名]  
**聯絡方式**: [您的郵件]  
**專案連結**: [GitHub Repository]

---

**🎊 感謝使用外賣平台後端系統！如果這個專案對您有幫助，請給個 ⭐ Star！**