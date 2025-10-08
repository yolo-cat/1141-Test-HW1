#!/bin/bash

# 無 GUI 測試執行腳本
echo "=========================================="
echo "🧪 外賣平台無 GUI 測試方案"
echo "=========================================="
echo ""

# 檢查參數
if [ "$1" = "batch" ]; then
    echo "🤖 執行批次自動測試..."
    echo "適用於 CI/CD 和自動化測試環境"
    echo ""
    mvn clean compile -q
    mvn exec:java -Dexec.mainClass="com.deliveryplatform.demo.BatchTestRunner" -q
elif [ "$1" = "console" ]; then
    echo "💻 啟動互動式控制台測試..."
    echo "適用於手動測試和除錯"
    echo ""
    mvn clean compile -q
    mvn exec:java -Dexec.mainClass="com.deliveryplatform.demo.ConsoleTestRunner"
elif [ "$1" = "demo" ]; then
    echo "🎯 執行簡化演示..."
    echo "快速展示核心異常處理功能"
    echo ""
    mvn clean compile -q
    mvn exec:java -Dexec.mainClass="com.deliveryplatform.demo.SimplifiedOrderDemo" -q
elif [ "$1" = "junit" ]; then
    echo "🧪 執行 JUnit 單元測試..."
    echo "注意: 目前有時間相關的測試失敗"
    echo ""
    mvn test
else
    echo "請選擇測試模式："
    echo ""
    echo "📋 可用的測試方案："
    echo ""
    echo "1. 💻 互動式控制台測試"
    echo "   ./run-console-tests.sh console"
    echo "   - 提供選單界面"
    echo "   - 可手動選擇測試項目" 
    echo "   - 適合逐步測試和學習"
    echo ""
    echo "2. 🤖 批次自動測試"
    echo "   ./run-console-tests.sh batch"
    echo "   - 自動執行所有測試"
    echo "   - 提供詳細測試報告"
    echo "   - 適合 CI/CD 環境"
    echo ""
    echo "3. 🎯 快速演示"
    echo "   ./run-console-tests.sh demo"
    echo "   - 快速展示核心功能"
    echo "   - 演示異常處理機制"
    echo "   - 適合快速驗證"
    echo ""
    echo "4. 🧪 JUnit 測試"
    echo "   ./run-console-tests.sh junit"
    echo "   - 執行標準單元測試"
    echo "   - Maven 測試報告"
    echo "   - 適合開發階段"
    echo ""
    echo "範例: ./run-console-tests.sh console"
fi