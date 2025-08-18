// ReservationService.java
package service;

import model.Reservation;
import model.enums.ReservationStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ReservationService {
    private final List<Reservation> reservations;
    private final AtomicInteger idGenerator;

    public ReservationService() {
        this.reservations = new ArrayList<>();
        this.idGenerator = new AtomicInteger(1);
    }

    //  Generate unique ID (for controller usage)
    public int generateNewId() {
        return idGenerator.getAndIncrement();
    }

    //  Add new reservation (controller passes ready object)
    public boolean addReservation(Reservation reservation) {
        return reservations.add(reservation);
    }

    //  Find reservation by ID
    public Reservation findReservationById(int id) {
        return reservations.stream()
                .filter(r -> r.getId() == id)
                .findFirst()
                .orElse(null);
    }

    //  Get reservations by username
    public List<Reservation> getReservationsByUsername(String username) {
        return reservations.stream()
                .filter(r -> r.getUsername().equalsIgnoreCase(username))
                .collect(Collectors.toList());
    }

    //  Cancel reservation (with validation)
    public boolean cancelReservation(int reservationId, String username) {
        Reservation res = findReservationById(reservationId);
        if (res != null && res.getUsername().equalsIgnoreCase(username)) {
            if (res.getStatus() == ReservationStatus.PENDING ||
                res.getStatus() == ReservationStatus.APPROVED) {
                res.setStatus(ReservationStatus.CANCELLED);
                return true;
            }
        }
        return false;
    }

    //  Get all reservations
    public List<Reservation> getAllReservations() {
        return new ArrayList<>(reservations);
    }
}
