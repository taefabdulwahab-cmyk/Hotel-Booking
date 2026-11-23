package com.hotel.controller;

import com.hotel.App;
import com.hotel.model.DataStore;
import com.hotel.model.Room;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

/**
 * RoomsController - controls the Room Management window (View/Rooms.fxml).
 * Owner: Maysaa
 *
 * Add, update, delete, search, filter and sort hotel rooms.
 */
public class RoomsController {

    // ---------- Form ----------
    @FXML private TextField roomNumberField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField priceField;
    @FXML private CheckBox availableCheck;

    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;

    // ---------- Search / filter / sort ----------
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCombo;
    @FXML private ComboBox<String> sortCombo;

    // ---------- Table ----------
    @FXML private TableView<Room> roomsTable;
    @FXML private TableColumn<Room, Integer> numberColumn;
    @FXML private TableColumn<Room, String> typeColumn;
    @FXML private TableColumn<Room, Double> priceColumn;
    @FXML private TableColumn<Room, String> statusColumn;

    @FXML private Label countLabel;

    private static final String FILTER_ALL = "All Rooms";
    private static final String FILTER_AVAILABLE = "Available";
    private static final String FILTER_NOT_AVAILABLE = "Not Available";

    private static final String SORT_NUMBER = "Sort by Room Number";
    private static final String SORT_PRICE = "Sort by Price";

