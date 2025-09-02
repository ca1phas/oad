package view;

import model.Reservation;
import model.User;

import java.util.List;
import java.util.Scanner;

public class ReservationsView {
    private final Scanner scanner = new Scanner(System.in);

    // ------------------- Main Menu -------------------
    public void displayMainMenu() {
        System.out.println("\n=== Reservations Menu ===");
        System.out.println("1. View All Reservations");
        System.out.println("2. Sort & Filter Reservations");
        System.out.println("3. Select Reservation Detail");
        System.out.println("4. Reserve a Book");
        System.out.println("0. Back");
    }

    // ------------------- Paginated Table -------------------
    public void displayReservationsTable(List<Reservation> reservations, int page, int totalPages) {
        if (reservations.isEmpty()) {
            System.out.println("\nNo reservations found.");
            return;
        }

        System.out.println("\n           = RESERVATIONS - PAGE " + page + " OF " + totalPages + " =");
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.printf("| %-4s | %-12s | %-25s | %-10s | %-11s | %-11s | %-11s |\n",
                "ID", "Username", "Book Title", "Status", "Res. Date", "Start Date", "End Date");
        System.out.println("-----------------------------------------------------------------------------------------");

        for (Reservation r : reservations) {
            String title = r.getBook() != null ? r.getBook().getTitle() : "N/A";
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

        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.println("[1] Previous | [2] Next | [0] Back");
    }

    // ------------------- Single Reservation Details -------------------
    public void displayReservationDetails(Reservation r) {
        System.out.println("\n=== Reservation Detail ===");
        System.out.println("ID: " + r.getId());
        System.out.println("Username: " + r.getUsername());
        System.out.println("Book Title: " + (r.getBook() != null ? r.getBook().getTitle() : "N/A"));
        System.out.println("Status: " + r.getStatus());
        System.out.println("Reservation Date: " + r.getReservationDate());
        System.out.println("Start Date: " + r.getStartDate());
        System.out.println("End Date: " + r.getEndDate());
        System.out.println("-----------------------------------------");
    }

    // ------------------- Reservation Action Menu -------------------
    public void displayDetailsMenu(Reservation r, User currentUser) {
        if (currentUser.isAdmin()) {
            System.out.println("--- Reservation Actions (Admin) ---");
            System.out.println("1. View Book Details");
            System.out.println("2. Update Status");
            System.out.println("3. Update Start Date");
            System.out.println("4. Update End Date");
            System.out.println("5. Delete Reservation");
            System.out.println("0. Back");
        } else {
            System.out.println("--- Reservation Actions (User) ---");
            System.out.println("1. View Book Details");
            System.out.println("2. Update Start Date");
            System.out.println("3. Update End Date");
            System.out.println("0. Back");
        }
    }

    // ------------------- Sort & Filter Input -------------------
    public SortFilterOptions getSortAndFilterOptions() {
        System.out.println("\n--- Enter Sort & Filter Options ---");

        System.out.print("Filter by ID (exact match or leave blank): ");
        String idFilter = scanner.nextLine().trim();

        System.out.print("Filter by Username (partial match or leave blank): ");
        String usernameFilter = scanner.nextLine().trim();

        System.out.print("Filter by Book Title (partial match or leave blank): ");
        String bookTitleFilter = scanner.nextLine().trim();

        System.out.print("Sort by (id, username, booktitle, status, resdate, startdate, enddate): ");
        String sortBy = scanner.nextLine().trim().toLowerCase();
        if (!sortBy.matches("id|username|booktitle|status|resdate|startdate|enddate")) {
            System.out.println("Invalid sort field. Using 'id' as default.");
            sortBy = "id";
        }

        System.out.print("Sort descending? (y/n): ");
        String desc = scanner.nextLine().trim();
        boolean ascending = !desc.equalsIgnoreCase("y");

        return new SortFilterOptions(sortBy, ascending, idFilter, usernameFilter, bookTitleFilter);
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

    // ------------------- Helper Class -------------------
    public static class SortFilterOptions {
        public final String sortBy;
        public final boolean ascending;
        public final String idFilter;
        public final String usernameFilter;
        public final String bookTitleFilter;

        public SortFilterOptions(String sortBy, boolean ascending,
                                 String idFilter, String usernameFilter, String bookTitleFilter) {
            this.sortBy = sortBy;
            this.ascending = ascending;
            this.idFilter = idFilter;
            this.usernameFilter = usernameFilter;
            this.bookTitleFilter = bookTitleFilter;
        }
    }
}
