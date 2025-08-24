package view;

import java.util.List;
import java.util.Scanner;
import model.Reservation;
import util.DateTimeUtil; 
public class ReservationsView {
    private final Scanner sc;

    public ReservationsView(Scanner sc) {
        this.sc = sc;
    }

    public void displayMainMenu(boolean isAdmin) {
        System.out.println("\n===== Reservation Management Menu =====");
        if (isAdmin) {
            System.out.println("1. View All Reservations (Paginated, Sorted, Filtered)");
            System.out.println("2. Select a Reservation to View Details");
        } else {
            System.out.println("1. View My Reservations (Paginated, Sorted, Filtered)");
            System.out.println("2. Select One of My Reservations to View Details");
        }
        System.out.println("0. Return to Main Menu");
        System.out.print("Enter your choice: ");
    }
    
    public void displayDetailsMenu(boolean canUpdate, boolean canDelete) {
        System.out.println("\n===== Reservation Details Menu =====");
        System.out.println("1. View Associated Book");
        if (canUpdate) {
            System.out.println("2. Update Status");
            System.out.println("3. Update Start Date");
            System.out.println("4. Update End Date");
        }
        if (canDelete) {
            System.out.println("5. Delete Reservation");
        }
        System.out.println("0. Return to Previous Menu");
        System.out.print("Enter your choice: ");
    }
    
    // Displays the reservation list in a formatted table
    public void displayReservations(List<Reservation> reservations) {
        if (reservations.isEmpty()) {
            System.out.println("No reservations found.");
            return;
        }

        System.out.println("\n=== Reservation List ===");
        System.out.printf("%-5s %-15s %-25s %-15s %-15s %-15s %-15s\n", 
            "ID", "Username", "Book Title", "Status", "Reservation Date", "Start Date", "End Date");
        System.out.println("------------------------------------------------------------------------------------------------------");

        for (Reservation res : reservations) {
            System.out.printf("%-5d %-15s %-25s %-15s %-15s %-15s %-15s\n",
                res.getId(),
                res.getUsername(),
                res.getBook() != null ? res.getBook().getTitle() : "N/A", // Use ternary operator for null safety
                res.getStatus(),
                DateTimeUtil.formatDate(res.getReservationDate().toLocalDate()), // Convert LocalDateTime to LocalDate before formatting
                res.getStartDate() != null ? DateTimeUtil.formatDate(res.getStartDate()) : "N/A",
                res.getEndDate() != null ? DateTimeUtil.formatDate(res.getEndDate()) : "N/A"
            );
        }
    }

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
            try {
                String input = sc.nextLine().trim();
                if (input.isBlank()) {
                    return -1; // Indicate no input
                }
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