package controller;

import model.Book;
import service.BookService;
import util.DateTimeUtil;
import view.BookView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class BookController {
    private final BookService bookService;
    private final BookView bookView;

    // Constants
    private static final String ADMIN_ONLY_MESSAGE = "Only admins can perform this action.";
    private static final String INVALID_CHOICE_MESSAGE = "Invalid choice.";
    private static final String INVALID_DATE_MESSAGE = "Invalid date format. Please use yyyy-mm-dd.";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public BookController(Scanner sc) {
        this.bookService = new BookService();
        this.bookView = new BookView(sc);
    }

    // === MAIN MENU ===
    public void handleMenu(boolean isAdmin, boolean hasReserved) {
        while (true) {
            bookView.displayBookMenu();
            int choice = bookView.promptInt("");

            switch (choice) {
                case 1 -> handleListBooksByPage(isAdmin, hasReserved);
                case 2 -> handleFilterBooks(isAdmin, hasReserved);
                case 3 -> handleViewBookDetails(isAdmin, hasReserved);
                case 4 -> {
                    if (isAdmin)
                        handleAddBook();
                    else
                        bookView.showMessage(ADMIN_ONLY_MESSAGE);
                }
                case 0 -> {
                    return;
                } // Exit to main menu
                default -> bookView.showMessage(INVALID_CHOICE_MESSAGE);
            }
        }
    }

    // === 1. LIST BOOKS WITH PAGINATION ===
    private void handleListBooksByPage(boolean isAdmin, boolean hasReserved) {
        List<Book> allBooks = bookService.filterSortPaginateBooks(
                "", "", "", "", null, null, "id", true, 1, Integer.MAX_VALUE);

        if (allBooks.isEmpty()) {
            bookView.showMessage("No books found.");
            return;
        }

        final int pageSize = 5;
        int pageNumber = 0;
        int totalPages = (int) Math.ceil((double) allBooks.size() / pageSize);

        while (true) {
            int start = pageNumber * pageSize;
            int end = Math.min(start + pageSize, allBooks.size());
            List<Book> pageOfBooks = allBooks.subList(start, end);

            bookView.showBookListPage(pageOfBooks, pageNumber + 1, totalPages);

            // This line now correctly separates navigation from other actions
            int choice = bookView.promptInt("Choose an option: ");

            if (choice == 0) {
                bookView.showMessage("Returning to 📚 BOOKS (HOME PAGE)...");
                return;
            } else if (choice == 1 && pageNumber > 0) {
                pageNumber--;
            } else if (choice == 2 && pageNumber < totalPages - 1) {
                pageNumber++;
            } else {
                bookView.showMessage(INVALID_CHOICE_MESSAGE);
            }
        }
    }

    // === 3. VIEW SINGLE BOOK ===
    private void handleViewBookDetails(boolean isAdmin, boolean hasReserved) {
        int id = bookView.promptInt("Enter Book ID to view details (0 to go back): ");
        if (id == 0)
            return;

        bookService.viewBook(id).ifPresentOrElse(
                b -> handleBookSubMenu(b, isAdmin, hasReserved),
                () -> bookView.showMessage("Book not found."));
    }

    // === 2. FILTER & SORT BOOKS ===
    private void handleFilterBooks(boolean isAdmin, boolean hasReserved) {
        String idFilter = bookView.prompt("Filter by ID (partial match): ");
        String titleFilter = bookView.prompt("Filter by Title (partial match): ");
        String authorFilter = bookView.prompt("Filter by Author (partial match): ");
        String genreFilter = bookView.prompt("Filter by Genre (partial match): ");

        LocalDate startDate = parseDate(bookView.prompt("Start Release Date (yyyy-mm-dd or blank): "));
        LocalDate endDate = parseDate(bookView.prompt("End Release Date (yyyy-mm-dd or blank): "));

        String sortField = bookView.prompt("Sort by (id, title, author, genre, releasedDate): ").toLowerCase();
        if (!isValidSortField(sortField)) {
            bookView.showMessage("Invalid sort field. Using 'id' as default.");
            sortField = "id";
        }

        boolean ascending = bookView.prompt("Ascending? (y/n): ").equalsIgnoreCase("y");
        int pageNumber = Math.max(1, bookView.promptInt("Page number: "));
        int pageSize = Math.max(1, bookView.promptInt("Page size: "));

        // The partial ID filter is now handled by the service layer, no manual
        // filtering needed here.
        List<Book> books = bookService.filterSortPaginateBooks(
                idFilter, titleFilter, authorFilter, genreFilter,
                startDate, endDate, sortField, ascending, pageNumber, pageSize);

        if (books.isEmpty()) {
            bookView.showMessage("No books found matching the criteria.");
            return;
        }

        // Get total results for correct pagination info
        int totalResults = bookService.filterSortPaginateBooks(
                idFilter, titleFilter, authorFilter, genreFilter,
                startDate, endDate, sortField, ascending, 1, Integer.MAX_VALUE).size();
        int totalPages = (int) Math.ceil((double) totalResults / pageSize);

        bookView.showBookListPage(books, pageNumber, totalPages);

        int id = bookView.promptInt("");
        if (id != 0) {
            bookService.viewBook(id).ifPresent(b -> handleBookSubMenu(b, isAdmin, hasReserved));
        }
    }

    private boolean isValidSortField(String field) {
        return switch (field.toLowerCase()) {
            case "id", "title", "author", "genre", "releaseddate" -> true;
            default -> false;
        };
    }

    // === SUBMENU FOR BOOK ===
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
                    } else
                        bookView.showMessage(ADMIN_ONLY_MESSAGE);
                }
                case 2 -> {
                    if (isAdmin) {
                        if (handleDeleteBook(book.getId())) {
                            return; // Exit the submenu after successful deletion
                        }
                    } else
                        bookView.showMessage(ADMIN_ONLY_MESSAGE);
                }
                case 3 -> handleReadBook(book);
                case 4 -> handleReserveBook(book, hasReserved);
                case 0 -> {
                    return;
                }
                default -> bookView.showMessage(INVALID_CHOICE_MESSAGE);
            }
        }
    }

    // === 4. CRUD OPERATIONS ===
    private void handleAddBook() {
        String title = bookView.prompt("Enter title: ");
        String author = bookView.prompt("Enter author: ");
        String genre = bookView.prompt("Enter genre: ");

        // Correctly handle date input with a loop until valid input is provided
        LocalDate releaseDate;
        while (true) {
            String dateString = bookView.prompt("Enter release date (yyyy-mm-dd): ");
            releaseDate = DateTimeUtil.parseDate(dateString);
            if (releaseDate != null) {
                break; // Exit the loop if the date is successfully parsed
            }
            // DateTimeUtil.parseDate already prints the error message, so no need for
            // another one here.
        }

        String filename = bookView.prompt("Enter filename (without .txt): ");

        try {
            if (bookService.addBook(title, author, genre, releaseDate, filename)) {
                bookView.showMessage("Book added successfully.");
            } else {
                bookView.showMessage("Failed to add book. File '" + filename + ".txt' not found in books directory.");
            }
        } catch (Exception e) {
            bookView.showMessage("Failed to add book. Error: " + e.getMessage());
        }
    }

    private void handleUpdateBook(int id) {
        String field = bookView.prompt("Enter field to update (title, author, genre, date, filename): ").toLowerCase();
        String newValue = null;
        LocalDate newDate = null;

        if (field.equals("date")) {
            newDate = promptDate("Enter new release date (yyyy-mm-dd): ");
        } else {
            newValue = bookView.prompt("Enter new value: ");
            if (field.equals("filename") && newValue.endsWith(".txt")) {
                newValue = newValue.substring(0, newValue.length() - 4);
            }
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

    private boolean handleDeleteBook(int id) {
        String confirmation = bookView.prompt("Type 'YES' to confirm deletion: ");
        if (!confirmation.equalsIgnoreCase("YES")) {
            bookView.showMessage("Deletion cancelled.");
            return false;
        }

        boolean success = bookService.deleteBook(id, true);
        bookView.showMessage(success ? "Book deleted successfully." : "Delete failed.");
        return success;
    }

    private void handleReadBook(Book book) {
        int pageNumber = 1;
        while (true) {
            try {
                List<String> page = bookService.readBook(book.getFilename(), pageNumber, true);
                if (page == null || page.isEmpty()) {
                    bookView.showMessage(pageNumber == 1
                            ? "Book is empty or file '" + book.getFilename() + ".txt' not found."
                            : "End of book reached.");
                    break;
                }

                bookView.showBookContent(page, pageNumber);
                int action = bookView.promptInt("Navigation\n 1: Next \n 2: Previous \n 0: Exit\n Option: ");

                if (action == 1)
                    pageNumber++;
                else if (action == 2 && pageNumber > 1)
                    pageNumber--;
                else if (action == 0) {
                    bookView.showMessage("Exiting book reader...");
                    break;
                } else {
                    bookView.showMessage("Invalid option.");
                }
            } catch (Exception e) {
                bookView.showMessage("Error reading book: " + e.getMessage());
                break;
            }
        }
    }

    private void handleReserveBook(Book book, boolean hasReserved) {
        if (hasReserved) {
            bookView.showMessage("You already reserved a book. Only one reservation allowed.");
        } else {
            bookView.showMessage("Book '" + book.getTitle() + "' reserved successfully!");
        }
    }

    // === HELPERS ===
    private LocalDate promptDate(String message) {
        while (true) {
            try {
                return LocalDate.parse(bookView.prompt(message), DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println(e);
                bookView.showMessage(INVALID_DATE_MESSAGE);
            }
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank())
            return null;
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            bookView.showMessage(INVALID_DATE_MESSAGE + " You entered: '" + dateStr + "'");
            return null;
        }
    }
}