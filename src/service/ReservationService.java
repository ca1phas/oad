package service;

import model.Book;
import model.Reservation;
import model.enums.ReservationStatus;
import repository.ReservationRepository;
import util.IDGeneratorUtil;
import util.PaginationUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ReservationService {
    private final ReservationRepository reservationRepository;

    public ReservationService() {
        this.reservationRepository = new ReservationRepository();
    }

    // FR26: Reserve book
    public boolean reserveBook(Book book, String username, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate))
            return false;

        int id = IDGeneratorUtil.generateId(reservationRepository.getFilePath());
        Reservation reservation = new Reservation(id, book, username, LocalDateTime.now(),
                ReservationStatus.PENDING, startDate, endDate);

        reservationRepository.append(reservation);
        return true;
    }

    // FR10: Paginated reservation list (admin: all, member: own)
    public List<Reservation> getPaginatedReservations(String currentUsername, boolean isAdmin, int page, int pageSize) {
        List<Reservation> list = reservationRepository.readAll();

        if (!isAdmin) {
            list = list.stream()
                    .filter(r -> r.getUsername().equalsIgnoreCase(currentUsername))
                    .collect(Collectors.toList());
        }

        list.sort(Comparator.comparing(Reservation::getReservationDate).reversed());
        return PaginationUtil.paginate(list, page, pageSize);
    }

    // FR11 & FR12: Filter + Sort + Pagination
    public List<Reservation> filterSortPaginate(String currentUsername, boolean isAdmin,
            String idFilter, String usernameFilter, String bookTitleFilter,
            ReservationStatus statusFilter,
            LocalDate resStart, LocalDate resEnd,
            LocalDate startStart, LocalDate startEnd,
            LocalDate endStart, LocalDate endEnd,
            String sortField, boolean ascending,
            int page, int pageSize) {
        List<Reservation> list = reservationRepository.readAll();

        // Access filter
        if (!isAdmin) {
            list = list.stream()
                    .filter(r -> r.getUsername().equalsIgnoreCase(currentUsername))
                    .collect(Collectors.toList());
        }

        // Filters
        if (idFilter != null && !idFilter.isBlank()) {
            try {
                int id = Integer.parseInt(idFilter.trim());
                list = list.stream().filter(r -> r.getId() == id).collect(Collectors.toList());
            } catch (NumberFormatException ignored) {
            }
        }

        if (usernameFilter != null && !usernameFilter.isBlank()) {
            list = list.stream()
                    .filter(r -> r.getUsername().toLowerCase().contains(usernameFilter.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (bookTitleFilter != null && !bookTitleFilter.isBlank()) {
            list = list.stream()
                    .filter(r -> Optional
                            .ofNullable(r.getBook().getTitle())
                            .map(t -> t.toLowerCase().contains(bookTitleFilter.toLowerCase()))
                            .orElse(false))
                    .collect(Collectors.toList());
        }

        if (statusFilter != null) {
            list = list.stream().filter(r -> r.getStatus() == statusFilter).collect(Collectors.toList());
        }

        if (resStart != null) {
            list = list.stream().filter(r -> !r.getReservationDate().toLocalDate().isBefore(resStart))
                    .collect(Collectors.toList());
        }

        if (resEnd != null) {
            list = list.stream().filter(r -> !r.getReservationDate().toLocalDate().isAfter(resEnd))
                    .collect(Collectors.toList());
        }

        if (startStart != null) {
            list = list.stream().filter(r -> !r.getStartDate().isBefore(startStart)).collect(Collectors.toList());
        }

        if (startEnd != null) {
            list = list.stream().filter(r -> !r.getStartDate().isAfter(startEnd)).collect(Collectors.toList());
        }

        if (endStart != null) {
            list = list.stream().filter(r -> !r.getEndDate().isBefore(endStart)).collect(Collectors.toList());
        }

        if (endEnd != null) {
            list = list.stream().filter(r -> !r.getEndDate().isAfter(endEnd)).collect(Collectors.toList());
        }

        // Sort
        Comparator<Reservation> comparator = Comparator.comparing(Reservation::getReservationDate); // default
        switch (sortField != null ? sortField.toLowerCase() : "") {
            case "id":
                comparator = Comparator.comparing(Reservation::getId);
                break;
            case "username":
                comparator = Comparator.comparing(Reservation::getUsername, String.CASE_INSENSITIVE_ORDER);
                break;
            case "booktitle":
                comparator = Comparator.comparing(r -> r.getBook().getTitle(), String.CASE_INSENSITIVE_ORDER);
                break;
            case "status":
                comparator = Comparator.comparing(r -> r.getStatus().name());
                break;
            case "reservationdate":
                comparator = Comparator.comparing(Reservation::getReservationDate);
                break;
            case "startdate":
                comparator = Comparator.comparing(Reservation::getStartDate);
                break;
            case "enddate":
                comparator = Comparator.comparing(Reservation::getEndDate);
                break;
        }

        if (!ascending)
            comparator = comparator.reversed();

        list.sort(comparator);

        return PaginationUtil.paginate(list, page, pageSize);
    }

    // FR13 & 14: Select & View reservation by ID
    public Optional<Reservation> selectReservation(int reservationId, String currentUser, boolean isAdmin) {
        Optional<Reservation> opt = reservationRepository.findById(reservationId);
        if (opt.isEmpty())
            return Optional.empty();
        if (!isAdmin && !opt.get().getUsername().equalsIgnoreCase(currentUser))
            return Optional.empty();
        return opt;
    }

    // FR16: Update status
    public boolean updateStatus(int reservationId, String currentUser, boolean isAdmin, ReservationStatus newStatus) {
        Optional<Reservation> opt = reservationRepository.findById(reservationId);
        if (opt.isEmpty())
            return false;

        Reservation r = opt.get();
        ReservationStatus currentStatus = r.getStatus();

        if (isAdmin) {
            if (currentStatus != ReservationStatus.PENDING ||
                    !(newStatus == ReservationStatus.APPROVED || newStatus == ReservationStatus.DENIED))
                return false;
        } else {
            if (!r.getUsername().equalsIgnoreCase(currentUser))
                return false;
            if (currentStatus == ReservationStatus.PENDING && newStatus == ReservationStatus.CANCELLED) {
                // allowed
            } else if ((currentStatus == ReservationStatus.APPROVED || currentStatus == ReservationStatus.ACTIVE)
                    && newStatus == ReservationStatus.RETURNED) {
                // allowed
                r.setEndDate(LocalDate.now());
            } else
                return false;
        }

        r.setStatus(newStatus);
        return reservationRepository.update(r);
    }

    // FR17: Update start date
    public boolean updateStartDate(int reservationId, String currentUser, boolean isAdmin, LocalDate newStart) {
        Optional<Reservation> opt = reservationRepository.findById(reservationId);
        if (opt.isEmpty())
            return false;

        Reservation r = opt.get();
        if (!isAdmin && !r.getUsername().equalsIgnoreCase(currentUser))
            return false;
        if (r.getStatus() != ReservationStatus.PENDING)
            return false;

        if (newStart == null || newStart.isAfter(r.getEndDate())
                || newStart.isBefore(r.getReservationDate().toLocalDate()))
            return false;

        r.setStartDate(newStart);
        return reservationRepository.update(r);
    }

    // FR18: Update end date
    public boolean updateEndDate(int reservationId, String currentUser, boolean isAdmin, LocalDate newEnd) {
        Optional<Reservation> opt = reservationRepository.findById(reservationId);
        if (opt.isEmpty())
            return false;

        Reservation r = opt.get();
        if (!isAdmin && !r.getUsername().equalsIgnoreCase(currentUser))
            return false;
        if (r.getStatus() != ReservationStatus.PENDING)
            return false;

        if (newEnd == null || newEnd.isBefore(r.getStartDate()))
            return false;

        r.setEndDate(newEnd);
        return reservationRepository.update(r);
    }

    // FR19: Delete reservation (admin only)
    public boolean deleteReservation(int reservationId, boolean isAdmin) {
        if (!isAdmin)
            return false;

        Optional<Reservation> opt = reservationRepository.findById(reservationId);
        if (opt.isEmpty())
            return false;

        return reservationRepository.deleteByKey(String.valueOf(reservationId));
    }
}