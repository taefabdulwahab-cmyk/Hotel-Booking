// Author: Shatha
package com.hotel.controller;

import com.hotel.App;
import com.hotel.model.DataStore;
import com.hotel.model.Payment;
import com.hotel.model.Room;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class DashboardController implements Initializable {

    @FXML private Label availableLabel;
    @FXML private Label bookedLabel;
    @FXML private Label reservationsLabel;
    @FXML private Label revenueLabel;
    @FXML private ListView<String> reportList;

    @FXML private Button roomsButton;
    @FXML private Button bookingButton;
    @FXML private Button reservationsButton;
    @FXML private Button paymentButton;
    @FXML private Button logoutButton;

    // Predicate: is the room available?
    private final Predicate<Room> isAvailable = Room::isAvailable;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadStatistics();
        loadRoomsByTypeReport();

        // Buttons open the other windows (lambda handlers)
        roomsButton.setOnAction(e -> openWindow("View/Rooms"));
        bookingButton.setOnAction(e -> openWindow("View/RoomBooking"));
        reservationsButton.setOnAction(e -> openWindow("View/Reservations"));
        paymentButton.setOnAction(e -> openWindow("View/Payment"));
        logoutButton.setOnAction(e -> openWindow("View/Login"));
    }

    // Summary numbers (Stream API)
    private void loadStatistics() {
        long available = DataStore.getRooms().stream().filter(isAvailable).count();
        long booked = DataStore.getRooms().stream().filter(isAvailable.negate()).count();
        double revenue = DataStore.getPayments().stream()
                .mapToDouble(Payment::getAmount)
                .sum();

        availableLabel.setText(String.valueOf(available));
        bookedLabel.setText(String.valueOf(booked));
         reservationsLabel.setText(String.valueOf(DataStore.getReservations().size()));
        revenueLabel.setText(String.format(Locale.US, "%.2f", revenue));
    }

    // Report: rooms per type (Collectors)
    private void loadRoomsByTypeReport() {
        Map<String, Long> roomsByType = DataStore.getRooms().stream()
                .collect(Collectors.groupingBy(Room::getType, TreeMap::new, Collectors.counting()));

        reportList.setItems(FXCollections.observableArrayList(
                roomsByType.entrySet().stream()
                        .map(en -> en.getKey() + ": " + en.getValue() + " room(s)")
                        .collect(Collectors.toList())));

        if (roomsByType.isEmpty()) {
            reportList.setItems(FXCollections.observableArrayList("No rooms added yet"));
        }
    }

    // Open a window; show an alert if it is not available
    private void openWindow(String fxmlName) {
        try {
            App.setRoot(fxmlName);
        } catch (IOException | RuntimeException ex) {
            showAlert("Window not available",
                    "Could not open " + fxmlName.replace("View/", "") + ".");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
