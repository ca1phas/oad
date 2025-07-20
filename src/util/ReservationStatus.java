package util;

public enum ReservationStatus {
    PENDING,
    APPROVED,
    DENIED,
    CANCELLED,
    ACTIVE,
    EXPIRED,
    RETURNED;

    public static ReservationStatus fromString(String status) {
        for (ReservationStatus s : ReservationStatus.values())
            if (s.name().equalsIgnoreCase(status.trim()))
                return s;
        throw new IllegalArgumentException("Invalid reservation status: " + status);
    }

    @Override
    public String toString() {
        return name().toLowerCase(); // "approved", "returned", etc.
    }
}
