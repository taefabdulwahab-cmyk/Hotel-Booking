package com.hotel;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // أول نافذة تنفتح: Login
        Parent root = FXMLLoader.load(getClass().getResource("View/Login.fxml"));
        scene = new Scene(root, 640, 480);
        primaryStage.setTitle("Hotel Booking System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // كل البنات يستخدمونها للتنقل بين النوافذ
    public static void setRoot(String fxml) throws IOException {
        Parent root = FXMLLoader.load(App.class.getResource(fxml + ".fxml"));
        scene.setRoot(root);
    }

    public static void main(String[] args) {
        launch(args);
    }
}