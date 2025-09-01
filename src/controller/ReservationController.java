package controller;

import model.Book;
import model.Reservation;
import model.User;
import model.enums.ReservationStatus;
import service.BookService;
import service.ReservationService;
import view.ReservationsView;
import view.BookView;
import util.PaginationUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ReservationController {
    private final ReservationService reservationService;
    private final BookService bookService;
    private final ReservationsView view;
    private final BookView bookView;  

    public ReservationController(ReservationService reservationService,BookService bookService,ReservationsView view,BookView bookView) {   
        this.reservationService = reservationService;
        this.bookService = bookService;
        this.view = view;
        this.bookView = bookView;

        this.reservationService.updateReservationStatusesOnStartup();
    }

    // Main Reservations Menu
    public void handleReservationsMenu(User currentUser) {
        boolean isAdmin = currentUser.isAdmin();
        boolean running = true;

        while (running) {
            view.displayMainMenu(isAdmin);
            int choice = view.promptInt("Your choice: ");

            switch (choice) {
            case 1: // List reservations
             handlePaginatedReservations(currentUser, isAdmin);
            break;

            case 2: // Select reservation details
                    int resId = view.promptInt("Enter reservation ID: ");
                    Optional<Reservation> opt = reservationService.selectReservation(resId, currentUser.getUsername(), isAdmin);
            if (opt.isPresent()) handleReservationDetails(opt.get(), currentUser);
                else view.showMessage("Reservation not found or not accessible.");
            break;

            case 3: 
            handleReserveBook(currentUser);
            break;

    case 0:
        running = false;
        break;

    default:
        view.showMessage("Invalid choice.");
}

        }
    }

    // ------------------- Pagination, Sort & Filter -------------------
    private void handlePaginatedReservations(User currentUser, boolean isAdmin) {
        int page = 1;
        int pageSize = 5;

        // Initial fetch: get all reservations
        List<Reservation> allReservations = reservationService.filterSortPaginate(
                currentUser.getUsername(),
                isAdmin,
                null, null, null, null, null, null, null, null, null, null, null,
                true,
                1,
                Integer.MAX_VALUE
        );

        List<Reservation> currentList = new ArrayList<>(allReservations);
        int totalPages = PaginationUtil.getTotalPages(currentList.size(), pageSize);

        String sortBy = "id";
        boolean ascending = true;
        String filterBy = null;
        String filterValue = null;

        boolean running = true;
        while (running) {
            List<Reservation> paginated = PaginationUtil.paginate(currentList, page, pageSize);
            view.displayReservationsTable(paginated, page, totalPages);

            view.showMessage("[1] Previous | [2] Next | [3] Sort & Filter | [4] Clear Filters | [0] Back");
            int choice = view.promptInt("Your choice: ");

            switch (choice) {
                case 1:
                    if (page > 1) page--;
                    else view.showMessage("Already at first page.");
                    break;

                case 2:
                    if (page < totalPages) page++;
                    else view.showMessage("Already at last page.");
                    break;

                case 3:
                    // Get sort & filter options
                    String[] sf = view.getSortAndFilterOptions();
                    sortBy = sf[0];
                    ascending = Boolean.parseBoolean(sf[1]);
                    filterBy = sf[2];
                    filterValue = sf[3];

                    // Apply sort & filter
                    currentList = applySortAndFilter(allReservations, sortBy, ascending, filterBy, filterValue);
                    page = 1;
                    totalPages = PaginationUtil.getTotalPages(currentList.size(), pageSize);
                    break;

                case 4: // Clear filters
                    currentList = new ArrayList<>(allReservations);
                    page = 1;
                    totalPages = PaginationUtil.getTotalPages(currentList.size(), pageSize);
                    break;

                case 0:
                    running = false;
                    break;

                default:
                    view.showMessage("Invalid choice.");
            }
        }
    }

    private List<Reservation> applySortAndFilter(List<Reservation> list, String sortBy, boolean ascending,
                                                  String filterBy, String filterValue) {
        List<Reservation> filtered = new ArrayList<>(list);

        // Filter
        if (filterBy != null && filterValue != null && !filterValue.isBlank()) {
            switch (filterBy.toLowerCase()) {
                case "username" -> filtered.removeIf(r -> !r.getUsername().toLowerCase().contains(filterValue.toLowerCase()));
                case "status" -> filtered.removeIf(r -> !r.getStatus().name().equalsIgnoreCase(filterValue));
                case "booktitle" -> filtered.removeIf(r -> !r.getBook().getTitle().toLowerCase().contains(filterValue.toLowerCase()));
            }
        }

        // Sort
        Comparator<Reservation> comparator = switch (sortBy.toLowerCase()) {
            case "username" -> Comparator.comparing(Reservation::getUsername);
            case "status" -> Comparator.comparing(Reservation::getStatus);
            case "resdate" -> Comparator.comparing(Reservation::getReservationDate);
            case "startdate" -> Comparator.comparing(Reservation::getStartDate);
            case "enddate" -> Comparator.comparing(Reservation::getEndDate);
            default -> Comparator.comparing(Reservation::getId);
        };
        if (!ascending) comparator = comparator.reversed();
        filtered.sort(comparator);

        return filtered;
    }

// ------------------- Reservation Details -------------------
private void handleReservationDetails(Reservation r, User currentUser) {
    boolean isAdmin = currentUser.isAdmin();
    boolean running = true;

    while (running) {

        // Show current reservation details
        view.displayReservationDetails(r);

        view.displayDetailsMenu(r, currentUser);
        int choice = view.promptInt("Your choice: ");

        switch (choice) {
        case 1: // View linked Book Details
            if (r.getBook() != null) {
                int bookId = r.getBook().getId();
                Optional<Book> bookOpt = bookService.viewBook(bookId);
            if (bookOpt.isPresent()) {
                bookView.showBookDetails(bookOpt.get());
                System.out.println("\nPress Enter to go back...");
                new Scanner(System.in).nextLine(); // <- 等待用户按 Enter
            } else {
                view.showMessage("Book not found.");
            }
            } else {
            view.showMessage("No book linked to this reservation.");
            }
            break;

            case 2: 
                if (isAdmin) {
                    // Admin: update status
                    try {
                        ReservationStatus newStatus =
                                ReservationStatus.valueOf(view.getStatusInput(currentUser).toUpperCase());
                        boolean ok = reservationService.updateStatus(
                                r.getId(), currentUser.getUsername(), true, newStatus
                        );
                        view.showMessage(ok ? "Status updated!" : "Failed.");
                        if (ok) {
                            r = reservationService.selectReservation(r.getId(),
                                    currentUser.getUsername(), true).orElse(r);
                        }
                    } catch (IllegalArgumentException e) {
                        view.showMessage("Invalid status.");
                    }
                } else {
                    // User: update start date
                    try {
                        LocalDate start = LocalDate.parse(view.getDateInput("Enter new start date"));
                        boolean ok = reservationService.updateStartDate(
                                r.getId(), currentUser.getUsername(), false, start
                        );
                        view.showMessage(ok ? "Start date updated!" : "Failed.");
                        if (ok) {
                            r = reservationService.selectReservation(r.getId(),
                                    currentUser.getUsername(), false).orElse(r);
                        }
                    } catch (Exception e) {
                        view.showMessage("Invalid date format.");
                    }
                }
                break;

            case 3:
                if (isAdmin) {
                    // Admin: update start date
                    try {
                        LocalDate start = LocalDate.parse(view.getDateInput("Enter new start date"));
                        boolean ok = reservationService.updateStartDate(
                                r.getId(), currentUser.getUsername(), true, start
                        );
                        view.showMessage(ok ? "Start date updated!" : "Failed.");
                        if (ok) {
                            r = reservationService.selectReservation(r.getId(),
                                    currentUser.getUsername(), true).orElse(r);
                        }
                    } catch (Exception e) {
                        view.showMessage("Invalid date format.");
                    }
                } else {
                    // User: update end date
                    try {
                        LocalDate end = LocalDate.parse(view.getDateInput("Enter new end date"));
                        boolean ok = reservationService.updateEndDate(
                                r.getId(), currentUser.getUsername(), false, end
                        );
                        view.showMessage(ok ? "End date updated!" : "Failed.");
                        if (ok) {
                            r = reservationService.selectReservation(r.getId(),
                                    currentUser.getUsername(), false).orElse(r);
                        }
                    } catch (Exception e) {
                        view.showMessage("Invalid date format.");
                    }
                }
                break;

            case 4: 
                if (isAdmin) {
                    // Admin: update end date
                    try {
                        LocalDate end = LocalDate.parse(view.getDateInput("Enter new end date"));
                        boolean ok = reservationService.updateEndDate(
                                r.getId(), currentUser.getUsername(), true, end
                        );
                        view.showMessage(ok ? "End date updated!" : "Failed.");
                        if (ok) {
                            r = reservationService.selectReservation(r.getId(),
                                    currentUser.getUsername(), true).orElse(r);
                        }
                    } catch (Exception e) {
                        view.showMessage("Invalid date format.");
                    }
                } else {
                    view.showMessage("Invalid choice.");
                }
                break;

            case 5:
                if (isAdmin) {
                    // Admin: delete reservation
                    boolean ok = reservationService.deleteReservation(r.getId(), true);
                    view.showMessage(ok ? "Deleted successfully!" : "Failed.");
                    if (ok) running = false;
                } else {
                    view.showMessage("Invalid choice.");
                }
                break;

            case 0:
                // Back to previous menu
                running = false;
                break;

            default:
                view.showMessage("Invalid choice.");
        }
    }
}



// ------------------- Reserve Book -------------------
    private void handleReserveBook(User currentUser) {
        int bookId = view.promptInt("Enter Book ID to reserve: ");
        Optional<Book> bookOpt = bookService.viewBook(bookId);
        if (bookOpt.isEmpty()) {
            view.showMessage("Book not found.");
            return;
        }

        try {
            LocalDate start = LocalDate.parse(view.getDateInput("Enter start date (YYYY-MM-DD)"));
            LocalDate end = LocalDate.parse(view.getDateInput("Enter end date (YYYY-MM-DD)"));
            boolean ok = reservationService.reserveBook(bookOpt.get(), currentUser.getUsername(), start, end);
            view.showMessage(ok ? "Reservation created!" : "Failed to reserve book.");
        } catch (Exception e) {
            view.showMessage("Invalid date format.");
        }
    }
}
