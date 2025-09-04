package repository;

import model.Book;
import model.Reservation;
import model.enums.ReservationStatus;
import util.DateTimeUtil;

import java.util.*;

public class ReservationRepository extends BaseRepository<Reservation> {
    private final BookRepository bookRepository;

    public ReservationRepository() {
        super("data/reservations.txt", "id|bookId|username|reservationDate|status|startDate|endDate");
        bookRepository = new BookRepository();
    }

    @Override
    protected Reservation mapToModel(List<String> row) {
        Book book = bookRepository.findById(Integer.parseInt(row.get(1))).orElse(null);
        return new Reservation(
                Integer.parseInt(row.get(0)),
                book,
                row.get(2),
                DateTimeUtil.parseDateTime(row.get(3)),
                ReservationStatus.valueOf(row.get(4)),
                DateTimeUtil.parseDate(row.get(5)),
                DateTimeUtil.parseDate(row.get(6)));
    }

    @Override
    protected List<String> mapFromModel(Reservation r) {
        return Arrays.asList(
                String.valueOf(r.getId()),
                String.valueOf(r.getBook().getId()),
                r.getUsername(),
                DateTimeUtil.formatDateTime(r.getReservationDate()),
                r.getStatus().toString(),
                DateTimeUtil.formatDate(r.getStartDate()),
                DateTimeUtil.formatDate(r.getEndDate()));
    }

    public Optional<Reservation> findById(int id) {
        return findByKey(String.valueOf(id));
    }

    public boolean update(Reservation r) {
        return updateByKey(r);
    }

    public boolean delete(int id) {
        return deleteByKey(String.valueOf(id));
    }
}
