package controller;

import model.Reservation;
import model.User;
import model.enums.ReservationStatus;
import service.ReservationService;
import service.BookService;
import view.ReservationsView;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ReservationController {
    private final ReservationService reservationService;
    private final BookService bookService;
    private final ReservationsView view;

    public ReservationController(ReservationService reservationService, BookService bookService, ReservationsView view) {
        this.reservationService = reservationService;
        this.bookService = bookService;
        this.view = view;

        // FR01: update statuses on startup
        this.reservationService.updateReservationStatusesOnStartup();
    }

    // Main menu
    public void handleReservationsMenu(User currentUser) {
        boolean isAdmin = currentUser.isAdmin();
        boolean running = true;

        while (running) {
            view.displayMainMenu(isAdmin);
            int choice = view.promptInt("Your choice: ");

            switch (choice) {
                case 1:
                    List<Reservation> reservations = reservationService.filterSortPaginate(
                            currentUser.getUsername(), isAdmin,
                            "id", null, null, null, null, null, null, null, null, null, null, true, 1, 10);
                    view.displayReservations(reservations, 1, 1);
                    break;
                case 2:
                    int resId = view.promptInt("Enter reservation ID: ");
                    Optional<Reservation> opt = reservationService.selectReservation(resId, currentUser.getUsername(), isAdmin);
                    if (opt.isPresent()) handleReservationDetails(opt.get(), currentUser);
                    else view.showMessage("Reservation not found or not accessible.");
                    break;
                case 3:
                    if (!isAdmin) {
                        int bookId = view.promptInt("Enter book ID to reserve: ");
                        var bookOpt = bookService.viewBook(bookId);
                        if (bookOpt.isEmpty()) view.showMessage("Book not found.");
                        else {
                            try {
                                LocalDate start = LocalDate.parse(view.getDateInput("Enter start date"));
                                LocalDate end = LocalDate.parse(view.getDateInput("Enter end date"));
                                boolean ok = reservationService.reserveBook(bookOpt.get(), currentUser.getUsername(), start, end);
                                view.showMessage(ok ? "Reservation created!" : "Failed to reserve book.");
                            } catch (Exception e) {
                                view.showMessage("Invalid date format.");
                            }
                        }
                    }
                    break;
                case 0: running = false; break;
                default: view.showMessage("Invalid choice.");
            }
        }
    }

    // Reservation details menu
    private void handleReservationDetails(Reservation r, User currentUser) {
        boolean isAdmin = currentUser.isAdmin();
        boolean running = true;

        while (running) {
            view.displayReservationDetails(r);
            view.displayDetailsMenu(r, currentUser);
            int choice = view.promptInt("Your choice: ");

            switch (choice) {
                case 1:
                    System.out.println("Associated Book: " + (r.getBook() != null ? r.getBook().getTitle() : "N/A"));
                    break;
                case 2:
                    try {
                        ReservationStatus newStatus = ReservationStatus.valueOf(view.getStatusInput(currentUser).toUpperCase());
                        boolean ok = reservationService.updateStatus(r.getId(), currentUser.getUsername(), isAdmin, newStatus);
                        view.showMessage(ok ? "Status updated." : "Failed to update status.");
                    } catch (IllegalArgumentException e) { view.showMessage("Invalid status."); }
                    break;
                case 3:
                    try {
                        LocalDate start = LocalDate.parse(view.getDateInput("Enter new start date"));
                        boolean ok = reservationService.updateStartDate(r.getId(), currentUser.getUsername(), isAdmin, start);
                        view.showMessage(ok ? "Start date updated." : "Failed to update start date.");
                    } catch (Exception e) { view.showMessage("Invalid date format."); }
                    break;
                case 4:
                    try {
                        LocalDate end = LocalDate.parse(view.getDateInput("Enter new end date"));
                        boolean ok = reservationService.updateEndDate(r.getId(), currentUser.getUsername(), isAdmin, end);
                        view.showMessage(ok ? "End date updated." : "Failed to update end date.");
                    } catch (Exception e) { view.showMessage("Invalid date format."); }
                    break;
                case 5:
                    if (isAdmin) {
                        boolean ok = reservationService.deleteReservation(r.getId(), true);
                        view.showMessage(ok ? "Reservation deleted." : "Failed to delete reservation.");
                        running = false;
                    }
                    break;
                case 0: running = false; break;
                default: view.showMessage("Invalid choice.");
            }
        }
    }
}
