package controller;

import model.Reservation;
import model.User;
import model.enums.ReservationStatus;
import service.ReservationService;
import util.DateTimeUtil;
import util.PaginationUtil;
import view.ReservationsView;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ReservationController {
    private final Scanner sc;
    private final ReservationService reservationService;
    private final ReservationsView reservationsView;

    public ReservationController(Scanner sc, ReservationService reservationService, ReservationsView reservationsView) {
        this.sc = sc;
        this.reservationService = reservationService;
        this.reservationsView = reservationsView;
    }

    // ===== FR01: 自动更新预约状态 =====
    public void updateStatusesOnStartup() {
        reservationService.updateReservationStatusesOnStartup();
    }

public void handleReservationsMenu(User currentUser) {
    while (true) {
        reservationsView.displayMainMenu(currentUser.isAdmin());
        String choice = reservationsView.promptString("");

        switch (choice) {
            case "1" -> handleViewSortFilter(currentUser);     // FR10~FR12
            case "2" -> handleSelectReservation(currentUser);  // FR13
            case "3" -> { 
                if (!currentUser.isAdmin()) {
                    createReservation(currentUser);            // FR26
                } else {
                    reservationsView.showMessage("Invalid choice for admin.");
                }
            }
            case "0" -> { return; }
            default -> reservationsView.showMessage("Invalid choice.");
        }
    }
}

       // ===== FR10 + FR11 + FR12: 查看 + 排序 + 过滤 + 分页 =====
    private void handleViewSortFilter(User currentUser) {
        String idFilter = reservationsView.promptString("Filter by ID: ");
        String usernameFilter = currentUser.isAdmin()
                ? reservationsView.promptString("Filter by username: ")
                : currentUser.getUsername();
        String bookTitleFilter = reservationsView.promptString("Filter by book title: ");
        String statusStr = reservationsView.promptString("Filter by status: ");
        ReservationStatus statusFilter = null;
        if (!statusStr.isBlank()) {
            try { statusFilter = ReservationStatus.valueOf(statusStr.toUpperCase()); }
            catch (Exception e) { reservationsView.showMessage("Invalid status filter."); }
        }
        String sortField = reservationsView.promptString("Sort field (id, username, booktitle, status, reservationdate, startdate, enddate): ");
        boolean ascending = reservationsView.askYesNo("Sort ascending?");

        int page = 1;
        int pageSize = 10;
        boolean quit = false;
        while (!quit) {
            List<Reservation> reservations = reservationService.filterSortPaginate(
                    currentUser.getUsername(), currentUser.isAdmin(),
                    idFilter, usernameFilter, bookTitleFilter, statusFilter,
                    null, null, null, null, null, null,
                    sortField, ascending, page, pageSize
            );
            int totalPages = PaginationUtil.getTotalPages(
                    reservationService.filterSortPaginate(
                            currentUser.getUsername(), currentUser.isAdmin(),
                            idFilter, usernameFilter, bookTitleFilter, statusFilter,
                            null, null, null, null, null, null,
                            sortField, ascending, 1, Integer.MAX_VALUE
                    ).size(),
                    pageSize
            );
            reservationsView.displayReservations(reservations, page, totalPages);
            String cmd = reservationsView.promptString("[2]Next | [1]Prev | [0] Quit: ");
            switch (cmd.toLowerCase()) {
                case "2" -> { if (page < totalPages) page++; }
                case "1" -> { if (page > 1) page--; }
                case "0" -> quit = true;
            }
        }
    }

    // ===== FR13: 选择预约 =====
    private void handleSelectReservation(User currentUser) {
        int id = reservationsView.promptInt("Enter Reservation ID: ");
        Optional<Reservation> rOpt = reservationService.selectReservation(id, currentUser.getUsername(), currentUser.isAdmin());
        if (rOpt.isPresent()) {
            handleReservationPage(rOpt.get(), currentUser);
        } else {
            reservationsView.showMessage("Reservation not found or no permission.");
        }
    }

    // ===== FR14~FR19: Reservation详情页 =====
    private void handleReservationPage(Reservation r, User currentUser) {
        while (true) {
            reservationsView.displayDetailsMenu(r, currentUser);
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> viewAssociatedBook(r);          // FR15
                case "2" -> updateStatus(r, currentUser);   // FR16
                case "3" -> updateStartDate(r, currentUser);// FR17
                case "4" -> updateEndDate(r, currentUser);  // FR18
                case "5" -> { if (deleteReservation(r, currentUser)) return; } // FR19
                case "0" -> { return; }
                default -> reservationsView.showMessage("Invalid choice.");
            }
        }
    }

    // ===== FR15: 查看关联书籍 =====
    private void viewAssociatedBook(Reservation r) {
        if (r.getBook() != null) {
            System.out.println("\nBook ID: " + r.getBook().getId());
            System.out.println("Title: " + r.getBook().getTitle());
            System.out.println("Author: " + r.getBook().getAuthor());
        } else {
            reservationsView.showMessage("No book linked to this reservation.");
        }
    }

    // ===== FR16: 更新状态 =====
    private void updateStatus(Reservation r, User currentUser) {
        String statusStr = reservationsView.getStatusInput();
        try {
            ReservationStatus newStatus = ReservationStatus.valueOf(statusStr.toUpperCase());
            boolean success = reservationService.updateStatus(
                    r.getId(), currentUser.getUsername(), currentUser.isAdmin(), newStatus
            );
            reservationsView.showMessage(success ? "Status updated." : "Update failed.");
        } catch (Exception e) {
            reservationsView.showMessage("Invalid status.");
        }
    }

    // ===== FR17: 更新开始日期 =====
    private void updateStartDate(Reservation r, User currentUser) {
        String dateStr = reservationsView.getDateInput("Enter new start date");
        try {
            LocalDate newStart = DateTimeUtil.parseDate(dateStr);
            boolean success = reservationService.updateStartDate(
                    r.getId(), currentUser.getUsername(), currentUser.isAdmin(), newStart
            );
            reservationsView.showMessage(success ? "Start date updated." : "Update failed.");
        } catch (Exception e) {
            reservationsView.showMessage("Invalid date.");
        }
    }

    // ===== FR18: 更新结束日期 =====
    private void updateEndDate(Reservation r, User currentUser) {
        String dateStr = reservationsView.getDateInput("Enter new end date");
        try {
            LocalDate newEnd = DateTimeUtil.parseDate(dateStr);
            boolean success = reservationService.updateEndDate(
                    r.getId(), currentUser.getUsername(), currentUser.isAdmin(), newEnd
            );
            reservationsView.showMessage(success ? "End date updated." : "Update failed.");
        } catch (Exception e) {
            reservationsView.showMessage("Invalid date.");
        }
    }

    // ===== FR19: 删除预约 =====
    private boolean deleteReservation(Reservation r, User currentUser) {
        if (!currentUser.isAdmin()) {
            reservationsView.showMessage("Only admins can delete reservations.");
            return false;
        }
        boolean confirm = reservationsView.askYesNo("Confirm delete?");
        if (!confirm) return false;
        boolean success = reservationService.deleteReservation(r.getId(), true);
        reservationsView.showMessage(success ? "Deleted." : "Delete failed.");
        return success;
    }

    // ===== FR26: 预订书籍 =====
    private void createReservation(User currentUser) {
        int bookId = reservationsView.promptInt("Enter Book ID: ");
        String startStr = reservationsView.getDateInput("Enter start date");
        String endStr = reservationsView.getDateInput("Enter end date");
        try {
            LocalDate start = DateTimeUtil.parseDate(startStr);
            LocalDate end = DateTimeUtil.parseDate(endStr);
            boolean success = reservationService.reserveBook(null, currentUser.getUsername(), start, end);
            reservationsView.showMessage(success ? "Reservation created." : "Failed to create.");
        } catch (Exception e) {
            reservationsView.showMessage("Invalid date.");
        }
    }

}

