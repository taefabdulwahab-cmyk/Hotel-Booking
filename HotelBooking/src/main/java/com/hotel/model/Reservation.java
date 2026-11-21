package com.hotel.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Reservation - represents a hotel reservation. Student: 
 * Shooq Mohammed Bawiyan
 
 */
public class Reservation {

    // Reservation information
    private int reservationId;
    private int guestId;
    private int roomNumber;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String status;
    private double totalCost;

    // Empty constructor
    public Reservation() {
    }

    // Constructor to create a reservation
    public Reservation(int reservationId, int guestId, int roomNumber,
            LocalDate checkInDate, LocalDate checkOutDate,
            String status, double totalCost) {
        this.reservationId = reservationId;
        this.guestId = guestId;
        this.roomNumber = roomNumber;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = status;
        this.totalCost = totalCost;
    }

    // Getters and setters for reservation data
    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public int getGuestId() {
        return guestId;
    }

    public void setGuestId(int guestId) {
        this.guestId = guestId;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    // Calculate the number of nights
    public long getNumberOfNights() {
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    // Convert reservation data to CSV for saving
    public String toCsv() {
        return reservationId + "," + guestId + "," + roomNumber + ","
                + checkInDate + "," + checkOutDate + ","
                + status + "," + totalCost;
    }

    // Read reservation data from a CSV line
    public static Reservation fromCsv(String line) {
        String[] p = line.split(",");

        return new Reservation(
                Integer.parseInt(p[0].trim()),
                Integer.parseInt(p[1].trim()),
                Integer.parseInt(p[2].trim()),
                LocalDate.parse(p[3].trim()),
                LocalDate.parse(p[4].trim()),
                p[5].trim(),
                Double.parseDouble(p[6].trim())
        );
    }

    // Display the reservation ID
    @Override
    public String toString() {
        return "Reservation " + reservationId;
    }
}
