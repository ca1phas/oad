package controller;

import model.Book;
import service.BookService;
import view.BookView;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class BookController {
    private final BookService bookService;
    private final BookView bookView;

    private static final String ADMIN_ONLY_MESSAGE = "Only admins can perform this action.";
    private static final String INVALID_CHOICE_MESSAGE = "Invalid choice.";
    private static final String INVALID_DATE_MESSAGE = "Invalid date format. Please use yyyy-MM-dd.";

    public BookController(Scanner sc) {
        this.bookService = new BookService();
        this.bookView = new BookView(sc);
    }

    public void handleMenu(boolean isAdmin, boolean hasReserved) {
        while (true) {
            bookView.displayBookMenu();
            int choice = bookView.promptInt("");

            switch (choice) {
                case 1 -> handleListBooks(isAdmin, hasReserved);       // View by page
                case 2 -> handleFilterBooks(isAdmin, hasReserved);     // Filter & sort
                case 3 -> handleViewBookDetails(isAdmin, hasReserved); // View details
                case 4 -> {
                    if (isAdmin) handleAddBook();
                    else bookView.showMessage(ADMIN_ONLY_MESSAGE);
                }
                case 0 -> { return; }
                default -> bookView.showMessage(INVALID_CHOICE_MESSAGE);
            }
        }
    }

    // === 1. LIST BOOKS BY PAGE ===
    private void handleListBooks(boolean isAdmin, boolean hasReserved) {
        List<Book> books = bookService.filterSortPaginateBooks(
                "", "", "", "", null, null, "id", true, 1, 100
        );
        bookView.showBookList(books);

        int id = bookView.promptInt("\nEnter Book ID to view details (0 to go back): ");
        if (id == 0) return;

        bookService.viewBook(id).ifPresentOrElse(
            b -> handleBookSubMenu(b, isAdmin, hasReserved),
            () -> bookView.showMessage("Book not found.")
        );
    }

    // === 2. FILTER & SORT ===
    private void handleFilterBooks(boolean isAdmin, boolean hasReserved) {
        String idFilter = bookView.prompt("Filter by ID (blank for none): ");
        String titleFilter = bookView.prompt("Filter by Title: ");
        String authorFilter = bookView.prompt("Filter by Author: ");
        String genreFilter = bookView.prompt("Filter by Genre: ");
        String startDateStr = bookView.prompt("Start Release Date (yyyy-MM-dd or blank): ");
        String endDateStr = bookView.prompt("End Release Date (yyyy-MM-dd or blank): ");

        LocalDate startDate = parseDate(startDateStr);
        LocalDate endDate = parseDate(endDateStr);

        String sortField = bookView.prompt("Sort by (id, title, author, genre, releasedDate): ");
        boolean ascending = bookView.prompt("Ascending? (y/n): ").equalsIgnoreCase("y");

        int pageNumber = bookView.promptInt("Page number: ");
        int pageSize = bookView.promptInt("Page size: ");

        List<Book> books = bookService.filterSortPaginateBooks(
                idFilter, titleFilter, authorFilter, genreFilter,
                startDate, endDate, sortField, ascending,
                pageNumber, pageSize
        );

        bookView.showBookList(books);

        int id = bookView.promptInt("\nEnter Book ID to view details (0 to go back): ");
        if (id != 0) {
            bookService.viewBook(id).ifPresent(b -> handleBookSubMenu(b, isAdmin, hasReserved));
        }
    }

    // === 3. VIEW SINGLE BOOK DETAILS ===
    private void handleViewBookDetails(boolean isAdmin, boolean hasReserved) {
        int id = bookView.promptInt("Enter Book ID to view details (0 to go back): ");
        if (id == 0) return;

        bookService.viewBook(id).ifPresentOrElse(
            b -> handleBookSubMenu(b, isAdmin, hasReserved),
            () -> bookView.showMessage("Book not found.")
        );
    }

    // === SUBMENU FOR BOOK DETAILS ===
    private void handleBookSubMenu(Book book, boolean isAdmin, boolean hasReserved) {
        while (true) {
            bookView.showBookDetails(book);
            bookView.displayBookSubMenu(isAdmin);

            int choice = bookView.promptInt("Choose an option: ");
            switch (choice) {
                case 1 -> {
                    if (isAdmin) {
                        handleUpdateBook(book.getId());
                        book = bookService.viewBook(book.getId()).orElse(book);
                    } else bookView.showMessage(ADMIN_ONLY_MESSAGE);
                }
                case 2 -> {
                    if (isAdmin) {
                        handleDeleteBook(book.getId());
                        return; // go back after deletion
                    } else bookView.showMessage(ADMIN_ONLY_MESSAGE);
                }
                case 3 -> handleReadBook(book, hasReserved);
                case 4 -> handleReserveBook(book, hasReserved);
                case 0 -> { return; }
                default -> bookView.showMessage(INVALID_CHOICE_MESSAGE);
            }
        }
    }

    // === 4. CREATE BOOK ===
    private void handleAddBook() {
        String title = bookView.prompt("Enter title: ");
        String author = bookView.prompt("Enter author: ");
        String genre = bookView.prompt("Enter genre: ");
        LocalDate releaseDate = promptDate("Enter release date (yyyy-MM-dd): ");
        String filename = bookView.prompt("Enter filename (without .txt): ");

        boolean success = bookService.addBook(title, author, genre, releaseDate, filename);
        bookView.showMessage(success ? "Book added successfully." : "Failed to add book.");
    }

    private void handleUpdateBook(int id) {
        String field = bookView.prompt("Enter field to update (title, author, genre, date, filename): ").toLowerCase();

        String newValue = null;
        LocalDate newDate = null;

        if (field.equals("date")) {
            newDate = promptDate("Enter new release date (yyyy-MM-dd): ");
        } else {
            newValue = bookView.prompt("Enter new value: ");
        }

        boolean success = switch (field) {
            case "title" -> bookService.updateTitle(id, newValue, true);
            case "author" -> bookService.updateAuthor(id, newValue, true);
            case "genre" -> bookService.updateGenre(id, newValue, true);
            case "date" -> bookService.updateReleaseDate(id, newDate, true);
            case "filename" -> bookService.updateFilename(id, newValue, true);
            default -> {
                bookView.showMessage("Invalid field.");
                yield false;
            }
        };

        bookView.showMessage(success ? "Updated successfully." : "Update failed.");
    }

    private void handleDeleteBook(int id) {
        bookView.showMessage(bookService.deleteBook(id, true) ? "Deleted successfully." : "Delete failed.");
    }

    private void handleReadBook(Book book, boolean hasReserved) {
        if (!hasReserved) {
            bookView.showMessage("You must reserve the book first to read it.");
            return;
        }

        int pageNumber = 1;
        while (true) {
            List<String> page = bookService.readBook(book.getFilename(), pageNumber, true);
            if (page.isEmpty()) {
                bookView.showMessage("No more content.");
                break;
            }

            bookView.showBookContent(page);

            String action = bookView.prompt("N = next page, P = previous page, Q = quit: ").toLowerCase();
            if (action.equals("n")) pageNumber++;
            else if (action.equals("p") && pageNumber > 1) pageNumber--;
            else if (action.equals("q")) break;
            else bookView.showMessage("Invalid option.");
        }
    }

    private void handleReserveBook(Book book, boolean hasReserved) {
        if (hasReserved) {
            bookView.showMessage("You have already reserved this book.");
            return;
        }
        // just a stub, depends on your reservation system
        bookView.showMessage("Book reserved successfully.");
    }

    private LocalDate promptDate(String message) {
        while (true) {
            try {
                String dateStr = bookView.prompt(message);
                return LocalDate.parse(dateStr);
            } catch (DateTimeParseException e) {
                bookView.showMessage(INVALID_DATE_MESSAGE);
            }
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr.isBlank()) return null;
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            bookView.showMessage(INVALID_DATE_MESSAGE);
            return null;
        }
    }
}
