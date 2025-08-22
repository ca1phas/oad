package view;

import model.Book;
import java.util.List;
import java.util.Scanner;

public class BookView {
    private final Scanner sc;

    public BookView(Scanner sc) {
        this.sc = sc;
    }

    // === MAIN MENU ===
    public void displayBookMenu() {
        System.out.println("        BOOKS (HOME PAGE) ");
        System.out.println("1. View Books by Page");
        System.out.println("2. Filter & Sort Books");
        System.out.println("3. View Book Details");
        System.out.println("4. Create New Book (Admin Only)");
        System.out.println("0. Back to Main Menu");
        System.out.print("Enter your choice: ");
    }

    // === SUBMENU ===
    public void displayBookSubMenu(boolean isAdmin) {

        System.out.println("        BOOK OPTIONS ");

        if (isAdmin) {
            System.out.println("1. Update Book (Admin)");
            System.out.println("2. Delete Book (Admin)");
        }
        System.out.println("3. Read Book");
        System.out.println("4. Reserve Book");
        System.out.println("0. Back");

    }

    // === INPUT ===
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

    // === BOOK LIST ===
    public void showBookListPage(List<Book> books, int currentPage, int totalPages) {
        if (books.isEmpty()) {
            System.out.println("\n No books found for this page.");
            return;
        }

        System.out.printf("                    = BOOK LIBRARY - PAGE %d OF %d =%n", currentPage, totalPages);

        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            System.out.printf("%-3d │ ID: %-4d │ %-30s │ %-20s │ %-12s │ %s%n",
                    (i + 1),
                    book.getId(),
                    truncate(book.getTitle(), 30),
                    truncate(book.getAuthor(), 20),
                    truncate(book.getGenre(), 12),
                    book.getReleasedDate()
            );
        }

        System.out.print(" Navigation: ");
        if (currentPage > 1) System.out.print("1️ Previous │ ");
        if (currentPage < totalPages) System.out.print("2️ Next │ ");
        System.out.println("0️ Back to Books (Home)");
    }

    // === BOOK DETAILS ===
    public void showBookDetails(Book book) {
        if (book == null) {
            System.out.println("\n Book not found.");
            return;
        }

        System.out.println("                    = BOOK DETAILS =");

        System.out.printf(" ID: %d%n", book.getId());
        System.out.printf(" Title: %s%n", book.getTitle());
        System.out.printf(" Author: %s%n", book.getAuthor());
        System.out.printf(" Genre: %s%n", book.getGenre());
        System.out.printf(" Released: %s%n", book.getReleasedDate());
        System.out.printf(" Filename: %s.txt%n", book.getFilename());

    }

    // === BOOK READER ===
    public void showBookContent(List<String> content, int pageNumber) {
        System.out.printf("                     READING - PAGE %d %n", pageNumber);

        if (content == null || content.isEmpty()) {
            System.out.println(" No content available on this page.");
        } else {
            content.forEach(line -> System.out.println("  " + line));
        }

    }

    public void showBookContent(List<String> content) {
        showBookContent(content, 1);
    }

    // === MESSAGE ===
    public void showMessage(String message) {
        String msg = message.toLowerCase();
        if (msg.contains("success") || msg.contains("added") || msg.contains("updated") || msg.contains("reserved")) {
            System.out.println(" " + message);
        } else if (msg.contains("error") || msg.contains("failed") || msg.contains("not found") || msg.contains("invalid")) {
            System.out.println(" " + message);
        } else if (msg.contains("cancelled") || msg.contains("exiting") || msg.contains("returning")) {
            System.out.println("ℹ " + message);
        } else {
            System.out.println(" " + message);
        }
    }

    // === HELPER ===
    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        return str.length() <= maxLength
                ? String.format("%-" + maxLength + "s", str)
                : str.substring(0, maxLength - 3) + "...";
    }
}