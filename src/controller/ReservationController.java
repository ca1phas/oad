package controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

import model.Reservation;
import model.Book;
import model.User;
import model.enums.ReservationStatus;
import service.ReservationService;
import service.BookService;
import view.ReservationsView;

public class ReservationController {
    private Scanner sc;
    private ReservationService reservationService;
    private BookService bookService;
    private ReservationsView reservationsView;

    public ReservationController(Scanner sc) {
        this.sc = sc;
        this.reservationService = new ReservationService();
        this.bookService = new BookService();
        this.reservationsView = new ReservationsView(sc);
    }

    // Main handler for reservation menu
    public void handleReservationMenu(User currentUser) {
        boolean back = false;

        while (!back) {
            reservationsView.displayMenu(currentUser.isAdmin());
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1": // User: View own reservations
                    viewUserReservations(currentUser);
                    break;
                case "2": // User: Make new reservation
                    makeReservation(currentUser);
                    break;
                case "3": // User: Cancel own reservation
                    cancelReservation(currentUser);
                    break;
                case "4":
                    if (currentUser.isAdmin()) {
                        viewAllReservations(); // Admin only
                    } else {
                        back = true; // Go back to main menu
                    }
                    break;
                case "5":
                    if (currentUser.isAdmin()) {
                        approveOrDenyReservation(); // Admin only
                    } else {
                        reservationsView.showMessage("Invalid option.");
                    }
                    break;
                case "6":
                    if (currentUser.isAdmin()) {
                        back = true; // Back to main menu
                    } else {
                        reservationsView.showMessage("Invalid option.");
                    }
                    break;
                default:
                    reservationsView.showMessage("Invalid choice. Please try again.");
            }
        }
    }

    // ================= USER FEATURES =================

    // Show reservations of the current user
    private void viewUserReservations(User currentUser) {
        List<Reservation> reservations = reservationService.getReservationsByUsername(currentUser.getUsername());
        if (reservations.isEmpty()) {
            reservationsView.showMessage("You have no reservations.");
        } else {
            reservations.forEach(r -> reservationsView.showMessage(r.toString()));
        }
    }

    // Allow user to make a new reservation
    private void makeReservation(User currentUser) {
        String bookId = reservationsView.getBookIdInput();
        Book book = bookService.findBookById(bookId);  // bookservice problem will be fixed later
            reservationsView.showMessage("Book not found.");
            return;
        }

        String start = reservationsView.getStartDateInput();
        String end = reservationsView.getEndDateInput();
        try {
            LocalDate startDate = LocalDate.parse(start);
            LocalDate endDate = LocalDate.parse(end);

            Reservation newReservation = new Reservation(
                    reservationService.generateNewId(), // Generate unique ID
                    book,
                    currentUser.getUsername(),
                    LocalDateTime.now(),
                    ReservationStatus.PENDING, // Default status
                    startDate,
                    endDate
            );

            reservationService.addReservation(newReservation);
            reservationsView.showMessage("Reservation created successfully! Status: PENDING.");
        } catch (Exception e) {
            reservationsView.showMessage("Invalid date format. Please use yyyy-MM-dd.");
        }
    }

    // Cancel an existing reservation (user can only cancel their own)
    private void cancelReservation(User currentUser) {
        int id = reservationsView.getReservationIdInput();
        Reservation res = reservationService.findReservationById(id);
        if (res == null || !res.getUsername().equals(currentUser.getUsername())) {
            reservationsView.showMessage("Reservation not found or not yours.");
            return;
        }
        res.setStatus(ReservationStatus.CANCELLED);
        reservationsView.showMessage("Reservation " + id + " cancelled.");
    }

    // ================= ADMIN FEATURES =================

    // Show all reservations in the system
    private void viewAllReservations() {
        List<Reservation> reservations = reservationService.getAllReservations();
        if (reservations.isEmpty()) {
            reservationsView.showMessage("No reservations in the system.");
        } else {
            reservations.forEach(r -> reservationsView.showMessage(r.toString()));
        }
    }

    // Admin can approve or deny a reservation
    private void approveOrDenyReservation() {
        int id = reservationsView.getReservationIdInput();
        Reservation res = reservationService.findReservationById(id);
        if (res == null) {
            reservationsView.showMessage("Reservation not found.");
            return;
        }

        reservationsView.showMessage("Current status: " + res.getStatus());
        System.out.print("Approve (A) / Deny (D): ");
        String decision = sc.nextLine().trim().toUpperCase();

        if (decision.equals("A")) {
            res.setStatus(ReservationStatus.APPROVED);
            reservationsView.showMessage("Reservation " + id + " approved.");
        } else if (decision.equals("D")) {
            res.setStatus(ReservationStatus.DENIED);
            reservationsView.showMessage("Reservation " + id + " denied.");
        } else {
            reservationsView.showMessage("Invalid input. Action cancelled.");
        }
    }
}
