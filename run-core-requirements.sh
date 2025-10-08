#!/bin/bash

# 核心需求演示腳本
echo "=================================================="
echo "🎯 美食外送伺服端核心需求演示"
echo "=================================================="
echo ""
echo "📋 演示內容："
echo "✅ 1. 收到顧客訂單 (Order 類別設計)"
echo "✅ 2. 餐廳收單 acceptOrder() (可能拋出例外)"
echo "✅ 3. 外送員接單 (DeliveryAssignment)"
echo "✅ 4. 各種例外處理 (Checked/Unchecked Exception)"
echo "✅ 5. Log4j2 不同日誌級別 (INFO/WARN/ERROR)"
echo "✅ 6. Enum 訂單狀態設計 (PENDING 等)"
echo "✅ 7. 客製化 Exception 設計"
echo ""

if [ "$1" = "run" ]; then
    echo "🚀 開始執行核心需求演示..."
    echo ""
    
    # 編譯專案
    mvn clean compile -q
    
    # 執行核心需求演示
    mvn exec:java -Dexec.mainClass="com.deliveryplatform.demo.CoreRequirementsDemo"
    
elif [ "$1" = "logs" ]; then
    echo "📝 查看產生的日誌文件..."
    echo ""
    
    if [ -f "logs/delivery-platform.log" ]; then
        echo "=== 日誌內容預覽 ==="
        tail -30 logs/delivery-platform.log
        echo ""
        echo "完整日誌請查看: logs/delivery-platform.log"
    else
        echo "❌ 日誌文件不存在，請先執行演示程式"
    fi
    
else
    echo "使用方式："
    echo ""
    echo "1. 🚀 執行核心需求演示"
    echo "   ./run-core-requirements.sh run"
    echo ""
    echo "2. 📝 查看產生的日誌"
    echo "   ./run-core-requirements.sh logs"
    echo ""
    echo "範例: ./run-core-requirements.sh run"
fi