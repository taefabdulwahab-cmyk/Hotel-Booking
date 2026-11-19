package com.hotel.controller;

import com.hotel.model.Room;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Author: Taif Abdulwahab
 */
public class RoomBookingController implements Initializable {

    // Guest Information
    @FXML
    private TextField guestNameField;

    @FXML
    private TextField phoneField;

    // Room Selection
    @FXML
    private ComboBox<Room> roomComboBox;

    @FXML
    private TextField roomTypeField;

    @FXML
    private TextField priceField;

    // Booking Dates
    @FXML
    private DatePicker checkInDatePicker;

    @FXML
    private DatePicker checkOutDatePicker;

    // Total Amount
    @FXML
    private Label totalAmountLabel;

    // Buttons
    @FXML
    private Button calculateButton;

    @FXML
    private Button confirmBookingButton;

    @FXML
    private Button clearButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        calculateButton.setOnAction(event -> calculateTotal());

        clearButton.setOnAction(event -> clearForm());

        roomComboBox.setOnAction(event -> showSelectedRoom());
    }

    // Display selected room information
    private void showSelectedRoom() {

        Room selectedRoom = roomComboBox.getValue();

        if (selectedRoom != null) {

            roomTypeField.setText(selectedRoom.getType());

            priceField.setText(
                    String.format(Locale.US, "%.2f",
                            selectedRoom.getPricePerNight())
            );
        }
    }

    // Calculate total booking amount
    private void calculateTotal() {

        String guestName = guestNameField.getText().trim();
        String phone = phoneField.getText().trim();

        Room selectedRoom = roomComboBox.getValue();

        LocalDate checkIn = checkInDatePicker.getValue();
        LocalDate checkOut = checkOutDatePicker.getValue();

        // Validate guest name
        if (guestName.isEmpty()) {
            showAlert("Validation Error", "Please enter the guest name.");
            return;
        }

        // Validate phone number
        if (!phone.matches("\\d{10}")) {
            showAlert("Validation Error",
                    "Please enter a valid 10-digit phone number.");
            return;
        }

        // Validate room selection
        if (selectedRoom == null) {
            showAlert("Validation Error", "Please select a room.");
            return;
        }

        // Validate dates
        if (checkIn == null || checkOut == null) {
            showAlert("Validation Error",
                    "Please select both check-in and check-out dates.");
            return;
        }

        if (!checkOut.isAfter(checkIn)) {
            showAlert("Validation Error",
                    "Check-out date must be after check-in date.");
            return;
        }

        // Calculate number of nights
        long numberOfNights =
                ChronoUnit.DAYS.between(checkIn, checkOut);

        // Calculate total amount
        double pricePerNight = selectedRoom.getPricePerNight();

        double totalAmount = numberOfNights * pricePerNight;

        // Display total amount
        totalAmountLabel.setText(
                String.format(Locale.US, "%.2f SAR", totalAmount)
        );
    }

    // Clear all booking fields
    private void clearForm() {

        guestNameField.clear();
        phoneField.clear();

        roomComboBox.getSelectionModel().clearSelection();

        roomTypeField.clear();
        priceField.clear();

        checkInDatePicker.setValue(null);
        checkOutDatePicker.setValue(null);

        totalAmountLabel.setText("0.00 SAR");
    }

    // Display alert messages
    private void showAlert(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.WARNING);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}