package com.deliveryplatform.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;

/**
 * 極簡風格 JavaFX 應用程式主類
 * 使用新的極簡主義設計風格
 */
public class MinimalExceptionDemoApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 設置應用程式圖標
        InputStream iconStream = getClass().getResourceAsStream("/icon.png");
        if (iconStream != null) {
            primaryStage.getIcons().add(new Image(iconStream));
        }

        // 載入極簡 FXML 文件
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/minimal.fxml"));
        Scene scene = new Scene(loader.load());
        
        // 載入極簡 CSS 樣式
        scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
        
        primaryStage.setTitle("異常處理演示系統 - 極簡版");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(700);
        primaryStage.setResizable(true);
        
        // 居中顯示
        primaryStage.centerOnScreen();
        primaryStage.show();
        
        System.out.println("=== 極簡風格 GUI 啟動成功 ===");
        System.out.println("- 純白背景，黑字設計");
        System.out.println("- 細邊框，無陰影效果"); 
        System.out.println("- 簡潔現代的界面風格");
        System.out.println("==============================");
    }

    public static void main(String[] args) {
        System.out.println("正在啟動極簡風格 GUI...");
        launch(args);
    }
}