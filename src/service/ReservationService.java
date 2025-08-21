package service;

import model.Reservation;
import model.User;
import model.enums.ReservationStatus;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ReservationService {
    private final List<Reservation> reservations = new ArrayList<>();

    

    public List<Reservation> getAllReservations() {
        return new ArrayList<>(reservations);
    }

    public Optional<Reservation> findById(int id) {
        return reservations.stream().filter(r -> r.getId() == id).findFirst();
    }

    public void addReservation(Reservation r) {
        reservations.add(r);
    }

    public boolean deleteReservation(int id) {
        return reservations.removeIf(r -> r.getId() == id);
    }

    // --- FR10 page ---
    public List<Reservation> getReservationsByPage(int page, int pageSize) {
        int start = (page - 1) * pageSize;
        if (start >= reservations.size()) return Collections.emptyList();
        return reservations.stream()
                .skip(start)
                .limit(pageSize)
                .collect(Collectors.toList());
    }

    // --- FR11 Sort reservations ---
    public List<Reservation> sortReservations(String field, String order) {
        Comparator<Reservation> comparator;

        switch (field) {
            case "id" -> comparator = Comparator.comparingInt(Reservation::getId);
            case "book" -> comparator = Comparator.comparing(r -> r.getBook().getTitle(), String.CASE_INSENSITIVE_ORDER);
            case "username" -> comparator = Comparator.comparing(Reservation::getUsername, String.CASE_INSENSITIVE_ORDER);
            case "date" -> comparator = Comparator.comparing(Reservation::getReservationDate);
            case "status" -> comparator = Comparator.comparing(r -> r.getStatus().name());
            default -> {
                System.out.println("Invalid sort field.");
                return reservations;
            }
        }

        if ("desc".equalsIgnoreCase(order)) comparator = comparator.reversed();
        return reservations.stream().sorted(comparator).collect(Collectors.toList());
    }

    // --- FR12 filter ---
    public List<Reservation> filterReservations(String field, String value) {
        return reservations.stream().filter(r -> {
            switch (field) {
                case "id" -> {
                    try { return r.getId() == Integer.parseInt(value); }
                    catch (NumberFormatException e) { return false; }
                }
                case "book" -> { return r.getBook().getTitle().equalsIgnoreCase(value); }
                case "username" -> { return r.getUsername().equalsIgnoreCase(value); }
                case "status" -> { return r.getStatus().name().equalsIgnoreCase(value); }
                case "date" -> { return r.getReservationDate().toLocalDate().toString().equals(value); }
                default -> { return false; }
            }
        }).collect(Collectors.toList());
    }

    // --- FR16 updatestatus ---
    public boolean updateStatus(int id, ReservationStatus newStatus, User currentUser) {
        Optional<Reservation> opt = findById(id);
        if (opt.isEmpty()) return false;

        Reservation r = opt.get();

        
        if (!currentUser.isAdmin() && !r.getUsername().equals(currentUser.getUsername())) {
            System.out.println("Access denied: You can only update your own reservations.");
            return false;
        }

        r.setStatus(newStatus);
        return true;
    }

    // --- FR17 update startDate ---
    public boolean updateStartDate(int id, LocalDate newDate, User currentUser) {
        Optional<Reservation> opt = findById(id);
        if (opt.isEmpty()) return false;

        Reservation r = opt.get();
        if (r.getStatus() != ReservationStatus.PENDING) return false;

        if (!currentUser.isAdmin() && !r.getUsername().equals(currentUser.getUsername())) {
            System.out.println("Access denied: You can only update your own reservations.");
            return false;
        }

        r.setStartDate(newDate);
        return true;
    }

    // --- FR18 update endDate ---
    public boolean updateEndDate(int id, LocalDate newDate, User currentUser) {
        Optional<Reservation> opt = findById(id);
        if (opt.isEmpty()) return false;

        Reservation r = opt.get();
        if (r.getStatus() != ReservationStatus.PENDING) return false;

        if (!currentUser.isAdmin() && !r.getUsername().equals(currentUser.getUsername())) {
            System.out.println("Access denied: You can only update your own reservations.");
            return false;
        }

        r.setEndDate(newDate);
        return true;
    }
}
