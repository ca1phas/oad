package view;

import model.Reservation;
import model.User;
import util.DateTimeUtil;

import java.util.List;
import java.util.Scanner;

public class ReservationsView {
    private final Scanner sc;

    public ReservationsView(Scanner sc) {
        this.sc = sc;
    }

    // ===== FR10~FR13, FR26: Main Menu =====
    public void displayMainMenu(boolean isAdmin) {
        System.out.println("\n===== Reservation Management Menu =====");
        System.out.println("1. View " + (isAdmin ? "All" : "My") + " Reservations"); // FR10~FR12
        System.out.println("2. Select a Reservation to View Details");            // FR13
        if (!isAdmin) {
            System.out.println("3. Reserve a Book");                               // FR26
        }
        System.out.println("0. Return to Main Menu");
        System.out.print("Enter your choice: ");
    }

    // ===== FR14: Reservation Details Menu =====
    public void displayDetailsMenu(Reservation r, User currentUser) {
        System.out.println("\n===== Reservation Details Menu =====");
        System.out.println("1. View Associated Book");                             // FR15

        if (currentUser.isAdmin() || r.getUsername().equalsIgnoreCase(currentUser.getUsername())) {
            System.out.println("2. Update Status");                                // FR16
            System.out.println("3. Update Start Date");                            // FR17
            System.out.println("4. Update End Date");                              // FR18
        }

        if (currentUser.isAdmin()) {
            System.out.println("5. Delete Reservation");                           // FR19
        }
        System.out.println("0. Return to Previous Menu");
        System.out.print("Enter your choice: ");
    }

    // ===== FR10~FR12: Display Reservations List =====
    public void displayReservations(List<Reservation> reservations, int page, int totalPages) {
        if (reservations.isEmpty()) {
            System.out.println("No reservations found.");
            return;
        }
        System.out.println("\n=== Reservation List (Page " + page + " of " + totalPages + ") ===");
        System.out.printf("%-5s %-15s %-25s %-15s %-20s %-15s %-15s%n",
                "ID", "Username", "Book Title", "Status", "Reservation Date", "Start Date", "End Date");
        System.out.println("-------------------------------------------------------------------------------------------------------------");
        for (Reservation res : reservations) {
            System.out.printf("%-5d %-15s %-25s %-15s %-20s %-15s %-15s%n",
                    res.getId(),
                    res.getUsername(),
                    res.getBook() != null ? res.getBook().getTitle() : "N/A",
                    res.getStatus().name(), // <-- force uppercase
                    DateTimeUtil.formatDate(res.getReservationDate().toLocalDate()),
                    res.getStartDate() != null ? DateTimeUtil.formatDate(res.getStartDate()) : "N/A",
                    res.getEndDate() != null ? DateTimeUtil.formatDate(res.getEndDate()) : "N/A"
            );
        }
    }

    // ===== FR14: Display Reservation Details =====
    public void displayReservationDetails(Reservation r) {
        System.out.println("\n===== Reservation Details =====");
        System.out.println("ID: " + r.getId());
        System.out.println("Username: " + r.getUsername());
        System.out.println("Book Title: " + (r.getBook() != null ? r.getBook().getTitle() : "N/A"));
        System.out.println("Status: " + r.getStatus().name()); // <-- force uppercase
        System.out.println("Reservation Date: " + DateTimeUtil.formatDate(r.getReservationDate().toLocalDate()));
        System.out.println("Start Date: " + (r.getStartDate() != null ? DateTimeUtil.formatDate(r.getStartDate()) : "N/A"));
        System.out.println("End Date: " + (r.getEndDate() != null ? DateTimeUtil.formatDate(r.getEndDate()) : "N/A"));
    }

    // ===== Common Methods =====
    public void showMessage(String message) {
        System.out.println(message);
    }

    public String promptString(String message) {
        System.out.print(message);
        return sc.nextLine().trim();
    }

    public int promptInt(String message) {
        while (true) {
            System.out.print(message);
            String input = sc.nextLine().trim();
            if (input.isBlank()) {
                return -1; // Controller must handle -1 as skip
            }
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    public String getDateInput(String prompt) {
        return promptString(prompt + " (yyyy-mm-dd, leave blank to skip): ");
    }

    public String getStatusInput() {
        return promptString("Enter new status (PENDING, APPROVED, DENIED, CANCELLED, ACTIVE, RETURNED): ");
    }

    public boolean askYesNo(String msg) {
        String input = promptString(msg + " (y/n): ").toLowerCase();
        return input.equals("y") || input.equals("yes");
    }
}
