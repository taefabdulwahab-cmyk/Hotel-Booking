package com.hotel.model;

import java.time.LocalDate;

/**
 * Room - a hotel room.
 * Owner: Maysaa Anas 
 *
 * Plain Java class (POJO): no JavaFX imports, so the model stays independent of the UI (MVC).
 *
 * A room is AVAILABLE when:
 *   1) it is in service (the "Available" checkbox in the Rooms window), and
 *   2) it has no active reservation (checked automatically from DataStore).
 * So booking a room or cancelling a reservation updates the room status by itself.
 */
public class Room {

    // Allowed room types (use these in the ComboBox so everyone uses the same spelling)
    public static final String TYPE_SINGLE = "Single";
    public static final String TYPE_DOUBLE = "Double";
    public static final String TYPE_SUITE  = "Suite";

    private int roomNumber;        // unique, e.g. 101
    private String type;           // Single / Double / Suite
    private double pricePerNight;  // in SAR
    private boolean available;     // true = in service, false = out of service (set by staff)

    public Room(int roomNumber, String type, double pricePerNight, boolean available) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.available = available;
    }

    // ---------- Getters & Setters ----------
    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(double pricePerNight) { this.pricePerNight = pricePerNight; }

    /** true only if the room is in service AND not booked */
    public boolean isAvailable() { return available && !isBooked(); }
    public void setAvailable(boolean available) { this.available = available; }

    /** true if staff marked the room as in service (ignores reservations) */
    public boolean isInService() { return available; }

    // ---------- Booking status (Streams + Predicate) ----------
    /**
     * true if this room has an active reservation:
     * not cancelled / checked out, and the check-out date has not passed yet.
     */
    public boolean isBooked() {
        LocalDate today = LocalDate.now();
        return DataStore.getReservations().stream()
                .filter(r -> r.getRoomNumber() == roomNumber)
                .filter(r -> isActiveStatus(r.getStatus()))
                .anyMatch(r -> r.getCheckOutDate() == null || !r.getCheckOutDate().isBefore(today));
    }

    /** Active = anything except Cancelled / Checked Out / Completed */
    private static boolean isActiveStatus(String status) {
        if (status == null) {
            return true;
        }
        String s = status.toLowerCase().replaceAll("[\\s_-]", "");
        return !(s.equals("cancelled") || s.equals("canceled")
                || s.equals("checkedout") || s.equals("completed"));
    }

    /** Text for the Status column in the Rooms table */
    public String getStatusText() {
        if (!available) {
            return "Out of Service";
        }
        return isBooked() ? "Booked" : "Available";
    }

    // ---------- File saving ----------
    /** One line in rooms.csv: roomNumber,type,pricePerNight,available */
    public String toCsv() {
        return roomNumber + "," + type + "," + pricePerNight + "," + available;
    }

    /** Builds a Room from one line of rooms.csv */
    public static Room fromCsv(String line) {
        String[] p = line.split(",");
        return new Room(Integer.parseInt(p[0].trim()), p[1].trim(),
                        Double.parseDouble(p[2].trim()), Boolean.parseBoolean(p[3].trim()));
    }

    /** What a ComboBox / ListView shows for this room */
    @Override
    public String toString() {
        return "Room " + roomNumber + " (" + type + ")";
    }
}