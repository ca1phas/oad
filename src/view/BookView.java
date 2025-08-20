package view;

import model.Book;

import java.util.List;
import java.util.Scanner;

public class BookView {
    private final Scanner sc;

    public BookView(Scanner sc) {
        this.sc = sc;
    }

    // === MAIN BOOK MENU (HOME PAGE) ===
    public void displayBookMenu() {
        System.out.println("\n=== Books (Home Page) ===");
        System.out.println("1. View Books by Page");
        System.out.println("2. Filter & Sort Books");
        System.out.println("3. View Book Details");
        System.out.println("4. Create New Book");
        System.out.println("0. Back");
        System.out.print("Enter choice: ");
    }

    // === SUBMENU FOR A SINGLE BOOK ===
    public void displayBookSubMenu(boolean isAdmin) {
        System.out.println("\n=== Book Options ===");
        if (isAdmin) {
            System.out.println("1. Update Book");
            System.out.println("2. Delete Book");
        }
        System.out.println("3. Read Book");
        System.out.println("4. Reserve Book");
        System.out.println("0. Back");
    }

    // === INPUT HELPERS ===
    public String prompt(String message) {
        System.out.print(message);
        return sc.nextLine().trim();
    }

    public int promptInt(String message) {
        System.out.print(message);
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // === DISPLAY LIST OF BOOKS WITH PAGINATION ===
    public void showBookList(List<Book> books) {
        if (books.isEmpty()) {
            System.out.println("No books found.");
            return;
        }

        final int pageSize = 5; // Number of books per page
        int page = 0;
        int totalPages = (int) Math.ceil((double) books.size() / pageSize);

        while (true) {
            int start = page * pageSize;
            int end = Math.min(start + pageSize, books.size());

            System.out.println("\n=== Book List (Page " + (page + 1) + " of " + totalPages + ") ===");
            for (int i = start; i < end; i++) {
                Book book = books.get(i);
                System.out.printf(
                        "ID: %d\nTitle: %s\nAuthor: %s\nGenre: %s\nReleased: %s\n----------------------\n",
                        book.getId(), book.getTitle(), book.getAuthor(),
                        book.getGenre(), book.getReleasedDate()
                );
            }

            System.out.println("1. Previous Page | 2. Next Page | 0. Exit");
            int choice = promptInt("Choose option: ");

            if (choice == 1 && page > 0) {
                page--;
            } else if (choice == 2 && page < totalPages - 1) {
                page++;
            } else if (choice == 0) {
                break;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    // === DISPLAY SINGLE BOOK DETAILS ===
    public void showBookDetails(Book book) {
        if (book == null) {
            System.out.println("Book not found.");
        } else {
            System.out.println("\n=== Book Details ===");
            System.out.println("ID: " + book.getId());
            System.out.println("Title: " + book.getTitle());
            System.out.println("Author: " + book.getAuthor());
            System.out.println("Genre: " + book.getGenre());
            System.out.println("Released: " + book.getReleasedDate());
            System.out.println("Filename: " + book.getFilename());
        }
    }

    // === DISPLAY BOOK CONTENT (FOR READING) ===
    public void showBookContent(List<String> content) {
        System.out.println("\n=== Book Content ===");
        content.forEach(System.out::println);
        System.out.println("====================\n");
    }

    // === GENERIC MESSAGE ===
    public void showMessage(String message) {
        System.out.println(message);
    }
}
