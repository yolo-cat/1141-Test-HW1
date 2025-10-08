# 實作計畫

- [x] 1. 建立專案結構與核心依賴
  - 建立 Maven 專案結構（src/main/java, src/test/java, src/main/resources）
  - 在 pom.xml 加入 Log4j 2、JUnit 5、Mockito 依賴
  - 建立 exceptions、models、services、repositories 等套件
  - _對應需求: 4.6, 5.2_

- [x] 2. 實作訂單狀態 enum 與狀態管理
  - [x] 2.1 建立 OrderStatus enum，含所有狀態
    - 定義 enum 值：PENDING, ACCEPTED, PREPARING, READY_FOR_DELIVERY, IN_DELIVERY, DELIVERED, CANCELLED, REJECTED
    - 實作 canTransitionTo() 狀態轉換驗證邏輯
    - _對應需求: 5.1, 5.4_
  
  - [x] 2.2 撰寫 OrderStatus 狀態轉換單元測試
    - 測試合法轉換回傳 true
    - 測試非法轉換回傳 false
    - _對應需求: 5.1, 5.4_

- [x] 3. 建立自訂例外階層
  - [x] 3.1 實作基礎 DeliveryPlatformException 類別
    - 建立含 errorCode 與 timestamp 欄位的抽象 checked 例外
    - 加入建構子與 getter 方法
    - _對應需求: 4.1, 5.2, 5.5_
  
  - [x] 3.2 實作特定商業例外
    - 建立 RestaurantUnavailableException，含餐廳 ID
    - 建立 InvalidOrderStateException，含狀態轉換細節
    - 建立 DeliveryAssignmentException，含訂單與失敗原因
    - 建立 OrderValidationException，處理訂單資料無效
    - _對應需求: 4.1, 5.2, 5.5_
  
  - [x] 3.3 撰寫自訂例外單元測試
    - 測試例外建立、錯誤碼與訊息
    - 驗證例外繼承與欄位值
    - _對應需求: 4.1, 5.2_

- [x] 4. 實作 Order 實體與資料模型
  - [x] 4.1 建立 Order 類別，含所有必要欄位
    - 加入 orderId, customerId, restaurantId, status, timestamps, items, totalAmount, deliveryAddress, driverId
    - 實作建構子、getter、setter
    - _對應需求: 1.1, 1.4, 6.1_
  
  - [x] 4.2 Order 類別加入狀態轉換方法
    - 實作 transitionTo() 並驗證狀態
    - 狀態轉換時記錄日誌
    - 非法轉換拋出 InvalidOrderStateException
    - _對應需求: 5.1, 5.4, 6.1_
  
  - [x] 4.3 建立 OrderItem 與 Restaurant 支援類別
    - OrderItem 含品項細節與價格
    - Restaurant 含營業狀態與容量管理
    - _對應需求: 2.1, 2.2_
  
  - [x] 4.4 撰寫 Order 實體單元測試
    - 測試訂單建立與欄位驗證
    - 測試狀態轉換方法（合法與非法）
    - _對應需求: 1.1, 5.1, 5.4_

- [x] 5. 設定 Log4j 2 與日誌服務
  - [x] 5.1 建立 Log4j 2 設定檔
    - 設定 console 與 file appender，指定格式
    - 設定檔案輪替策略
    - 定義各 package 日誌層級
    - _對應需求: 4.2, 4.3, 4.4, 4.5, 4.6_
  
  - [x] 5.2 實作 OrderLoggingService 類別
    - 建立訂單建立、狀態變更、例外等日誌方法
    - 實作 logOrderCreated(), logOrderStatusChange(), logBusinessException(), logSystemError()
    - 依情境使用 INFO、WARN、ERROR 層級
    - _對應需求: 4.2, 4.3, 4.4, 4.5, 6.1, 6.2_
  
  - [x] 5.3 撰寫日誌服務單元測試
    - 測試日誌格式與層級
    - 使用 mock appender 驗證日誌呼叫
    - _對應需求: 4.2, 4.3, 4.4, 4.5_

