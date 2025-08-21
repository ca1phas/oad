package view;

import java.util.Scanner;

public class ReservationsView {
    private Scanner sc;

    public ReservationsView(Scanner sc) {
        this.sc = sc;
    }

    public void displayMenu(boolean isAdmin) {
        System.out.println("\n===== Reservations Menu =====");
        System.out.println("1. View My Reservations");
        System.out.println("2. Make a New Reservation");
        System.out.println("3. Cancel a Reservation");

        if (isAdmin) {
            System.out.println("4. View All Reservations");
            System.out.println("5. Approve / Deny Reservation");
            System.out.println("6. Back to Main Menu");
        } else {
            System.out.println("4. Back to Main Menu");
        }

        System.out.print("Enter your choice: ");
    }

    public void showMessage(String message) {
        System.out.println(message);
    }

    public int getReservationIdInput() {
        System.out.print("Enter Reservation ID: ");
        return Integer.parseInt(sc.nextLine().trim());
    }

    public String getBookIdInput() {
        System.out.print("Enter Book ID: ");
        return sc.nextLine().trim();
    }

    public String getStartDateInput() {
        System.out.print("Enter Start Date (yyyy-MM-dd): ");
        return sc.nextLine().trim();
    }

    public String getEndDateInput() {
        System.out.print("Enter End Date (yyyy-MM-dd): ");
        return sc.nextLine().trim();
    }

    // NEW: Ask for page number
    public int getPageNumberInput() {
        System.out.print("Enter page number: ");
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    // NEW: Ask Yes/No
    public boolean askYesNo(String msg) {
        System.out.print(msg + " (y/n): ");
        String input = sc.nextLine().trim().toLowerCase();
        return input.equals("y") || input.equals("yes");
    }
}
