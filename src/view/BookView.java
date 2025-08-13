package view;

import model.Book;

import java.util.List;
import java.util.Scanner;

public class BookView {
    private final Scanner sc;

    public BookView(Scanner sc) {
        this.sc = sc;
    }

    public void displayBookMenu() {
        System.out.println("\n=== Book Management ===");
        System.out.println("1. View");
        System.out.println("2. List (Go through the books like a list)");
        System.out.println("3. Create");
        System.out.println("4. Update");
        System.out.println("5. Delete");
        System.out.println("6. Filter");
        System.out.println("7. Read Book"); // NEW
        System.out.println("0. Back");
        System.out.print("Enter choice: ");
    }

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

    public void showBookList(List<Book> books) {
        if (books.isEmpty()) {
            System.out.println("No books found.");
        } else {
            System.out.println("\n=== Book List ===");
            for (Book book : books) {
                System.out.printf("ID: %d | Title: %s | Author: %s | Genre: %s | Released: %s%n",
                        book.getId(), book.getTitle(), book.getAuthor(), book.getGenre(), book.getReleasedDate());
            }
        }
    }

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

    // NEW method for reading book content
    public void showBookContent(List<String> content) {
        System.out.println("\n=== Book Content ===");
        content.forEach(System.out::println);
        System.out.println("====================\n");
    }

    public void showMessage(String message) {
        System.out.println(message);
    }
}
