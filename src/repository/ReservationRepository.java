package repository;

import model.Book;
import model.Reservation;
import model.enums.ReservationStatus;
import util.DateTimeUtil;

import java.util.*;

public class ReservationRepository extends BaseRepository<Reservation> {
    private final BookRepository bookRepository; // Used to fetch book details for reservations

    // Constructor sets file path and schema for reservations.txt
    public ReservationRepository() {
        super("data/reservations.txt", "id|bookId|username|reservationDate|status|startDate|endDate");
        bookRepository = new BookRepository();
    }

    // Convert a row from the file into a Reservation object
    @Override
    protected Reservation mapToModel(List<String> row) {
        Book book = bookRepository.findById(Integer.parseInt(row.get(1))).orElse(null);
        return new Reservation(
                Integer.parseInt(row.get(0)), // Reservation ID
                book, // Book object (fetched from BookRepository)
                row.get(2), // Username of reserver
                DateTimeUtil.parseDateTime(row.get(3)), // Reservation date (LocalDateTime)
                ReservationStatus.valueOf(row.get(4)), // Status (e.g., PENDING, ACTIVE, CANCELLED)
                DateTimeUtil.parseDate(row.get(5)), // Start date (LocalDate)
                DateTimeUtil.parseDate(row.get(6)) // End date (LocalDate)
        );
    }

    // Convert a Reservation object into a list of strings for file storage
    @Override
    protected List<String> mapFromModel(Reservation r) {
        return Arrays.asList(
                String.valueOf(r.getId()), // Reservation ID
                String.valueOf(r.getBook().getId()), // Book ID
                r.getUsername(), // Username
                DateTimeUtil.formatDateTime(r.getReservationDate()), // Reservation date (formatted)
                r.getStatus().toString(), // Status
                DateTimeUtil.formatDate(r.getStartDate()), // Start date (formatted)
                DateTimeUtil.formatDate(r.getEndDate()) // End date (formatted)
        );
    }

    // Find reservation by ID
    public Optional<Reservation> findById(int id) {
        return findByKey(String.valueOf(id));
    }

    // Update an existing reservation
    public boolean update(Reservation r) {
        return updateByKey(r);
    }

    // Delete reservation by ID
    public boolean delete(int id) {
        return deleteByKey(String.valueOf(id));
    }
}
