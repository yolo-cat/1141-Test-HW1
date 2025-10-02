@echo off
:: 外賣平台異常處理演示系統啟動腳本 (Windows)
:: Food Delivery Platform Exception Demo GUI Launcher

echo 🍕 外賣平台異常處理演示系統
echo =============================
echo 正在啟動 GUI 應用程式...
echo.

:: 檢查 Java 版本
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 錯誤: 未找到 Java，請先安裝 Java 17 或更高版本
    pause
    exit /b 1
)

echo ✅ Java 檢查通過

:: 檢查 Maven
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 錯誤: 未找到 Maven，請先安裝 Maven
    pause
    exit /b 1
)

echo ✅ Maven 檢查通過

:: 編譯專案
echo 📦 正在編譯專案...
mvn compile -q
if %errorlevel% neq 0 (
    echo ❌ 編譯失敗，請檢查錯誤訊息
    pause
    exit /b 1
)

echo ✅ 編譯完成

:: 啟動 JavaFX 應用程式
echo 🚀 啟動 GUI 應用程式...
echo 請稍候，應用程式視窗即將出現...
echo.

:: 使用 JavaFX Maven 插件啟動
mvn javafx:run

echo.
echo 👋 應用程式已關閉
pause