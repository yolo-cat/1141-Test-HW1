package com.deliveryplatform.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;

/**
 * JavaFX 應用程式主類
 * 用於演示外賣平台異常處理和日誌記錄
 */
public class ExceptionDemoApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 設置應用程式圖標
        InputStream iconStream = getClass().getResourceAsStream("/icon.png");
        if (iconStream != null) {
            primaryStage.getIcons().add(new Image(iconStream));
        }

        // 載入 FXML 文件
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Scene scene = new Scene(loader.load());
        
        // 載入 CSS 樣式
        scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
        
        primaryStage.setTitle("外賣平台異常處理演示系統 - Exception Demo");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);
        primaryStage.show();
    }

    public static void main(String[] args) {
        // 設置 JavaFX 系統屬性
        System.setProperty("javafx.preloader", ExceptionDemoApp.class.getCanonicalName());
        launch(args);
    }
}