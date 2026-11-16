// Author: Raneem Alharbi
package com.hotel.controller;

import com.hotel.App;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Window;

public class LoginController implements Initializable {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;

    
    private final Predicate<String> validUsername = u -> u.length() >= 3;
    private final Predicate<String> validPassword = p -> p.length() >= 4;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

      
        loginButton.disableProperty().bind(
                usernameField.textProperty().isEmpty()
                .or(passwordField.textProperty().isEmpty()));

      
        passwordField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                login();
            }
        });
    }

    
    @FXML
    protected void handleLoginButtonAction(ActionEvent event) {
        login();
    }

    private void login() {
        Window owner = loginButton.getScene().getWindow();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, owner, "Form Error!",
                    "Please fill in all fields");
            return;
        }
        if (!validUsername.test(username)) {
            showAlert(Alert.AlertType.ERROR, owner, "Form Error!",
                    "Username must be at least 3 characters");
            return;
        }
        if (!validPassword.test(password)) {
            showAlert(Alert.AlertType.ERROR, owner, "Form Error!",
                    "Password must be at least 4 characters");
            return;
        }

        try {
            App.setRoot("View/Dashboard");
        } catch (Exception ex) {
          
            showAlert(Alert.AlertType.INFORMATION, owner, "Login Successful!",
                    "Welcome " + username + ". The Dashboard window is not ready yet.");
        }
    }

    public static void showAlert(Alert.AlertType alertType, Window owner,
                                 String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.show();
    }
}