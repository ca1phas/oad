package model.enums;

// Represents the different statuses a reservation can have
public enum ReservationStatus {
    PENDING, // Waiting for approval
    APPROVED, // Approved by admin
    DENIED, // Rejected by admin
    CANCELLED, // Cancelled by user or system
    ACTIVE, // Currently active (book in use)
    EXPIRED, // Expired without being returned/collected
    RETURNED; // Book returned

    // Convert a string to a ReservationStatus (case-insensitive)
    public static ReservationStatus fromString(String status) {
        for (ReservationStatus s : ReservationStatus.values())
            if (s.name().equalsIgnoreCase(status.trim()))
                return s;
        throw new IllegalArgumentException("Invalid reservation status: " + status);
    }

    // Always return status in uppercase when printed
    @Override
    public String toString() {
        return name().toUpperCase();
    }
}
