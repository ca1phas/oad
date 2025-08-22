package controller;

import model.Book;
import service.BookService;
import util.DateTimeUtil;
import view.BookView;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class BookController {
    private final BookService bookService;
    private final BookView bookView;

    private static final String ADMIN_ONLY_MESSAGE = "Only admins can perform this action.";
    private static final String INVALID_CHOICE_MESSAGE = "Invalid choice.";
    private static final String INVALID_DATE_MESSAGE = "Invalid date format. Please use yyyy-mm-dd.";

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
                    if (isAdmin) handleAddBook();
                    else bookView.showMessage(ADMIN_ONLY_MESSAGE);
                }
                case 0 -> { return; }
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
        int pageNumber = 1;
        int totalPages = (int) Math.ceil((double) allBooks.size() / pageSize);

        while (true) {
            int start = (pageNumber - 1) * pageSize;
            int end = Math.min(start + pageSize, allBooks.size());
            List<Book> pageOfBooks = allBooks.subList(start, end);

            // Show table + navigation inside view
            bookView.showBookListPage(pageOfBooks, pageNumber, totalPages);

            int choice = bookView.promptInt("Choose an option: ");
            if (choice == 0) break;
            else if (choice == 1 && pageNumber > 1) pageNumber--;
            else if (choice == 2 && pageNumber < totalPages) pageNumber++;
            else bookView.showMessage(INVALID_CHOICE_MESSAGE);
        }
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

        boolean ascending = true;
        int pageNumber = 1;
        int pageSize = 5;
        int totalResults = bookService.filterSortPaginateBooks(
                idFilter, titleFilter, authorFilter, genreFilter,
                startDate, endDate, sortField, ascending, 1, Integer.MAX_VALUE).size();
        int totalPages = (int) Math.ceil((double) totalResults / pageSize);

        while (true) {
            List<Book> books = bookService.filterSortPaginateBooks(
                    idFilter, titleFilter, authorFilter, genreFilter,
                    startDate, endDate, sortField, ascending, pageNumber, pageSize);

            if (books.isEmpty()) {
                bookView.showMessage("No books found matching the criteria.");
                return;
            }

            // Show table + navigation inside view
            bookView.showBookListPage(books, pageNumber, totalPages);

            int choice = bookView.promptInt("Choose an option: ");
            if (choice == 0) break;
            else if (choice == 1 && pageNumber > 1) pageNumber--;
            else if (choice == 2 && pageNumber < totalPages) pageNumber++;
            else bookView.showMessage(INVALID_CHOICE_MESSAGE);
        }
    }

    private boolean isValidSortField(String field) {
        return switch (field.toLowerCase()) {
            case "id", "title", "author", "genre", "releaseddate" -> true;
            default -> false;
        };
    }

    // === 3. VIEW SINGLE BOOK ===
    private void handleViewBookDetails(boolean isAdmin, boolean hasReserved) {
        int id = bookView.promptInt("Enter Book ID to view details (0 to go back): ");
        if (id == 0) return;

        bookService.viewBook(id).ifPresentOrElse(
                b -> handleBookSubMenu(b, isAdmin, hasReserved),
                () -> bookView.showMessage("Book not found."));
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
                    } else bookView.showMessage(ADMIN_ONLY_MESSAGE);
                }
                case 2 -> {
                    if (isAdmin && handleDeleteBook(book.getId())) return;
                    else if (!isAdmin) bookView.showMessage(ADMIN_ONLY_MESSAGE);
                }
                case 3 -> handleReadBook(book);
                case 4 -> handleReserveBook(book, hasReserved);
                case 0 -> { return; }
                default -> bookView.showMessage(INVALID_CHOICE_MESSAGE);
            }
        }
    }

    // === CRUD OPERATIONS ===
    private void handleAddBook() {
        String title = bookView.prompt("Enter title: ");
        String author = bookView.prompt("Enter author: ");
        String genre = bookView.prompt("Enter genre: ");

        LocalDate releaseDate;
        while (true) {
            String dateString = bookView.prompt("Enter release date (yyyy-mm-dd): ");
            try {
                releaseDate = DateTimeUtil.parseDate(dateString);
                break;
            } catch (Exception e) {
                bookView.showMessage(INVALID_DATE_MESSAGE);
            }
        }

        String filename;
        while (true) {
            filename = bookView.prompt("Enter filename (without .txt): ");
            if (isValidFilename(filename)) break;
            bookView.showMessage("Invalid filename. Use only letters, numbers, _, - and no : ? * < > |");
        }

        try {
            if (bookService.addBook(title, author, genre, releaseDate, filename))
                bookView.showMessage("Book added successfully.");
            else
                bookView.showMessage("Failed to add book. File '" + filename + ".txt' not found.");
        } catch (Exception e) {
            bookView.showMessage("Failed to add book. Error: " + e.getMessage());
        }
    }

    private void handleUpdateBook(int id) {
        String field = bookView.prompt("Enter field to update (title, author, genre, date, filename): ").toLowerCase();
        String newValue = null;
        LocalDate newDate = null;

        if (field.equals("date")) {
            while (true) {
                try {
                    newDate = DateTimeUtil.parseDate(bookView.prompt("Enter new release date (yyyy-mm-dd): "));
                    break;
                } catch (Exception e) {
                    bookView.showMessage(INVALID_DATE_MESSAGE);
                }
            }
        } else {
            newValue = bookView.prompt("Enter new value: ");
            if (field.equals("filename")) {
                while (!isValidFilename(newValue)) {
                    bookView.showMessage("Invalid filename. Use only letters, numbers, _, - and no : ? * < > |");
                    newValue = bookView.prompt("Enter new filename: ");
                }
                if (newValue.endsWith(".txt")) newValue = newValue.substring(0, newValue.length() - 4);
            }
        }

        boolean success = switch (field) {
            case "title" -> bookService.updateTitle(id, newValue, true);
            case "author" -> bookService.updateAuthor(id, newValue, true);
            case "genre" -> bookService.updateGenre(id, newValue, true);
            case "date" -> bookService.updateReleaseDate(id, newDate, true);
            case "filename" -> bookService.updateFilename(id, newValue, true);
            default -> { bookView.showMessage("Invalid field."); yield false; }
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
                System.out.println("Navigation:\n 1: Next \n 2: Previous \n 0: Exit");
                int action = bookView.promptInt("Option: ");

                if (action == 1) pageNumber++;
                else if (action == 2 && pageNumber > 1) pageNumber--;
                else if (action == 0) { bookView.showMessage("Exiting book reader..."); break; }
                else bookView.showMessage("Invalid option.");
            } catch (Exception e) {
                bookView.showMessage("Error reading book: " + e.getMessage());
                break;
            }
        }
    }

    private void handleReserveBook(Book book, boolean hasReserved) {
        if (hasReserved) bookView.showMessage("You already reserved a book. Only one reservation allowed.");
        else bookView.showMessage("Book '" + book.getTitle() + "' reserved successfully!");
    }

    // === HELPERS ===
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return DateTimeUtil.parseDate(dateStr);
        } catch (Exception e) {
            bookView.showMessage(INVALID_DATE_MESSAGE + " You entered: '" + dateStr + "'");
            return null;
        }
    }

    private boolean isValidFilename(String filename) {
        return filename.matches("[a-zA-Z0-9_-]+");
    }
}
