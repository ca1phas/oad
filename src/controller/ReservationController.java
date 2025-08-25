package controller;

import model.Reservation;
import model.User;
import model.enums.ReservationStatus;
import service.BookService;
import service.ReservationService;
import view.ReservationsView;
import view.BookView;
import util.DateTimeUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ReservationController {
    private final Scanner sc;
    private final ReservationService reservationService;
    private final ReservationsView reservationsView;
    private final BookController bookController;

    public ReservationController(Scanner sc, ReservationService reservationService, ReservationsView reservationsView) {
        this.sc = sc;
        this.reservationService = reservationService;
        this.reservationsView = reservationsView;
        this.bookController = new BookController(new BookService(), new BookView(sc));
    }

    public void handleReservationsMenu(User currentUser) {
        while (true) {
            reservationsView.displayMainMenu(currentUser.isAdmin());
            String choice = reservationsView.promptString("").trim();
            switch (choice) {
                case "1" -> handleViewSortFilter(currentUser);
                case "2" -> handleSelectReservation(currentUser);
                case "0" -> { return; }
                default -> reservationsView.showMessage("Invalid choice.");
            }
        }
    }

    // New method to handle user-specific reservations menu
    public void handleMyReservations(User currentUser) {
        reservationsView.showMessage("--- My Reservations ---");
        handleViewSortFilter(currentUser);
    }

    private void handleViewSortFilter(User currentUser) {
        String idFilter = reservationsView.promptString("Enter reservation ID to filter by (leave blank to skip): ");
        String usernameFilter = currentUser.isAdmin() ? reservationsView.promptString("Enter username to filter by (leave blank to skip): ") : currentUser.getUsername();
        String bookTitleFilter = reservationsView.promptString("Enter book title to filter by (leave blank to skip): ");
        String statusStr = reservationsView.promptString("Enter status to filter by (leave blank to skip): ");
        ReservationStatus statusFilter = null;
        if (!statusStr.isBlank()) {
            try {
                statusFilter = ReservationStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                reservationsView.showMessage("Invalid status, skipping this filter.");
            }
        }
        
        LocalDate resStart = parseDateSafe(reservationsView.getDateInput("Enter reservation start date range"));
        LocalDate resEnd = parseDateSafe(reservationsView.getDateInput("Enter reservation end date range"));
        LocalDate startStart = parseDateSafe(reservationsView.getDateInput("Enter checkout start date range"));
        LocalDate startEnd = parseDateSafe(reservationsView.getDateInput("Enter checkout end date range"));
        LocalDate endStart = parseDateSafe(reservationsView.getDateInput("Enter checkout end date range"));
        LocalDate endEnd = parseDateSafe(reservationsView.getDateInput("Enter checkout end date range"));

        String sortField = reservationsView.promptString("Enter sort field (leave blank to skip): ");
        boolean ascending = reservationsView.askYesNo("Sort in ascending order?");

        int page = 1;
        int pageSize = 10;

        while (true) {
            List<Reservation> reservations = reservationService.filterSortPaginate(
                currentUser.getUsername(), currentUser.isAdmin(),
                idFilter, usernameFilter, bookTitleFilter, statusFilter,
                resStart, resEnd,
                startStart, startEnd,
                endStart, endEnd,
                sortField, ascending,
                page, pageSize
            );

            reservationsView.displayReservations(reservations);
            if (reservations.isEmpty()) {
                reservationsView.showMessage("No matching reservations found on this page.");
            }
            
            String cmd = reservationsView.promptString("[n] Next page | [p] Previous page | [r] Reset filters/sort | [q] Quit: ");
            if ("n".equalsIgnoreCase(cmd)) {
                page++;
            } else if ("p".equalsIgnoreCase(cmd) && page > 1) {
                page--;
            } else if ("r".equalsIgnoreCase(cmd)) {
                handleViewSortFilter(currentUser);
                return;
            } else if ("q".equalsIgnoreCase(cmd)) {
                break;
            }
        }
    }

    private void handleSelectReservation(User currentUser) {
        int id = reservationsView.promptInt("Enter Reservation ID: ");
        if (id == -1) {
            reservationsView.showMessage("Operation cancelled.");
            return;
        }

        Optional<Reservation> rOpt = reservationService.selectReservation(id, currentUser.getUsername(), currentUser.isAdmin());

        if (rOpt.isPresent()) {
            handleReservationPage(rOpt.get(), currentUser);
        } else {
            reservationsView.showMessage("Reservation not found or you don't have permission to view it.");
        }
    }
    
    // Displays details and sub-menu for a specific reservation
    private void handleReservationPage(Reservation r, User currentUser) {
        while (true) {
            reservationsView.displayDetailsMenu(r, currentUser); 
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> {
                    if (r.getBook() != null) {
                        bookController.handleBookMenu(r.getBook().getId(), currentUser);
                    } else {
                        reservationsView.showMessage("No book associated with this reservation.");
                    }
                }
                case "2" -> updateStatus(r, currentUser);
                case "3" -> updateStartDate(r, currentUser);
                case "4" -> updateEndDate(r, currentUser);
                case "5" -> {
                    if (deleteReservation(r, currentUser)) {
                        return;
                    }
                }
                case "0" -> { return; }
                default -> reservationsView.showMessage("Invalid choice.");
            }
        }
    }

    // Handles updating the reservation status
    private void updateStatus(Reservation r, User currentUser) {
        String statusStr = reservationsView.getStatusInput();
        try {
            ReservationStatus newStatus = ReservationStatus.valueOf(statusStr.toUpperCase());
            boolean success = reservationService.updateStatus(r.getId(), currentUser.getUsername(), currentUser.isAdmin(), newStatus);
            reservationsView.showMessage(success ? "Status updated successfully." : "Update failed. Check permissions or status transitions.");
        } catch (IllegalArgumentException e) {
            reservationsView.showMessage("Invalid status.");
        }
    }

    // Handles updating the reservation start date
    private void updateStartDate(Reservation r, User currentUser) {
        String dateStr = reservationsView.getDateInput("Enter new start date");
        if (dateStr.isBlank()) {
            reservationsView.showMessage("Operation cancelled.");
            return;
        }
        try {
            LocalDate newDate = DateTimeUtil.parseDate(dateStr);
            boolean success = reservationService.updateStartDate(r.getId(), currentUser.getUsername(), currentUser.isAdmin(), newDate);
            reservationsView.showMessage(success ? "Start date updated successfully." : "Update failed. Check reservation status or date validity.");
        } catch (Exception e) {
            reservationsView.showMessage("Invalid date format.");
        }
    }

    // Handles updating the reservation end date
    private void updateEndDate(Reservation r, User currentUser) {
        String dateStr = reservationsView.getDateInput("Enter new end date");
        if (dateStr.isBlank()) {
            reservationsView.showMessage("Operation cancelled.");
            return;
        }
        try {
            LocalDate newDate = DateTimeUtil.parseDate(dateStr);
            boolean success = reservationService.updateEndDate(r.getId(), currentUser.getUsername(), currentUser.isAdmin(), newDate);
            reservationsView.showMessage(success ? "End date updated successfully." : "Update failed. Check reservation status or date validity.");
        } catch (Exception e) {
            reservationsView.showMessage("Invalid date format.");
        }
    }
    
    private boolean deleteReservation(Reservation r, User currentUser) {
        if (!currentUser.isAdmin()) {
            reservationsView.showMessage("Permission denied. Only an admin can delete a reservation.");
            return false;
        }
        if (reservationsView.askYesNo("Are you sure you want to delete this reservation?")) {
            boolean success = reservationService.deleteReservation(r.getId(), currentUser.isAdmin());
            reservationsView.showMessage(success ? "Reservation deleted." : "Deletion failed.");
            return success;
        }
        return false;
    }
    
    // Helper method to safely parse dates, avoiding crashes on bad input
    private LocalDate parseDateSafe(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return DateTimeUtil.parseDate(dateStr);
        } catch (Exception e) {
            reservationsView.showMessage("Invalid date format, skipping filter.");
            return null;
        }
    }
}