    /** Runs automatically after the FXML is loaded */
    @FXML
    public void initialize() {
        // Fill the ComboBoxes
        typeCombo.getItems().addAll(Room.TYPE_SINGLE, Room.TYPE_DOUBLE, Room.TYPE_SUITE);
        filterCombo.getItems().addAll(FILTER_ALL, FILTER_AVAILABLE, FILTER_NOT_AVAILABLE);
        filterCombo.setValue(FILTER_ALL);
        sortCombo.getItems().addAll(SORT_NUMBER, SORT_PRICE);
        sortCombo.setValue(SORT_NUMBER);
        availableCheck.setSelected(true);

        // Table columns (lambdas: what each column shows for a room)
        numberColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getRoomNumber()));
        typeColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType()));
        priceColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getPricePerNight()));
        statusColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatusText()));

        // PROPERTY EVENT (binding): "Add" is disabled until all fields are filled
        addButton.disableProperty().bind(
                roomNumberField.textProperty().isEmpty()
                        .or(priceField.textProperty().isEmpty())
                        .or(typeCombo.valueProperty().isNull()));

        // PROPERTY EVENT (binding): "Update" and "Delete" are disabled until a row is selected
        updateButton.disableProperty().bind(roomsTable.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.disableProperty().bind(roomsTable.getSelectionModel().selectedItemProperty().isNull());

        // PROPERTY EVENT (ChangeListener): live search while typing + filter + sort
        searchField.textProperty().addListener((obs, oldText, newText) -> refreshTable());
        filterCombo.valueProperty().addListener((obs, oldValue, newValue) -> refreshTable());
        sortCombo.valueProperty().addListener((obs, oldValue, newValue) -> refreshTable());

        // MOUSE EVENT: double-click a row to load it into the form for editing
        roomsTable.setOnMouseClicked(event -> {
            Room selected = roomsTable.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 2 && selected != null) {
                fillForm(selected);
            }
        });

        // KEYBOARD EVENT: press Enter in the price field to add the room
        priceField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !addButton.isDisabled()) {
                handleAdd();
            }
        });

        refreshTable();
    }

    // =====================================================================
    //  ACTION EVENTS (buttons) - connected in Rooms.fxml with onAction="#..."
    // =====================================================================

    @FXML
    private void handleAdd() {
        Room newRoom = readForm(null);
        if (newRoom == null) {
            return;                       // invalid input: message already shown
        }
        DataStore.addRoom(newRoom);       // adds and saves to file
        clearForm();
        refreshTable();
        showInfo("Room " + newRoom.getRoomNumber() + " was added.");
    }

    @FXML
    private void handleUpdate() {
        Room selected = roomsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a room from the table first.");
            return;
        }
        Room newData = readForm(selected);
        if (newData == null) {
            return;
        }
        selected.setRoomNumber(newData.getRoomNumber());
        selected.setType(newData.getType());
        selected.setPricePerNight(newData.getPricePerNight());
        selected.setAvailable(newData.isInService());
        DataStore.saveAll();              // we edited with setters, so save
        clearForm();
        refreshTable();
        showInfo("Room " + selected.getRoomNumber() + " was updated.");
    }

    @FXML
    private void handleDelete() {
        Room selected = roomsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a room from the table first.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete room " + selected.getRoomNumber() + "?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        Optional<ButtonType> answer = confirm.showAndWait();
        if (answer.isPresent() && answer.get() == ButtonType.YES) {
            DataStore.removeRoom(selected);   // removes and saves to file
            clearForm();
            refreshTable();
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    /** Back to the Dashboard window */
    @FXML
    private void handleBack() {
        try {
            App.setRoot("View/Dashboard");
        } catch (IOException e) {
            showError("Could not open the Dashboard window.");
        }
    }

    // =====================================================================
    //  STREAMS: search (filter), sort (sorted), count
    // =====================================================================
    private void refreshTable() {
        final String text = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        final String filter = filterCombo.getValue();

        // Predicate 1: room number or type contains the search text
        Predicate<Room> matchesSearch = room ->
                String.valueOf(room.getRoomNumber()).contains(text)
                || room.getType().toLowerCase().contains(text);

        // Predicate 2: matches the chosen filter (All / Available / Not Available)
        Predicate<Room> matchesFilter = room ->
                filter == null || filter.equals(FILTER_ALL)
                || (filter.equals(FILTER_AVAILABLE) && room.isAvailable())
                || (filter.equals(FILTER_NOT_AVAILABLE) && !room.isAvailable());

        Comparator<Room> order = SORT_PRICE.equals(sortCombo.getValue())
                ? Comparator.comparingDouble(Room::getPricePerNight)
                : Comparator.comparingInt(Room::getRoomNumber);

        List<Room> result = DataStore.getRooms().stream()
                .filter(matchesSearch.and(matchesFilter))
                .sorted(order)
                .collect(Collectors.toList());

        roomsTable.setItems(FXCollections.observableArrayList(result));

        long availableCount = DataStore.getRooms().stream()
                .filter(Room::isAvailable)
                .count();
        countLabel.setText("Total rooms: " + DataStore.getRooms().size()
                + "     Available: " + availableCount
                + "     Showing: " + result.size());
    }

    // =====================================================================
    //  VALIDATION
    // =====================================================================
    /**
     * Reads and checks the form.
     * @param editing the room being edited (null when adding a new room)
     * @return a Room with the form values, or null if something is wrong
     */
    private Room readForm(Room editing) {
        String numberText = roomNumberField.getText().trim();
        String priceText = priceField.getText().trim();
        String type = typeCombo.getValue();

        // 1) Empty fields
        if (numberText.isEmpty() || priceText.isEmpty() || type == null) {
            showError("Please fill in all fields.");
            return null;
        }

        // 2) Room number must be a positive whole number
        int number;
        try {
            number = Integer.parseInt(numberText);
        } catch (NumberFormatException e) {
            showError("Room number must be a whole number, for example 101.");
            return null;
        }
        if (number <= 0) {
            showError("Room number must be greater than 0.");
            return null;
        }

        // 3) Price must be a positive number
        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            showError("Price must be a number, for example 350.");
            return null;
        }
        if (price <= 0) {
            showError("Price must be greater than 0.");
            return null;
        }

        // 4) No duplicate room numbers (ignore the room we are editing)
        final int roomNumber = number;
        boolean duplicate = DataStore.getRooms().stream()
                .anyMatch(r -> r.getRoomNumber() == roomNumber && r != editing);
        if (duplicate) {
            showError("Room " + roomNumber + " already exists.");
            return null;
        }

        return new Room(roomNumber, type, price, availableCheck.isSelected());
    }

    // =====================================================================
    //  Helpers
    // =====================================================================
    private void fillForm(Room room) {
        roomNumberField.setText(String.valueOf(room.getRoomNumber()));
        typeCombo.setValue(room.getType());
        priceField.setText(String.valueOf(room.getPricePerNight()));
        availableCheck.setSelected(room.isInService());
    }

    private void clearForm() {
        roomNumberField.clear();
        typeCombo.setValue(null);
        priceField.clear();
        availableCheck.setSelected(true);
        roomsTable.getSelectionModel().clearSelection();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}