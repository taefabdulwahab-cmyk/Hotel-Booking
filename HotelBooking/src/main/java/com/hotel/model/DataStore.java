// Author: Shatha
package com.hotel.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DataStore {

    private static final Path DIR = Paths.get("data");

    private static final List<Room> rooms = new ArrayList<>();
    private static final List<Guest> guests = new ArrayList<>();
//    private static final List<Reservation> reservations = new ArrayList<>();
    private static final List<Payment> payments = new ArrayList<>();

    private DataStore() {
    }

    public static List<Room> getRooms() { return rooms; }
    public static List<Guest> getGuests() { return guests; }
//    public static List<Reservation> getReservations() { return reservations; }
    public static List<Payment> getPayments() { return payments; }

    public static void addRoom(Room r) { rooms.add(r); saveAll(); }
    public static void removeRoom(Room r) { rooms.remove(r); saveAll(); }

    public static void addGuest(Guest g) { guests.add(g); saveAll(); }
    public static void removeGuest(Guest g) { guests.remove(g); saveAll(); }

//    public static void addReservation(Reservation r) { reservations.add(r); saveAll(); }
//    public static void removeReservation(Reservation r) { reservations.remove(r); saveAll(); }

    public static void addPayment(Payment p) { payments.add(p); saveAll(); }
    public static void removePayment(Payment p) { payments.remove(p); saveAll(); }

    public static boolean roomExists(int roomNumber) {
        return rooms.stream().anyMatch(r -> r.getRoomNumber() == roomNumber);
    }

    public static int nextGuestId() {
        return guests.stream().mapToInt(Guest::getGuestId).max().orElse(0) + 1;
    }

//    public static int nextReservationId() {
//        return reservations.stream().mapToInt(Reservation::getReservationId).max().orElse(0) + 1;
//    }

     public static int nextPaymentId() {
        return payments.stream().mapToInt(Payment::getPaymentId).max().orElse(0) + 1;
    }

    public static void saveAll() {
        try {
            Files.createDirectories(DIR);
            write("rooms.csv", rooms, Room::toCsv);
            write("guests.csv", guests, g -> g.getGuestId() + "," + g.getName() + "," + g.getPhone());
//            write("reservations.csv", reservations, Reservation::toCsv);
            write("payments.csv", payments, Payment::toCsv);
        } catch (IOException e) {
            System.err.println("Could not save data: " + e.getMessage());
        }
    }

    private static <T> void write(String file, List<T> items, Function<T, String> toCsv)
            throws IOException {
        List<String> lines = items.stream().map(toCsv).collect(Collectors.toList());
        Files.write(DIR.resolve(file), lines, StandardCharsets.UTF_8);
    }

    public static void loadAll() {
        rooms.clear();
        guests.clear();
//        reservations.clear();
        payments.clear();

        rooms.addAll(read("rooms.csv", Room::fromCsv));
        guests.addAll(read("guests.csv", line -> {
            String[] p = line.split(",");
            return new Guest(Integer.parseInt(p[0].trim()), p[1].trim(), p[2].trim());
        }));
//        reservations.addAll(read("reservations.csv", Reservation::fromCsv));
        payments.addAll(read("payments.csv", Payment::fromCsv));

        if (rooms.isEmpty()) {
            rooms.add(new Room(101, Room.TYPE_SINGLE, 200, true));
            rooms.add(new Room(102, Room.TYPE_DOUBLE, 350, true));
            rooms.add(new Room(201, Room.TYPE_SUITE, 700, true));
            saveAll();
        }
    }

    private static <T> List<T> read(String file, Function<String, T> fromCsv) {
        List<T> result = new ArrayList<>();
        Path path = DIR.resolve(file);
        if (!Files.exists(path)) {
            return result;
        }
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                if (line.isBlank()) {
                    continue;
                }
                try {
                    result.add(fromCsv.apply(line));
                } catch (RuntimeException ex) {
                    System.err.println("Skipped bad line in " + file + ": " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read " + file + ": " + e.getMessage());
        }
        return result;
    }
}
