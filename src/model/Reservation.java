package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import model.base.Identifiable;
import model.enums.ReservationStatus;

public class Reservation implements Identifiable {
    private int id;
    private Book book;
    private String username;
    private LocalDateTime reservationDate;
    private ReservationStatus status;
    private LocalDate startDate;
    private LocalDate endDate;

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
        return id + ": \n" + book.toString() + "\n"
                + " reserved by " + username + " (" + status.toString() + ") on "
                + reservationDate + ", from " + startDate + " to " + endDate + ".";
    }

    @Override
    public String getKey() {
        return String.valueOf(id);
    }
}
