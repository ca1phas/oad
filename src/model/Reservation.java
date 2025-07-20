package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import model.enums.ReservationStatus;

public class Reservation {
    private int id;
    private int bookId;
    private String username;
    private LocalDateTime reservationDate;
    private ReservationStatus status;
    private LocalDate startDate;
    private LocalDate endDate;

    public Reservation(int id, int bookId, String username, LocalDateTime reservationDate,
            ReservationStatus status, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.bookId = bookId;
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

    public int getBookId() {
        return bookId;
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

    // Setters
    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return id + ": Book " + bookId
                + " reserved by " + username + " (" + status.toString() + ") on "
                + reservationDate + ", from " + startDate + " to " + endDate + ".";
    }
}
