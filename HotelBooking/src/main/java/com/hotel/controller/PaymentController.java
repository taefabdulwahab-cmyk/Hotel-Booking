package com.hotel.controller;

import java.net.URL;
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
    }

    // Validate payment information
    private void confirmPayment() {

        String reservationId = reservationIdField.getText().trim();
        String amountText = amountField.getText().trim();
        String paymentMethod = paymentMethodComboBox.getValue();

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

            if (id <= 0) {
                showAlert("Validation Error",
                        "Invalid reservation ID.");
                return;
            }

        } catch (NumberFormatException e) {
            showAlert("Validation Error",
                    "Reservation ID must be a valid number.");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);

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

        showAlert("Payment Information",
                "Payment information is valid.\n"
                + "Saving functionality will be added later.");
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