#!/bin/bash

# 精簡化專案測試執行腳本
echo "================================================"
echo "🎯 精簡化外賣平台異常處理測試系統"
echo "================================================"
echo ""
echo "專案特色："
echo "✅ 移除 GUI - 純後端系統"
echo "✅ 移除 Spring - 輕量化架構"  
echo "✅ 保留核心 - 完整異常處理機制"
echo "✅ JAR 大小 - 減少 87% (8MB)"
echo ""

if [ "$1" = "quick" ]; then
    echo "🚀 執行快速功能演示..."
    mvn clean compile -q
    mvn exec:java -Dexec.mainClass="com.deliveryplatform.demo.SimplifiedOrderDemo"
    
elif [ "$1" = "auto" ]; then
    echo "🤖 執行自動化測試套件..."  
    mvn clean compile -q
    mvn exec:java -Dexec.mainClass="com.deliveryplatform.demo.BatchTestRunner"
    
elif [ "$1" = "detailed" ]; then
    echo "🔬 執行詳細測試演示..."
    mvn clean compile -q
    mvn exec:java -Dexec.mainClass="com.deliveryplatform.demo.SimpleConsoleDemo"
    
elif [ "$1" = "junit" ]; then
    echo "🧪 執行 JUnit 單元測試..."
    mvn test
    
else
    echo "請選擇測試模式："
    echo ""
    echo "1. 🚀 快速功能演示 (30秒)"
    echo "   ./run-simplified-tests.sh quick"
    echo ""
    echo "2. 🤖 自動化測試套件 (1分鐘)"  
    echo "   ./run-simplified-tests.sh auto"
    echo ""
    echo "3. 🔬 詳細測試演示 (1分鐘)"
    echo "   ./run-simplified-tests.sh detailed"
    echo ""
    echo "4. 🧪 JUnit 單元測試"
    echo "   ./run-simplified-tests.sh junit"
    echo ""
    echo "範例: ./run-simplified-tests.sh quick"
    echo ""
    echo "📊 專案統計："
    echo "   Java 文件: $(find src -name '*.java' | wc -l) 個"
    echo "   核心依賴: 8 個 (移除 JavaFX + Spring)"
    echo "   編譯時間: ~3 秒"
fi