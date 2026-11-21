package com.hotel.controller;

import com.hotel.App;
import com.hotel.model.DataStore;
import com.hotel.model.Payment;
import com.hotel.model.Reservation;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Author: Taif Abdulwahab
 */

public class PaymentController implements Initializable {

    // Payment Information
    @FXML
    private TextField reservationIdField;

    @FXML
    private TextField amountField;

    @FXML
    private TextField paymentDateField;

    // Payment Method
    @FXML
    private ComboBox<String> paymentMethodComboBox;

    // Payment Status
    @FXML
    private Label paymentStatusLabel;

    // Buttons
    @FXML
    private Button confirmPaymentButton;

    @FXML
    private Button clearButton;

    @FXML
    private Button backButton;

    // Current Reservation
    private Reservation currentReservation;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Add payment methods
        paymentMethodComboBox.getItems().addAll(
                "Cash",
                "Credit Card",
                "Debit Card"
        );

        // Set button actions
        clearButton.setOnAction(event -> clearForm());

        confirmPaymentButton.setOnAction(
                event -> confirmPayment()
        );

        backButton.setOnAction(event -> {
            try {
                App.setRoot("View/Dashboard");
            } catch (IOException e) {
                showAlert("Navigation Error",
                        "Unable to open Dashboard.");
            }
        });
    }

    // Set reservation information
    public void setReservation(Reservation reservation) {

        currentReservation = reservation;

        reservationIdField.setText(
                String.valueOf(reservation.getReservationId())
        );

        amountField.setText(
                String.format(java.util.Locale.US, "%.2f",
                        reservation.getTotalCost())
        );

        paymentDateField.setText(
                LocalDate.now().toString()
        );

        // Check payment status
        if (isPaymentExists(reservation.getReservationId())) {
            paymentStatusLabel.setText("Paid");
        } else {
            paymentStatusLabel.setText("Pending");
        }
    }

    // Check if payment already exists
    private boolean isPaymentExists(int reservationId) {

        for (Payment payment : DataStore.getPayments()) {
            if (payment.getReservationId() == reservationId) {
                return true;
            }
        }

        return false;
    }

    // Validate payment information
    private void confirmPayment() {

        String reservationId = reservationIdField.getText().trim();
        String amountText = amountField.getText().trim();
        String paymentMethod = paymentMethodComboBox.getValue();

        if (currentReservation == null) {
            showAlert("Validation Error",
                    "No reservation information is available.");
            return;
        }

        if (reservationId.isEmpty()) {
            showAlert("Validation Error",
                    "Reservation ID is missing.");
            return;
        }

        if (amountText.isEmpty()) {
            showAlert("Validation Error",
                    "Payment amount is missing.");
            return;
        }

        try {
            int id = Integer.parseInt(reservationId);

            if (id <= 0 || id != currentReservation.getReservationId()) {
                showAlert("Validation Error",
                        "Invalid reservation ID.");
                return;
            }

        } catch (NumberFormatException e) {
            showAlert("Validation Error",
                    "Reservation ID must be a valid number.");
            return;
        }

        double amount;

        try {
            amount = Double.parseDouble(amountText);

            if (!Double.isFinite(amount) || amount <= 0) {
                showAlert("Validation Error",
                        "Payment amount must be greater than zero.");
                return;
            }

        } catch (NumberFormatException e) {
            showAlert("Validation Error",
                    "Please enter a valid payment amount.");
            return;
        }

        if (paymentMethod == null) {
            showAlert("Validation Error",
                    "Please select a payment method.");
            return;
        }

        // Check if payment already exists
        if (isPaymentExists(currentReservation.getReservationId())) {
            paymentStatusLabel.setText("Paid");

            showAlert("Payment Information",
                    "This reservation has already been paid.");
            return;
        }

        // Create payment
        int paymentId = DataStore.nextPaymentId();

        Payment payment = new Payment(
                paymentId,
                currentReservation.getReservationId(),
                amount,
                LocalDate.now(),
                paymentMethod
        );

        // Save payment
        DataStore.addPayment(payment);

        // Update payment status
        paymentStatusLabel.setText("Paid");

        paymentDateField.setText(
                LocalDate.now().toString()
        );

        showAlert("Payment Successful",
                "Payment has been completed successfully.");
    }

    // Clear payment fields
    private void clearForm() {

        reservationIdField.clear();
        amountField.clear();
        paymentDateField.clear();

        paymentMethodComboBox.getSelectionModel().clearSelection();

        paymentStatusLabel.setText("Pending");
    }

    // Display alert messages
    private void showAlert(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}