- [x] 6. 實作餐廳服務與訂單接受
  - [x] 6.1 建立 RestaurantService 介面
    - 定義 acceptOrder(), rejectOrder(), markOrderReady() 方法
    - 方法簽名含例外宣告
    - _對應需求: 2.1, 2.2, 2.3, 2.4, 2.5_
  
  - [x] 6.2 實作 RestaurantServiceImpl
    - acceptOrder() 驗證餐廳可接單
    - 餐廳無法接單或狀態錯誤時拋出例外
    - 所有操作皆記錄日誌
    - 實作 rejectOrder(), markOrderReady()
    - _對應需求: 2.1, 2.2, 2.3, 2.4, 2.5_
  
  - [x] 6.3 撰寫餐廳服務單元測試
    - 測試成功與失敗情境
    - 驗證例外與日誌
    - _對應需求: 2.1, 2.2, 2.3, 2.4, 2.5_

- [x] 7. 實作訂單管理服務
  - [x] 7.1 建立 OrderService 處理顧客訂單
    - createOrder() 含驗證與日誌
    - 提供訂單查詢與狀態查詢
    - 所有操作皆處理例外與日誌
    - _對應需求: 1.1, 1.2, 1.3, 1.4_
  
  - [x] 7.2 實作訂單取消功能
    - cancelOrder() 含狀態驗證
    - 取消事件記錄日誌與原因
    - _對應需求: 6.5_
  
  - [x] 7.3 撰寫訂單服務單元測試
    - 測試訂單建立（合法與非法）
    - 測試取消情境
    - 驗證例外與日誌
    - _對應需求: 1.1, 1.2, 1.3, 1.4, 6.5_

- [x] 8. 實作外送服務
  - [x] 8.1 建立 DeliveryService
    - assignDriver() 驗證外送員可用性
    - completeDelivery() 狀態更新
    - 指派失敗時拋出例外
    - _對應需求: 3.1, 3.2, 3.3, 3.4, 3.5_
  
  - [x] 8.2 外送員可用性管理
    - 指派邏輯含容量檢查
    - 外送操作與失敗皆記錄日誌
    - _對應需求: 3.1, 3.2, 3.3, 3.5_
  
  - [x] 8.3 撰寫外送服務單元測試
    - 測試外送員指派（可用與不可用）
    - 測試送達情境
    - 驗證例外與日誌
    - _對應需求: 3.1, 3.2, 3.3, 3.4, 3.5_

- [x] 9. 實作全域例外處理器
  - [x] 9.1 建立 GlobalExceptionHandler
    - 處理 DeliveryPlatformException 及其子類
    - 處理 RuntimeException 並記錄日誌
    - 依例外型別回傳對應 HTTP 狀態
    - _對應需求: 4.1, 4.2, 4.5_
  
  - [x] 9.2 例外上下文擷取與日誌
    - 擷取訂單編號與上下文
    - 整合 OrderLoggingService 一致記錄
    - _對應需求: 4.1, 4.2, 4.5, 6.2_
  
  - [x] 9.3 撰寫例外處理器單元測試
    - 測試各類例外處理
    - 驗證 HTTP 狀態與錯誤回應
    - 測試日誌整合
    - _對應需求: 4.1, 4.2, 4.5_

- [x] 10. 建立整合測試與系統驗證
  - [x] 10.1 端到端訂單流程整合測試
    - 測試訂單從建立到送達的完整流程
    - 驗證每步狀態轉換與日誌
    - 納入例外情境
    - _對應需求: 1.1, 2.1, 3.1, 4.1, 5.1, 6.1_
  
  - [x] 10.2 日誌驗證與稽核測試
    - 驗證日誌輸出是否含所有必要資訊
    - 測試日誌層級正確
    - 驗證例外日誌含上下文與堆疊
    - _對應需求: 4.2, 4.3, 4.4, 4.5, 6.1, 6.2, 6.3_
  
  - [x] 10.3 撰寫效能與錯誤情境測試
    - 測試高併發與資料庫連線失敗
    - 驗證日誌效能與檔案輪替
    - _對應需求: 4.6, 6.4_
