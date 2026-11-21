package com.hotel;
import com.hotel.controller.PaymentController;
import com.hotel.model.Reservation;

import com.hotel.model.DataStore;
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
        //يحمل البيانات المحفوظه اول مايشتغل البرنامج
        DataStore.loadAll();
        // أول نافذة تنفتح: Login
        Parent root = FXMLLoader.load(getClass().getResource("View/Login.fxml"));
        scene = new Scene(root, 1000, 650);
        primaryStage.setTitle("Hotel Booking System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    
    // كل البنات يستخدمونها للتنقل بين النوافذ
    public static void setRoot(String fxml) throws IOException {
        Parent root = FXMLLoader.load(App.class.getResource(fxml + ".fxml"));
        scene.setRoot(root);
    }
    
    public static void showPayment(Reservation reservation) throws IOException {
    FXMLLoader loader = new FXMLLoader(App.class.getResource("View/Payment.fxml"));

    Parent root = loader.load();

    PaymentController controller = loader.getController();
    controller.setReservation(reservation);

    scene.setRoot(root);
}

    public static void main(String[] args) {
        launch(args);
    }
}


