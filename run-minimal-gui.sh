#!/bin/bash

# 極簡風格 GUI 啟動腳本
echo "==================================="
echo "🎨 極簡風格異常處理演示系統"
echo "==================================="
echo ""
echo "設計特色："
echo "✅ 純白底黑字配色"  
echo "✅ 細邊框設計 (1px)"
echo "✅ 無陰影極簡風格"
echo "✅ 現代優雅字體"
echo "✅ 中文界面"
echo ""
echo "正在編譯並啟動..."
echo ""

# 編譯專案
mvn clean compile -q

# 運行極簡 GUI
mvn exec:java -Dexec.mainClass="com.deliveryplatform.gui.MinimalExceptionDemoApp" -q