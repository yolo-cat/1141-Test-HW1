#!/bin/bash

# 外賣平台異常處理演示系統啟動腳本
# Food Delivery Platform Exception Demo GUI Launcher

echo "🍕 外賣平台異常處理演示系統"
echo "============================="
echo "正在啟動 GUI 應用程式..."
echo

# 檢查 Java 版本
java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{print $1}')
if [ "$java_version" -lt 17 ]; then
    echo "❌ 錯誤: 需要 Java 17 或更高版本"
    echo "當前版本: $(java -version 2>&1 | head -1)"
    exit 1
fi

echo "✅ Java 版本檢查通過"

# 檢查 Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ 錯誤: 未找到 Maven，請先安裝 Maven"
    exit 1
fi

echo "✅ Maven 檢查通過"

# 編譯專案
echo "📦 正在編譯專案..."
mvn compile -q
if [ $? -ne 0 ]; then
    echo "❌ 編譯失敗，請檢查錯誤訊息"
    exit 1
fi

echo "✅ 編譯完成"

# 啟動 JavaFX 應用程式
echo "🚀 啟動 GUI 應用程式..."
echo "請稍候，應用程式視窗即將出現..."
echo

# 使用 JavaFX Maven 插件啟動
mvn javafx:run

echo
echo "👋 應用程式已關閉"