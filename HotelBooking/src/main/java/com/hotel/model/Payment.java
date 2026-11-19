package com.hotel.model;

import java.time.LocalDate;

/**
 * Author: Taif Abdulwahab
 */
public class Payment {

    private int paymentId;
    private int reservationId;
    private double amount;
    private LocalDate paymentDate;
    private String paymentMethod;

    // Default Constructor
    public Payment() {
    }

    // Parameterized Constructor
    public Payment(int paymentId, int reservationId,
                   double amount, LocalDate paymentDate,
                   String paymentMethod) {

        this.paymentId = paymentId;
        this.reservationId = reservationId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
    }

    // Getters and Setters
    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    // Convert Payment object to CSV format
    public String toCsv() {
        return paymentId + ","
                + reservationId + ","
                + amount + ","
                + paymentDate + ","
                + paymentMethod;
    }

    // Create Payment object from CSV data
    public static Payment fromCsv(String line) {

        String[] p = line.split(",");

        return new Payment(
                Integer.parseInt(p[0].trim()),
                Integer.parseInt(p[1].trim()),
                Double.parseDouble(p[2].trim()),
                LocalDate.parse(p[3].trim()),
                p[4].trim()
        );
    }

    @Override
    public String toString() {
        return "Payment #" + paymentId
                + " - " + amount + " SAR";
    }
}