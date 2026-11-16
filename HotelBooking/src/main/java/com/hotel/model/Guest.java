// Author: Raneem Alharbi
package com.hotel.model;

public class Guest {

    private int guestId;
    private String name;
    private String phone;

    public Guest() {
    }

    public Guest(int guestId, String name, String phone) {
        this.guestId = guestId;
        this.name = name;
        this.phone = phone;
    }

    public int getGuestId() {
        return guestId;
    }

    public void setGuestId(int guestId) {
        this.guestId = guestId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return name;
    }
}