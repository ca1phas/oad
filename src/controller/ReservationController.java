package controller;

import model.Reservation;
import model.User;
import model.enums.ReservationStatus;
import service.ReservationService;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class ReservationController {
    private final ReservationService reservationService;
    private final Scanner scanner;
    private final User currentUser;

    public ReservationController(ReservationService reservationService, User currentUser) {
        this.reservationService = reservationService;
        this.scanner = new Scanner(System.in);
        this.currentUser = currentUser;
    }

    // main menu
    public void handleReservationsMenu() {
        while (true) {
            System.out.println("\n=== Reservations Menu ===");
            System.out.println("1. View Reservations (with pagination)");
            System.out.println("2. Sort Reservations");
            System.out.println("3. Filter Reservations");
            System.out.println("4. Select Reservation (enter ID)");
            System.out.println("0. Back to Main Menu");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewReservationsWithPagination();
                case "2" -> sortReservations();
                case "3" -> filterReservations();
                case "4" -> selectReservation();
                case "0" -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // FR10 pagination
    private void viewReservationsWithPagination() {
        int pageSize = 5;
        int page = 1;

        while (true) {
            List<Reservation> reservations = reservationService.getReservationsByPage(page, pageSize);
            if (reservations.isEmpty()) {
                System.out.println("No reservations on this page.");
            } else {
                System.out.println("\n--- Reservations Page " + page + " ---");
                reservations.forEach(System.out::println);
            }

            System.out.println("\n[n] Next page | [p] Previous page | [q] Quit");
            String cmd = scanner.nextLine().trim().toLowerCase();
            if ("n".equals(cmd)) page++;
            else if ("p".equals(cmd) && page > 1) page--;
            else if ("q".equals(cmd)) break;
        }
    }

    // FR11 sort
    private void sortReservations() {
        System.out.print("Enter field to sort by (id, book, username, date, status): ");
        String field = scanner.nextLine().trim().toLowerCase();
        System.out.print("Enter order (asc/desc): ");
        String order = scanner.nextLine().trim().toLowerCase();

        List<Reservation> sorted = reservationService.sortReservations(field, order);
        sorted.forEach(System.out::println);
    }

    // FR12 filter
    private void filterReservations() {
        System.out.print("Filter by field (id, book, username, status, date): ");
        String field = scanner.nextLine().trim().toLowerCase();
        System.out.print("Enter value: ");
        String value = scanner.nextLine().trim();

        List<Reservation> filtered = reservationService.filterReservations(field, value);
        filtered.forEach(System.out::println);
    }

    // FR13  Reservation
    private void selectReservation() {
        try {
            System.out.print("Enter Reservation ID: ");
            int id = Integer.parseInt(scanner.nextLine().trim());
            Reservation r = reservationService.findById(id).orElse(null);

            if (r == null) {
                System.out.println("Reservation not found.");
                return;
            }

            handleReservationPage(r);
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
        }
    }

    // === Reservation Page menu (FR14–FR19) ===
    private void handleReservationPage(Reservation r) {
        while (true) {
            System.out.println("\n=== Reservation Page ===");
            System.out.println(r);
            System.out.println("1. View Book");
            System.out.println("2. Update Status");
            System.out.println("3. Update Start Date");
            System.out.println("4. Update End Date");
            System.out.println("5. Delete Reservation");
            System.out.println("0. Back");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewBook(r);
                case "2" -> updateStatus(r);
                case "3" -> updateStartDate(r);
                case "4" -> updateEndDate(r);
                case "5" -> deleteReservation(r);
                case "0" -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // FR15 view book 
    private void viewBook(Reservation r) {
        System.out.println("Book details: " + r.getBook());
    }

    // FR16 update status 
    private void updateStatus(Reservation r) {
        System.out.print("Enter new status: ");
        String statusStr = scanner.nextLine().trim().toUpperCase();

        try {
            ReservationStatus status = ReservationStatus.valueOf(statusStr);
            boolean success = reservationService.updateStatus(r.getId(), status, currentUser);
            System.out.println(success ? "Status updated." : "Failed to update status.");
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid status.");
        }
    }

    // FR17 update start date
    private void updateStartDate(Reservation r) {
        if (r.getStatus() != ReservationStatus.PENDING) {
            System.out.println("Only PENDING reservations can update start date.");
            return;
        }
        System.out.print("Enter new start date (yyyy-mm-dd): ");
        String dateStr = scanner.nextLine().trim();
        try {
            LocalDate newDate = LocalDate.parse(dateStr);
            boolean success = reservationService.updateStartDate(r.getId(), newDate, currentUser);
            System.out.println(success ? "Start date updated." : "Failed to update.");
        } catch (Exception e) {
            System.out.println("Invalid date.");
        }
    }

    // FR18 update end date
    private void updateEndDate(Reservation r) {
        if (r.getStatus() != ReservationStatus.PENDING) {
            System.out.println("Only PENDING reservations can update end date.");
            return;
        }
        System.out.print("Enter new end date (yyyy-mm-dd): ");
        String dateStr = scanner.nextLine().trim();
        try {
            LocalDate newDate = LocalDate.parse(dateStr);
            boolean success = reservationService.updateEndDate(r.getId(), newDate, currentUser);
            System.out.println(success ? "End date updated." : "Failed to update.");
        } catch (Exception e) {
            System.out.println("Invalid date.");
        }
    }

    // FR19 delete reservation
    private void deleteReservation(Reservation r) {
        if (!currentUser.isAdmin()) {
            System.out.println("Access denied. Only admin can delete reservations.");
            return;
        }
        System.out.print("Are you sure? (yes/no): ");
        if ("yes".equalsIgnoreCase(scanner.nextLine().trim())) {
            boolean success = reservationService.deleteReservation(r.getId());
            System.out.println(success ? "Reservation deleted." : "Failed to delete.");
        }
    }
}
