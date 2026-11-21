package com.hotel.controller;

import com.hotel.App;
import com.hotel.model.DataStore;
import com.hotel.model.Guest;
import com.hotel.model.Reservation;
import com.hotel.model.Room;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
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
    @FXML
    private Button backButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        roomComboBox.setItems(
                FXCollections.observableArrayList(DataStore.getRooms())
        );

        calculateButton.setOnAction(event -> calculateTotal());
        confirmBookingButton.setOnAction(event -> confirmBooking());
        clearButton.setOnAction(event -> clearForm());
        roomComboBox.setOnAction(event -> showSelectedRoom());
        backButton.setOnAction(event -> {
    try {
        App.setRoot("View/Dashboard");
    } catch (Exception e) {
        showAlert("Navigation Error",
                "Unable to open Dashboard.");
    }
});
    }

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

        // Validate room availability
        if (!isRoomAvailable(selectedRoom, checkIn, checkOut)) {
            showAlert("Booking Error",
                    "This room is already reserved for the selected dates.");
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

    private boolean isRoomAvailable(Room selectedRoom,
            LocalDate checkIn, LocalDate checkOut) {

        for (Reservation reservation : DataStore.getReservations()) {

            if (reservation.getRoomNumber() == selectedRoom.getRoomNumber()
                    && !reservation.getStatus().equalsIgnoreCase("Cancelled")) {

                LocalDate existingCheckIn = reservation.getCheckInDate();
                LocalDate existingCheckOut = reservation.getCheckOutDate();

                if (checkIn.isBefore(existingCheckOut)
                        && checkOut.isAfter(existingCheckIn)) {
                    return false;
                }
            }
        }

        return true;
    }

    private void confirmBooking() {

        // Validate booking information and calculate total
        calculateTotal();

        String guestName = guestNameField.getText().trim();
        String phone = phoneField.getText().trim();
        Room selectedRoom = roomComboBox.getValue();
        LocalDate checkIn = checkInDatePicker.getValue();
        LocalDate checkOut = checkOutDatePicker.getValue();

        if (guestName.isEmpty() || !phone.matches("\\d{10}")
                || selectedRoom == null || checkIn == null
                || checkOut == null || !checkOut.isAfter(checkIn)) {
            return;
        }

        if (!isRoomAvailable(selectedRoom, checkIn, checkOut)) {
            return;
        }

        // Calculate total amount
        long numberOfNights =
                ChronoUnit.DAYS.between(checkIn, checkOut);

        double totalAmount =
                numberOfNights * selectedRoom.getPricePerNight();

        // Create guest
        int guestId = DataStore.nextGuestId();
        Guest guest = new Guest(guestId, guestName, phone);

        // Create reservation
        int reservationId = DataStore.nextReservationId();

        Reservation reservation = new Reservation(
                reservationId,
                guestId,
                selectedRoom.getRoomNumber(),
                checkIn,
                checkOut,
                "Confirmed",
                totalAmount
        );

        // Save guest and reservation
        DataStore.addGuest(guest);
        DataStore.addReservation(reservation);

        // Open Payment screen
        try {
            App.showPayment(reservation);
        } catch (IOException e) {
            showAlert("Navigation Error",
                    "The booking was saved, but the Payment screen could not be opened.");
        }
    }

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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}