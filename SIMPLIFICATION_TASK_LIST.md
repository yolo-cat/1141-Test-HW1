# 🚀 程式碼簡化任務清單

基於 **COMPLEXITY_ANALYSIS.md** 和 **SIMPLIFICATION_RECOMMENDATION.md** 分析報告，建立具體的簡化任務清單。

## 📊 **簡化目標總覽**

| 階段 | 目標 | 預期減少 | 優先級 | 預計時間 |
|------|------|----------|--------|----------|
| **Phase 1** | 核心類別簡化 | 70% | 🔴 高 | 2-3天 |
| **Phase 2** | Service 層重構 | 60% | 🟡 中 | 2-3天 |
| **Phase 3** | 日誌系統整合 | 100% | 🟡 中 | 1-2天 |
| **Phase 4** | 測試與驗證 | - | 🟢 低 | 1-2天 |

**總體目標**: 5,578行 → 3,200行 (減少 43%)

---

## 🎯 **Phase 1: 核心類別簡化** (高優先級)

### 📦 **Task 1.1: 簡化 Order.java** 
**目標**: 393行 → 120行 (減少70%)

#### ✅ **具體任務**
- [x] **1.1.1** 移除非核心屬性
  ```java
  // 移除以下屬性
  - deliveryInstructions (非核心需求)
  - estimatedDeliveryTime (複雜計算邏輯)
  - orderNotes (額外功能)  
  - paymentMethod (超出範圍)
  - deliveryFee (計算複雜)
  ```

- [x] **1.1.2** 簡化狀態管理方法
  ```java
  // 保留核心方法，移除複雜邏輯
  ✅ 保留: updateStatus(OrderStatus newStatus)
  ❌ 移除: validateStatusTransition() 詳細驗證
  ❌ 移除: getStatusHistory() 狀態歷史
  ❌ 移除: canCancelOrder() 複雜判斷邏輯
  ❌ 移除: calculateEstimatedTime() 時間計算
  ```

- [x] **1.1.3** 簡化驗證邏輯
  ```java
  // 簡化 validate() 方法
  - 移除複雜的業務規則驗證
  - 只保留基本的 null 檢查
  - 移除 @Valid 註解的複雜驗證
  ```

- [x] **1.1.4** 移除輔助方法
  ```java
  // 移除非核心的 utility 方法 (約15個方法)
  ❌ calculateTax()
  ❌ formatOrderSummary()  
  ❌ generateTrackingNumber()
  ❌ isDeliveryTimeValid()
  ❌ getOrderAge()
  ```

**驗收標準**: Order.java 不超過 120行，保留 10個核心方法

---

### 🏪 **Task 1.2: 簡化 Restaurant.java**
**目標**: 261行 → 80行 (減少69%)

#### ✅ **具體任務**
- [x] **1.2.1** 移除營業時間邏輯
  ```java
  // 移除複雜的時間管理
  ❌ openingTime, closingTime 屬性
  ❌ isOpenAt(LocalTime time) 方法
  ❌ getBusinessHours() 方法
  ❌ isWithinDeliveryArea() 地理邏輯
  ```

- [x] **1.2.2** 簡化為基本實體
  ```java
  // 只保留核心屬性
  ✅ restaurantId
  ✅ name
  ✅ isOpen (boolean)
  ❌ 移除其他複雜屬性 (約20個)
  ```

- [x] **1.2.3** 移除容量管理
  ```java
  ❌ currentOrderCount
  ❌ maxCapacity  
  ❌ canAcceptMoreOrders()
  ❌ updateCapacity()
  ```

**驗收標準**: Restaurant.java 不超過 80行，只含基本實體屬性

---

### 📄 **Task 1.3: 保持 OrderItem.java 簡潔**
**目標**: 187行 → 100行 (減少47%)

#### ✅ **具體任務**
- [x] **1.3.1** 移除複雜計算邏輯
  ```java
  ❌ calculateSubtotalWithTax()
  ❌ applyDiscount()
  ❌ getFormattedPrice()
  ```

- [x] **1.3.2** 保留核心屬性
  ```java
  ✅ itemId, name, quantity, unitPrice
  ❌ 移除其他非核心屬性
  ```

**驗收標準**: OrderItem.java 不超過 100行

---

## ⚙️ **Phase 2: Service 層重構** (中優先級)

### 🏪 **Task 2.1: 重構 RestaurantServiceImpl.java**
**目標**: 372行 → 150行 (減少60%)

#### ✅ **具體任務**
- [ ] **2.1.1** 簡化 acceptOrder() 方法
  ```java
  // 保留核心邏輯，移除過度驗證
  ✅ 基本 null 檢查
  ✅ 餐廳存在檢查
  ✅ 訂單狀態更新
  ❌ 移除營業時間檢查
  ❌ 移除容量檢查
  ❌ 移除複雜的業務規則驗證
  ```

