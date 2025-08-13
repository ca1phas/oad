package controller;

import model.Book;
import service.BookService;
import view.BookView;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
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
                case 1:
                    handleViewBook();
                    break;
                case 2:
                    handleListBooks();
                    break;
                case 3:
                    if (isAdmin) handleAddBook();
                    else bookView.showMessage(ADMIN_ONLY_MESSAGE);
                    break;
                case 4:
                    if (isAdmin) handleUpdateBook();
                    else bookView.showMessage(ADMIN_ONLY_MESSAGE);
                    break;
                case 5:
                    if (isAdmin) handleDeleteBook();
                    else bookView.showMessage(ADMIN_ONLY_MESSAGE);
                    break;
                case 6:
                    handleFilterBooks();
                    break;
                case 7:
                    handleReadBook(hasReserved);
                    break;
                case 0:
                    return;
                default:
                    bookView.showMessage(INVALID_CHOICE_MESSAGE);
            }
        }
    }

    private void handleViewBook() {
        int id = bookView.promptInt("Enter book ID: ");
        Optional<Book> book = bookService.viewBook(id);
        bookView.showBookDetails(book.orElse(null));
    }

    private void handleListBooks() {
        // Default list: page 1, size 10, sorted by ID ascending
        List<Book> books = bookService.filterSortPaginateBooks(
                "", "", "", "", null, null, "id", true, 1, 10
        );
        bookView.showBookList(books);
    }

    private void handleFilterBooks() {
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
    }

    private void handleAddBook() {
        String title = bookView.prompt("Enter title: ");
        String author = bookView.prompt("Enter author: ");
        String genre = bookView.prompt("Enter genre: ");
        LocalDate releaseDate = promptDate("Enter release date (yyyy-MM-dd): ");
        String filename = bookView.prompt("Enter filename (without .txt): ");

        boolean success = bookService.addBook(title, author, genre, releaseDate, filename);
        bookView.showMessage(success ? "Book added successfully." : "Failed to add book.");
    }

    private void handleUpdateBook() {
        int id = bookView.promptInt("Enter book ID: ");
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

    private void handleDeleteBook() {
        int id = bookView.promptInt("Enter book ID: ");
        bookView.showMessage(bookService.deleteBook(id, true) ? "Deleted successfully." : "Delete failed.");
    }

    private void handleReadBook(boolean hasReserved) {
        int id = bookView.promptInt("Enter book ID to read: ");
        Optional<Book> optional = bookService.viewBook(id);
        if (optional.isEmpty()) {
            bookView.showMessage("Book not found.");
            return;
        }

        Book book = optional.get();
        String filename = book.getFilename();

        if (!hasReserved) {
            bookView.showMessage("You must reserve the book first to read it.");
            return;
        }

        int pageNumber = 1;
        while (true) {
            List<String> page = bookService.readBook(filename, pageNumber, true);
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
