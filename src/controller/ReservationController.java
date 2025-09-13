package controller;

import model.Book;
import model.Reservation;
import model.User;
import model.enums.ReservationStatus;
import service.BookService;
import service.ReservationService;
import util.PaginationUtil;
import util.DateTimeUtil;
import view.ReservationsView;
import view.BookView;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ReservationController {
    private final ReservationService reservationService;
    private final BookService bookService;
    private final ReservationsView view;
    private final BookView bookView;

    private String sortField = "id";
    private boolean ascending = true;
    private String idFilter = null;
    private String usernameFilter = null;
    private String bookTitleFilter = null;
    private String statusFilter = null;
    private LocalDate resStart = null;
    private LocalDate resEnd = null;
    private LocalDate startStart = null;
    private LocalDate startEnd = null;
    private LocalDate endStart = null;
    private LocalDate endEnd = null;

    public ReservationController(ReservationService reservationService,
            BookService bookService,
            ReservationsView view,
            BookView bookView) {
        this.reservationService = reservationService;
        this.bookService = bookService;
        this.view = view;
        this.bookView = bookView;

        reservationService.updateReservationStatusesOnStartup();
    }

    public void handleReservationsMenu(User currentUser) {
        boolean isAdmin = currentUser.isAdmin();
        boolean running = true;

        while (running) {
            view.displayMainMenu();
            int choice = view.promptInt("Enter your choice: ");

            switch (choice) {
                case 1 -> { // Full list
                    idFilter = null;
                    usernameFilter = null;
                    bookTitleFilter = null;
                    statusFilter = null;
                    resStart = null;
                    resEnd = null;
                    startStart = null;
                    startEnd = null;
                    endStart = null;
                    endEnd = null;
                    sortField = "id";
                    ascending = true;
                    handlePaginatedReservations(currentUser, isAdmin);
                }
                case 2 -> { // Sort & Filter
                    ReservationsView.SortFilterOptions sf = view.getSortAndFilterOptions();
                    sortField = sf.sortBy;
                    ascending = sf.ascending;
                    idFilter = sf.idFilter;
                    usernameFilter = sf.usernameFilter;
                    bookTitleFilter = sf.bookTitleFilter;
                    statusFilter = sf.statusFilter;
                    resStart = sf.resStart != null ? DateTimeUtil.parseDate(sf.resStart) : null;
                    resEnd = sf.resEnd != null ? DateTimeUtil.parseDate(sf.resEnd) : null;
                    startStart = sf.startStart != null ? DateTimeUtil.parseDate(sf.startStart) : null;
                    startEnd = sf.startEnd != null ? DateTimeUtil.parseDate(sf.startEnd) : null;
                    endStart = sf.endStart != null ? DateTimeUtil.parseDate(sf.endStart) : null;
                    endEnd = sf.endEnd != null ? DateTimeUtil.parseDate(sf.endEnd) : null;
                    handlePaginatedReservations(currentUser, isAdmin);
                }
                case 3 -> { // Detail
                    int resId = view.promptInt("Enter reservation ID: ");
                    Optional<Reservation> opt = reservationService.selectReservation(resId, currentUser.getUsername(),
                            isAdmin);
                    opt.ifPresentOrElse(r -> handleReservationDetails(r, currentUser),
                            () -> view.showMessage("Reservation not found or not accessible."));
                }
                case 4 -> handleReserveBook(currentUser);
                case 0 -> running = false;
                default -> view.showMessage("Invalid choice.");
            }
        }
    }

    private void handlePaginatedReservations(User currentUser, boolean isAdmin) {
        int page = 1;
        int pageSize = 5;
        boolean running = true;

        while (running) {
            List<Reservation> paginated = reservationService.filterSortPaginate(
                    currentUser.getUsername(), isAdmin,
                    idFilter, usernameFilter, bookTitleFilter,
                    statusFilter != null ? ReservationStatus.valueOf(statusFilter) : null,
                    resStart, resEnd, startStart, startEnd, endStart, endEnd,
                    sortField, ascending,
                    page, pageSize);

            if (paginated.isEmpty()) {
                view.showMessage("\n No reservations found.");
                return;
            }

            int totalItems = reservationService.filterSortPaginate(
                    currentUser.getUsername(), isAdmin,
                    idFilter, usernameFilter, bookTitleFilter,
                    statusFilter != null ? ReservationStatus.valueOf(statusFilter) : null,
                    resStart, resEnd, startStart, startEnd, endStart, endEnd,
                    sortField, ascending,
                    1, Integer.MAX_VALUE).size();

            int totalPages = PaginationUtil.getTotalPages(totalItems, pageSize);
            view.displayReservationsTable(paginated, page, totalPages);

            int choice = view.promptInt("Enter your choice: ");
            switch (choice) {
                case 1 -> {
                    if (page > 1)
                        page--;
                    else
                        view.showMessage("Already at first page.");
                }
                case 2 -> {
                    if (page < totalPages)
                        page++;
                    else
                        view.showMessage("Already at last page.");
                }
                case 0 -> running = false;
                default -> view.showMessage("Invalid choice.");
            }
        }
    }

    private void handleReservationDetails(Reservation r, User currentUser) {
        boolean isAdmin = currentUser.isAdmin();
        boolean running = true;

        while (running) {
            view.displayReservationDetails(r);
            view.displayDetailsMenu(r, currentUser);
            int choice = view.promptInt("Your choice: ");

            switch (choice) {
                case 1 -> { // View linked book
                    if (r.getBook() != null) {
                        Optional<Book> bookOpt = bookService.viewBook(r.getBook().getId());
                        bookOpt.ifPresentOrElse(bookView::showBookDetails, () -> view.showMessage("Book not found."));
                        System.out.println("\nPress Enter to go back...");
                        new Scanner(System.in).nextLine();
                    } else
                        view.showMessage("No book linked to this reservation.");
                }
                case 2 -> { // Admin: update status | User: update start date
                    if (isAdmin) {
                        try {
                            ReservationStatus newStatus = ReservationStatus
                                    .valueOf(view.getStatusInput(currentUser).toUpperCase());
                            boolean ok = reservationService.updateStatus(r.getId(),
                                    currentUser.getUsername(), true, newStatus);
                            view.showMessage(ok ? "Status updated!" : "Failed.");
                            if (ok)
                                r = reservationService.selectReservation(r.getId(), currentUser.getUsername(), true)
                                        .orElse(r);
                        } catch (IllegalArgumentException e) {
                            view.showMessage("Invalid status.");
                        }
                    } else {
                        try {
                            LocalDate start = DateTimeUtil.parseDate(view.getDateInput("Enter new start date"));
                            boolean ok = reservationService.updateStartDate(r.getId(),
                                    currentUser.getUsername(), false, start);
                            view.showMessage(ok ? "Start date updated!" : "Failed.");
                            if (ok)
                                r = reservationService.selectReservation(r.getId(),
                                        currentUser.getUsername(), false).orElse(r);
                        } catch (Exception e) {
                            view.showMessage("Invalid date format.");
                        }
                    }
                }
                case 3 -> { // Admin: update start date | User: update end date
                    if (isAdmin) {
                        try {
                            LocalDate start = DateTimeUtil.parseDate(view.getDateInput("Enter new start date"));
                            boolean ok = reservationService.updateStartDate(r.getId(),
                                    currentUser.getUsername(), true, start);
                            view.showMessage(ok ? "Start date updated!" : "Failed.");
                            if (ok)
                                r = reservationService.selectReservation(r.getId(), currentUser.getUsername(), true)
                                        .orElse(r);
                        } catch (Exception e) {
                            view.showMessage("Invalid date format.Reservation failed");
                        }
                    } else {
                        try {
                            LocalDate end = DateTimeUtil.parseDate(view.getDateInput("Enter new end date"));
                            boolean ok = reservationService.updateEndDate(r.getId(),
                                    currentUser.getUsername(), false, end);
                            view.showMessage(ok ? "End date updated!" : "Failed.");
                            if (ok)
                                r = reservationService.selectReservation(r.getId(),
                                        currentUser.getUsername(), false).orElse(r);
                        } catch (Exception e) {
                            view.showMessage("Invalid date format.");
                        }
                    }
                }
                case 4 -> { // Admin: update end date
                    if (isAdmin) {
                        try {
                            LocalDate end = DateTimeUtil.parseDate(view.getDateInput("Enter new end date"));
                            boolean ok = reservationService.updateEndDate(r.getId(),
                                    currentUser.getUsername(), true, end);
                            view.showMessage(ok ? "End date updated!" : "Failed.");
                            if (ok)
                                r = reservationService.selectReservation(r.getId(), currentUser.getUsername(), true)
                                        .orElse(r);
                        } catch (Exception e) {
                            view.showMessage("Invalid date format.Reservation failed");
                        }
                    } else
                        view.showMessage("Invalid choice.");
                }
                case 5 -> { // Admin: delete
                    if (isAdmin) {
                        boolean ok = reservationService.deleteReservation(r.getId(), true);
                        view.showMessage(ok ? "Deleted successfully!" : "Failed.");
                        if (ok)
                            running = false;
                    } else
                        view.showMessage("Invalid choice.");
                }
                case 0 -> running = false;
                default -> view.showMessage("Invalid choice.");
            }
        }
    }

    private void handleReserveBook(User currentUser) {
        int bookId = view.promptInt("Enter Book ID to reserve: ");
        Optional<Book> bookOpt = bookService.viewBook(bookId);
        if (bookOpt.isEmpty()) {
            view.showMessage("Book not found.");
            return;
        }

        try {
            LocalDate start = DateTimeUtil.parseDate(view.getDateInput("Enter start date"));
            LocalDate end = DateTimeUtil.parseDate(view.getDateInput("Enter end date"));
            boolean ok = reservationService.reserveBook(bookOpt.get(), currentUser.getUsername(), start, end);
            view.showMessage(ok ? "Reservation created!" : "Failed to reserve book.");
        } catch (Exception e) {
            view.showMessage("Invalid date format.");
        }
    }
}
