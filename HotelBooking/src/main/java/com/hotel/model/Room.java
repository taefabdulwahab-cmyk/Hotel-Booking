package com.hotel.model;

/**
 * Room - a hotel room.
 * student: Maysaa Anas 
 *
 * Plain Java class (POJO): no JavaFX imports, so the model stays independent of the UI (MVC).
 */
public class Room {

    // Allowed room types (use these in the ComboBox so everyone uses the same spelling)
    public static final String TYPE_SINGLE = "Single";
    public static final String TYPE_DOUBLE = "Double";
    public static final String TYPE_SUITE  = "Suite";

    private int roomNumber;        // unique, e.g. 101
    private String type;           // Single / Double / Suite
    private double pricePerNight;  // in SAR
    private boolean available;     // true = can be booked, false = out of service / occupied now

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

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

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