- [ ] **2.1.2** 移除輔助驗證方法
  ```java
  ❌ validateOrderAndRestaurantIds() (過度驗證)
  ❌ getRestaurantAndValidateAvailability() (複雜邏輯)
  ❌ validateBusinessRules() (非核心)
  ```

- [ ] **2.1.3** 簡化異常處理
  ```java
  // 保留核心異常，移除過度處理
  ✅ RestaurantUnavailableException
  ✅ InvalidOrderStateException  
  ❌ 移除過多的 try-catch 嵌套
  ```

- [ ] **2.1.4** 整合日誌記錄
  ```java
  // 移除 loggingService 依賴，使用直接日誌
  - logger.info("Restaurant {} accepted order {}", restaurantId, orderId);
  - logger.warn("Restaurant {} unavailable", restaurantId);
  ```

**驗收標準**: RestaurantServiceImpl.java 不超過 150行，核心功能完整

---

### 🚚 **Task 2.2: 重構 DeliveryService.java**  
**目標**: 315行 → 120行 (減少62%)

#### ✅ **具體任務**
- [ ] **2.2.1** 簡化司機管理
  ```java
  // 移除複雜的容量管理
  ❌ MAX_DRIVER_CAPACITY 邏輯
  ❌ driverOrderCount 追蹤
  ❌ 複雜的負載平衡算法
  
  // 簡化為基本分配
  ✅ 保留 availableDrivers Set
  ✅ 簡單的 first-available 分配
  ```

- [ ] **2.2.2** 簡化 assignDriver() 方法
  ```java
  // 只保留核心邏輯
  ✅ 檢查司機可用性
  ✅ 更新訂單狀態
  ✅ 基本異常處理
  ❌ 移除複雜的分配算法
  ```

- [ ] **2.2.3** 移除非核心方法
  ```java
  ❌ optimizeDriverRoutes()
  ❌ calculateDeliveryTime()  
  ❌ getDriverStatistics()
  ❌ balanceDriverLoad()
  ```

**驗收標準**: DeliveryService.java 不超過 120行

---

### 📦 **Task 2.3: 重構 OrderService.java**
**目標**: 310行 → 150行 (減少52%)

#### ✅ **具體任務**  
- [ ] **2.3.1** 簡化 createOrder() 方法
  ```java
  // 保留核心功能
  ✅ 基本驗證 (null 檢查)
  ✅ 訂單建立
  ✅ 狀態設定 (PENDING)
  ❌ 移除複雜的業務規則驗證
  ```

- [ ] **2.3.2** 移除非核心查詢方法
  ```java
  ❌ getOrdersByCustomer() (非核心需求)
  ❌ getOrdersByRestaurant() (非核心需求)
  ❌ getOrdersByStatus() (非核心需求)  
  ❌ findOrdersWithFilters() (複雜查詢)
  ```

- [ ] **2.3.3** 簡化驗證邏輯
  ```java
  // 只保留基本驗證
  ✅ validateOrderData() 基本檢查
  ❌ 移除複雜的業務邏輯驗證
  ```

**驗收標準**: OrderService.java 不超過 150行，專注核心訂單建立

---

## 📝 **Phase 3: 日誌系統整合** (中優先級)

### 🗑️ **Task 3.1: 移除 OrderLoggingService.java**
**目標**: 306行 → 0行 (減少100%)

#### ✅ **具體任務**
- [ ] **3.1.1** 分析日誌方法使用情況
  ```bash
  # 搜尋所有使用 OrderLoggingService 的地方
  grep -r "loggingService\." src/main/java/
  grep -r "OrderLoggingService" src/main/java/
  ```

- [ ] **3.1.2** 移除 Service 依賴
  ```java
  // 在各 Service 建構子中移除
  ❌ private final OrderLoggingService loggingService;
  ❌ OrderLoggingService 參數
  ```

- [ ] **3.1.3** 替換為直接日誌
  ```java
  // OrderService 中
  - loggingService.logOrderCreation() 
  + logger.info("Order created: orderId={}", orderId);
  
  // RestaurantService 中  
  - loggingService.logRestaurantOperation()
  + logger.info("Restaurant {} accepted order {}", restaurantId, orderId);
  
  // DeliveryService 中
  - loggingService.logDriverAssignment()
  + logger.info("Driver {} assigned to order {}", driverId, orderId);
  ```

- [ ] **3.1.4** 刪除 OrderLoggingService.java 檔案
  ```bash
  rm src/main/java/com/deliveryplatform/services/OrderLoggingService.java
  ```

**驗收標準**: OrderLoggingService 完全移除，日誌功能整合到各 Service

---

### 📋 **Task 3.2: 標準化日誌格式**

