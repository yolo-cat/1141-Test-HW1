# 🧪 無 GUI 測試完整方案

## 📖 概述

本文檔提供多種在**無 GUI 環境**下測試外賣平台異常處理系統的方案，適用於不同場景和需求。

## 🎯 四種測試方案

### 1️⃣ 互動式控制台測試 (推薦)

**適用場景**: 手動測試、學習、除錯
**特點**: 提供友好的選單界面，可逐步執行測試

```bash
# 啟動互動式測試
./run-console-tests.sh console

# 或直接使用 Maven
mvn exec:java -Dexec.mainClass="com.deliveryplatform.demo.ConsoleTestRunner"
```

**功能特色**:
- 📋 選單式操作界面
- 🎯 異常處理專項測試
- 📦 完整訂單流程測試  
- 🔄 自動化測試套件
- 📊 系統狀態檢查
- 📝 日誌查看功能

### 2️⃣ 批次自動測試 (CI/CD 推薦)

**適用場景**: 自動化測試、CI/CD 管道、回歸測試
**特點**: 無須人工干預，自動執行所有測試案例

```bash
# 執行批次測試
./run-console-tests.sh batch

# 或直接使用 Maven
mvn exec:java -Dexec.mainClass="com.deliveryplatform.demo.BatchTestRunner"
```

**測試覆蓋**:
- ✅ OrderValidationException (4個測試案例)
- ✅ RestaurantUnavailableException (2個測試案例)
- ✅ InvalidOrderStateException (2個測試案例)  
- ✅ DeliveryAssignmentException (1個測試案例)
- ✅ 完整訂單流程 (1個測試案例)
- ✅ 邊界條件測試 (3個測試案例)

### 3️⃣ 快速演示測試

**適用場景**: 快速驗證、功能展示、新手入門  
**特點**: 簡潔快速，重點展示核心異常處理

```bash
# 執行快速演示
./run-console-tests.sh demo

# 或直接使用 Maven  
mvn exec:java -Dexec.mainClass="com.deliveryplatform.demo.SimplifiedOrderDemo"
```

### 4️⃣ JUnit 單元測試

**適用場景**: 開發階段測試、IDE 整合測試
**特點**: 標準 JUnit 測試框架，詳細測試報告

```bash
# 執行 JUnit 測試
./run-console-tests.sh junit

# 或直接使用 Maven
mvn test
```

**注意**: 目前 JUnit 測試有時間相關的問題（餐廳營業時間），建議使用其他方案。

## 📊 測試結果示例

### 批次測試輸出示例
```
🤖 批次測試執行器 - 開始執行
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
  6. 不存在的餐廳 ... ✅ 通過 (Exception)

📊 測試結果統計
============================================================
總測試數: 13
通過數: 13 ✅
失敗數: 0 ❌
成功率: 100.0%

🎉 所有測試都通過了！系統運行正常。
```

### 互動式測試選單示例
```
==================================================
📋 主選單 - 請選擇測試類型:
==================================================
1. 🎯 異常處理測試
2. 📦 完整訂單流程測試
3. 🔄 自動化測試套件
4. 📊 系統狀態檢查
5. 📝 查看系統日誌
0. 🚪 退出程式
==================================================
請輸入選項 (0-5): 
```

## 🔧 快速開始

### 步驟 1: 編譯專案
```bash
mvn clean compile
```

### 步驟 2: 選擇測試方案
```bash
# 看看有哪些選項
./run-console-tests.sh

# 執行互動式測試 (推薦新手)
./run-console-tests.sh console

# 執行自動化測試 (推薦 CI/CD)
./run-console-tests.sh batch
```

## 🎯 各方案優缺點對比

| 方案 | 優點 | 缺點 | 適用場景 |
|------|------|------|----------|
| 互動式控制台 | 友好界面、可控性強、學習價值高 | 需要手動操作 | 開發、學習、除錯 |
| 批次自動測試 | 完全自動化、適合 CI/CD | 缺乏互動性 | 自動化測試、回歸測試 |
| 快速演示 | 快速簡潔、容易理解 | 測試覆蓋有限 | 功能展示、快速驗證 |
| JUnit 測試 | 標準框架、IDE 整合 | 目前有時間問題 | 開發階段（需修復） |

## 🚀 進階用法

### 在 Docker 中執行
```dockerfile
FROM openjdk:17-jdk-slim
COPY . /app
WORKDIR /app
RUN chmod +x run-console-tests.sh
CMD ["./run-console-tests.sh", "batch"]
```

### 在 CI/CD 管道中使用
```yaml
# GitHub Actions 示例
- name: Run Headless Tests
  run: |
    mvn clean compile
    ./run-console-tests.sh batch
```

### 自定義測試
您可以修改 `BatchTestRunner.java` 或 `ConsoleTestRunner.java` 來：
- 添加新的測試案例
- 修改測試邏輯
- 自定義測試報告格式
- 集成其他測試框架

## 💡 最佳實踐建議

1. **開發階段**: 使用互動式控制台測試進行除錯和驗證
2. **CI/CD 管道**: 使用批次自動測試確保程式碼品質  
3. **功能演示**: 使用快速演示展示系統功能
4. **回歸測試**: 定期運行完整的自動化測試套件

## 🔍 故障排除

### 常見問題
1. **時間相關測試失敗**: 使用新的測試方案，已避免時間依賴問題
2. **編譯錯誤**: 確保使用 Java 17+ 和 Maven 3.6+
3. **權限問題**: 確保腳本有執行權限 `chmod +x run-console-tests.sh`

### 日誌位置
- 控制台輸出：即時顯示
- Log4j 日誌：`logs/` 目錄
- Maven 日誌：`target/surefire-reports/`

---

**🎉 現在您有了完整的無 GUI 測試解決方案！選擇最適合您需求的方案開始測試吧。**