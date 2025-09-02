package view;

import model.Reservation;
import model.User;
//import model.enums.ReservationStatus;

//import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class ReservationsView {
    private final Scanner scanner = new Scanner(System.in);

    // ------------------- Main Menu -------------------
    public void displayMainMenu(boolean isAdmin) {
        System.out.println("\n=== Reservations Menu ===");
        System.out.println("1. List Reservations");
        System.out.println("2. Select Reservation Detail");
        System.out.println("3. Reserve a Book");
        System.out.println("0. Back");
    }

    // ------------------- Paginated Table -------------------
    public void displayReservationsTable(List<Reservation> reservations, int page, int totalPages) {
        System.out.println("\n=== Reservation List (Page " + page + " of " + totalPages + ") ===");
        System.out.printf("| %-4s | %-12s | %-25s | %-10s | %-11s | %-11s | %-11s |\n",
                "ID", "Username", "Book Title", "Status", "Res. Date", "Start Date", "End Date");
        System.out.println(
                "-------------------------------------------------------------------------------------------------------------");

        for (Reservation r : reservations) {
            String title = r.getBook().getTitle();
            if (title.length() > 24)
                title = title.substring(0, 21) + "...";
            System.out.printf("| %-4d | %-12s | %-25s | %-10s | %-11s | %-11s | %-11s |\n",
                    r.getId(),
                    r.getUsername(),
                    title,
                    r.getStatus(),
                    r.getReservationDate(),
                    r.getStartDate(),
                    r.getEndDate());
        }
        System.out.println(
                "-------------------------------------------------------------------------------------------------------------");
    }

    public void displayDetailsMenu(Reservation r, User currentUser) {
        if (currentUser.isAdmin()) {
            System.out.println("--- Reservation Actions ---");
            System.out.println("1. View Book Title");
            System.out.println("2. Update Status");
            System.out.println("3. Update Start Date");
            System.out.println("4. Update End Date");
            System.out.println("5. Delete Reservation");
            System.out.println("0. Back");
        } else {
            System.out.println("--- Reservation Actions ---");
            System.out.println("1. View Book Title");
            System.out.println("2. Update Start Date");
            System.out.println("3. Update End Date");
            System.out.println("0. Back");
        }
    }

    // ------------------- Single Reservation Detail -------------------
    public void displayReservationDetails(Reservation r) {
        System.out.println("=== Reservation Detail ===");
        System.out.println("ID: " + r.getId());
        System.out.println("Username: " + r.getUsername());
        System.out.println("Book Title: " + (r.getBook() != null ? r.getBook().getTitle() : "N/A"));
        System.out.println("Status: " + r.getStatus());
        System.out.println("Reservation Date: " + r.getReservationDate());
        System.out.println("Start Date: " + r.getStartDate());
        System.out.println("End Date: " + r.getEndDate());
        System.out.println("-----------------------------------------\n");
    }

    // ------------------- Sort & Filter -------------------
    public String[] getSortAndFilterOptions() {
        scanner.nextLine(); // consume newline
        System.out.print("Sort by (id/username/status/resdate/startdate/enddate): ");
        String sortBy = scanner.nextLine();
        System.out.print("Ascending? (true/false): ");
        String ascending = scanner.nextLine();
        System.out.print("Filter by (username/status/booktitle) or leave empty: ");
        String filterBy = scanner.nextLine();
        String filterValue = "";
        if (!filterBy.isBlank()) {
            System.out.print("Filter value: ");
            filterValue = scanner.nextLine();
        }
        return new String[] { sortBy, ascending, filterBy, filterValue };
    }

    // ------------------- Prompt / Input Helpers -------------------
    public int promptInt(String message) {
        System.out.print(message);
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. " + message);
            scanner.next();
        }
        int value = scanner.nextInt();
        scanner.nextLine(); // consume newline
        return value;
    }

    public String getDateInput(String message) {
        System.out.print(message + " (YYYY-MM-DD): ");
        return scanner.nextLine();
    }

    public String getStatusInput(User user) {
        System.out.print("Enter new status (ACTIVE/CANCELLED/COMPLETED/PENDING/DENIED): ");
        return scanner.nextLine();
    }

    public void showMessage(String msg) {
        System.out.println(msg);
    }
}
