package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import model.base.Identifiable;
import model.enums.ReservationStatus;

public class Reservation implements Identifiable {
    private int id; // Unique reservation ID
    private Book book; // The reserved book (reference to Book object)
    private String username; // Username of the member who made the reservation
    private LocalDateTime reservationDate; // Date & time when the reservation was created
    private ReservationStatus status; // Current reservation status (PENDING, ACTIVE, COMPLETED, CANCELLED, etc.)
    private LocalDate startDate; // Reservation start date
    private LocalDate endDate; // Reservation end date

    // Full constructor
    public Reservation(int id, Book book, String username, LocalDateTime reservationDate,
            ReservationStatus status, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.book = book;
        this.username = username;
        this.reservationDate = reservationDate;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters
    public int getId() {
        return id;
    }

    public Book getBook() {
        return book;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getReservationDate() {
        return reservationDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    // Setters for fields that can change
    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    // For displaying reservation details in a user-friendly format
    @Override
    public String toString() {
        return id + ": \n" + book.toString() + "\n"
                + " reserved by " + username + " (" + status.toString() + ") on "
                + reservationDate + ", from " + startDate + " to " + endDate + ".";
    }

    // Required by BaseRepository for uniquely identifying the record
    @Override
    public String getKey() {
        return String.valueOf(id); // Reservation ID as the key
    }
}