#### ✅ **具體任務**
- [ ] **3.2.1** 建立日誌格式標準
  ```java
  // INFO 級別：正常業務操作
  logger.info("Order created: orderId={}, customerId={}", orderId, customerId);
  logger.info("Restaurant {} accepted order {}", restaurantId, orderId);
  logger.info("Driver {} assigned to order {}", driverId, orderId);
  
  // WARN 級別：業務邏輯錯誤 (Checked Exception)
  logger.warn("Order validation failed: orderId={}, errors={}", orderId, errors);
  logger.warn("Restaurant unavailable: restaurantId={}", restaurantId);
  
  // ERROR 級別：系統錯誤 (Unchecked Exception)  
  logger.error("System error in createOrder", exception);
  logger.error("Database connection failed", exception);
  ```

- [ ] **3.2.2** 更新所有 Service 的日誌
  - [ ] OrderService 日誌標準化
  - [ ] RestaurantService 日誌標準化  
  - [ ] DeliveryService 日誌標準化

**驗收標準**: 所有 Service 使用統一的日誌格式

---

## 🧪 **Phase 4: 測試與驗證** (低優先級)

### 📋 **Task 4.1: 更新演示程式**

#### ✅ **具體任務**
- [ ] **4.1.1** 更新 CoreRequirementsDemo.java
  ```java
  // 移除對 OrderLoggingService 的依賴
  // 確保所有演示邏輯正常運作
  ```

- [ ] **4.1.2** 驗證其他演示程式
  - [ ] SimplifiedOrderDemo.java 
  - [ ] BatchTestRunner.java
  - [ ] SimpleConsoleDemo.java

- [ ] **4.1.3** 更新建構邏輯
  ```java
  // 移除 OrderLoggingService 參數
  orderService = new OrderService(orderRepository);
  restaurantService = new RestaurantServiceImpl(orderRepository, restaurantRepository);
  deliveryService = new DeliveryService(orderRepository);
  ```

**驗收標準**: 所有演示程式正常運行

---

### 🧪 **Task 4.2: 更新測試案例**

#### ✅ **具體任務**
- [ ] **4.2.1** 檢查單元測試
  ```bash
  # 確保所有測試通過
  mvn test
  ```

- [ ] **4.2.2** 更新測試中的 Service 建構
  - [ ] OrderTest.java
  - [ ] CustomExceptionsTest.java
  - [ ] RestaurantServiceTest.java
  - [ ] OrderLifecycleIntegrationTest.java

- [ ] **4.2.3** 移除 OrderLoggingService 相關測試
  ```java
  // 如果有針對 OrderLoggingService 的測試，需要移除或修改
  ```

**驗收標準**: 所有測試通過，測試覆蓋核心功能

---

### 📝 **Task 4.3: 更新文檔**

#### ✅ **具體任務**
- [ ] **4.3.1** 更新 README.md
  ```markdown
  # 更新程式碼範例
  # 移除 OrderLoggingService 相關說明
  # 更新專案統計數據
  ```

- [ ] **4.3.2** 更新架構圖
  ```markdown
  # 移除 OrderLoggingService 從架構圖
  # 更新專案結構說明
  ```

- [ ] **4.3.3** 建立簡化完成報告
  ```markdown
  # 記錄簡化前後對比
  # 記錄程式碼行數變化
  # 記錄功能保留情況
  ```

**驗收標準**: 文檔反映簡化後的專案狀態

---

## 📊 **驗收檢查清單**

### ✅ **完成標準**
- [ ] **程式碼行數**: 總行數減少到 3,200行以下 (減少43%)
- [ ] **核心功能**: 顧客訂單、餐廳收單、外送員接單功能完整
- [ ] **異常處理**: 5種自訂異常功能正常
- [ ] **日誌系統**: Log4j2 三級日誌正常運作
- [ ] **測試通過**: 所有演示程式和測試案例通過
- [ ] **編譯成功**: `mvn clean compile` 成功
- [ ] **執行正常**: `./run-core-requirements.sh run` 正常

### 📋 **品質標準**
- [ ] **可讀性**: 程式碼清晰易懂，專注核心邏輯
- [ ] **一致性**: 程式碼風格統一，命名規範
- [ ] **完整性**: 核心需求功能無缺失
- [ ] **穩定性**: 無編譯錯誤，無執行異常

---

## 🚀 **執行建議**

### 📅 **時程安排**
- **Week 1**: Phase 1 (核心類別簡化)
- **Week 2**: Phase 2 (Service 層重構)  
- **Week 3**: Phase 3 (日誌系統整合)
- **Week 4**: Phase 4 (測試與驗證)

### 🔧 **執行方式**
```bash
# 建立簡化分支
git checkout -b code-simplification

# 按 Phase 順序執行
# 每完成一個 Task 就 commit
# 每完成一個 Phase 就測試驗證
```

### 📝 **進度追蹤**
在此檔案中更新任務完成狀態：
- [ ] → ✅ (完成)
- [ ] → ⚠️ (進行中)  
- [ ] → ❌ (需要協助)

---

**🎯 簡化目標：專注核心需求，突出異常處理學習重點，提升程式碼品質與可讀性！**