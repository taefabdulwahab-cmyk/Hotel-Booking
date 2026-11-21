
package com.hotel.controller;

import com.hotel.App;
import com.hotel.model.DataStore;
import com.hotel.model.Reservation;
import com.hotel.model.Room;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Reservation Controller - manages hotel reservations.
 * Student: Shooq Mohammed Bawiyan
 */
public class ReservationController implements Initializable {

    // Reservation table and columns
    @FXML
    private TableView<Reservation> reservationTable;

    @FXML
    private TableColumn<Reservation, Integer> reservationIdColumn;

    @FXML
    private TableColumn<Reservation, Integer> guestIdColumn;

    @FXML
    private TableColumn<Reservation, Integer> roomColumn;

    @FXML
    private TableColumn<Reservation, LocalDate> checkInColumn;

    @FXML
    private TableColumn<Reservation, LocalDate> checkOutColumn;

    @FXML
    private TableColumn<Reservation, String> statusColumn;

    @FXML
    private TableColumn<Reservation, Double> totalColumn;

    // Search and date fields
    @FXML
    private TextField searchField;

    @FXML
    private DatePicker checkInDatePicker;

    @FXML
    private DatePicker checkOutDatePicker;

    // Reservation data
    private final ObservableList<Reservation> reservations =
            FXCollections.observableArrayList();

    private FilteredList<Reservation> filteredReservations;

    // Initialize reservation window
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        reservationIdColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().getReservationId()));

        guestIdColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().getGuestId()));

        roomColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().getRoomNumber()));

        checkInColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().getCheckInDate()));

        checkOutColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().getCheckOutDate()));

        statusColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().getStatus()));

        totalColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().getTotalCost()));

        // Load reservations
        reservations.setAll(DataStore.getReservations());

        filteredReservations =
                new FilteredList<>(reservations, r -> true);

        reservationTable.setItems(filteredReservations);

        // Search by reservation ID
        searchField.textProperty().addListener(
                (obs, oldValue, newValue) ->
                        searchReservations(newValue));

        // Show dates when a reservation is selected
        reservationTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, selected) -> {

                    if (selected != null) {
                        checkInDatePicker.setValue(
                                selected.getCheckInDate());

                        checkOutDatePicker.setValue(
                                selected.getCheckOutDate());
                    } else {
                        checkInDatePicker.setValue(null);
                        checkOutDatePicker.setValue(null);
                    }
                });
    }

    // Search reservations by ID
    private void searchReservations(String keyword) {

        String search = keyword == null
                ? "" : keyword.trim();

        filteredReservations.setPredicate(reservation -> {

            if (search.isEmpty()) {
                return true;
            }

            return String.valueOf(
                    reservation.getReservationId()).contains(search);
        });
    }

    // Update selected reservation
    @FXML
    private void handleUpdate() {

        Reservation selected = reservationTable
                .getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Validation Error",
                    "Please select a reservation.");
            return;
        }

        if (isCancelled(selected)) {
            showAlert("Validation Error",
                    "Cancelled reservations cannot be updated.");
            return;
        }

        LocalDate checkIn = checkInDatePicker.getValue();
        LocalDate checkOut = checkOutDatePicker.getValue();

        if (checkIn == null || checkOut == null) {
            showAlert("Validation Error",
                    "Please select both dates.");
            return;
        }

        if (!checkOut.isAfter(checkIn)) {
            showAlert("Validation Error",
                    "Check-out date must be after check-in date.");
            return;
        }

        // Find the room
        Room room = DataStore.getRooms().stream()
                .filter(r -> r.getRoomNumber()
                        == selected.getRoomNumber())
                .findFirst()
                .orElse(null);

        if (room == null) {
            showAlert("Validation Error",
                    "The reservation room was not found.");
            return;
        }

        // Check overlapping reservations
        for (Reservation other : DataStore.getReservations()) {

            if (other.getReservationId()
                    == selected.getReservationId()
                    || other.getRoomNumber()
                    != selected.getRoomNumber()
                    || isCancelled(other)) {
                continue;
            }

            if (checkIn.isBefore(other.getCheckOutDate())
                    && checkOut.isAfter(other.getCheckInDate())) {

                showAlert("Validation Error",
                        "The room is already booked for these dates.");
                return;
            }
        }

        // Calculate updated cost
        long nights = ChronoUnit.DAYS.between(
                checkIn, checkOut);

        double totalCost = nights * room.getPricePerNight();

        // Update reservation
        selected.setCheckInDate(checkIn);
        selected.setCheckOutDate(checkOut);
        selected.setTotalCost(totalCost);

        // Save changes
        DataStore.saveAll();
        reservationTable.refresh();

        showInfo("Success",
                "Reservation updated successfully.");
    }

    // Cancel selected reservation
    @FXML
    private void handleCancelReservation() {

        Reservation selected = reservationTable
                .getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Validation Error",
                    "Please select a reservation.");
            return;
        }

        if (isCancelled(selected)) {
            showAlert("Validation Error",
                    "This reservation is already cancelled.");
            return;
        }

        // Confirm cancellation
        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION);

        confirmation.setTitle("Cancel Reservation");
        confirmation.setHeaderText(null);
        confirmation.setContentText(
                "Are you sure you want to cancel this reservation?");

        if (confirmation.showAndWait().orElse(
                ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        // Change reservation status
        selected.setStatus("Cancelled");

        DataStore.saveAll();
        reservationTable.refresh();

        showInfo("Success",
                "Reservation cancelled successfully.");
    }

    // Check cancellation status
    private boolean isCancelled(Reservation reservation) {

        String status = reservation.getStatus();

        return "Cancelled".equalsIgnoreCase(status)
                || "Canceled".equalsIgnoreCase(status);
    }

    // Clear search, selection and dates
    @FXML
    private void handleClear() {

        searchField.clear();

        reservationTable.getSelectionModel()
                .clearSelection();

        checkInDatePicker.setValue(null);
        checkOutDatePicker.setValue(null);
    }

    // Return to Dashboard
    @FXML
    private void handleBack() {

        try {
            App.setRoot("View/Dashboard");

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();

            showAlert("Navigation Error",
                    "Could not open Dashboard.");
        }
    }

    // Warning message
    private void showAlert(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.WARNING);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    // Success message
    private void showInfo(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}
