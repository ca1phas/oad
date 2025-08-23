package service;

import model.Book;
import repository.BookRepository;
import util.IDGeneratorUtil;
import util.PaginationUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BookService {
    private final BookRepository bookRepository;
    private static final int PAGE_SIZE = 20;

    public BookService() {
        this.bookRepository = new BookRepository();
    }

    // FR24: Add book
    public boolean addBook(String title, String author, String genre, LocalDate releasedDate, String filename) {
        Path filePath = Path.of("books", filename + ".txt");
        if (!Files.exists(filePath))
            return false;

        int id = IDGeneratorUtil.generateId(this.bookRepository.getFilePath());
        Book book = new Book(id, title, author, genre, releasedDate, filename);
        bookRepository.append(book);
        return true;
    }

    // FR 15 & FR23 & FR25: View book details (& Select book)
    public Optional<Book> viewBook(int id) {
        return bookRepository.findById(id);
    }

    // FR27: Read book by page
    public List<String> readBook(String filename, int pageNumber, boolean hasReserved) {
        if (!hasReserved)
            return Collections.emptyList();

        Path filePath = Path.of("books", filename + ".txt");
        if (!Files.exists(filePath))
            return Collections.emptyList();
        
        try {
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            return PaginationUtil.paginate(lines, pageNumber, PAGE_SIZE);
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    // FR28: Update title
    public boolean updateTitle(int bookId, String newTitle, boolean isAdmin) {
        if (!isAdmin)
            return false;

        Optional<Book> optional = bookRepository.findById(bookId);
        if (optional.isEmpty())
            return false;

        Book book = optional.get();
        book.setTitle(newTitle);
        return bookRepository.update(book);
    }

    // FR29: Update author
    public boolean updateAuthor(int bookId, String newAuthor, boolean isAdmin) {
        if (!isAdmin)
            return false;

        Optional<Book> optional = bookRepository.findById(bookId);
        if (optional.isEmpty())
            return false;

        Book book = optional.get();
        book.setAuthor(newAuthor);
        return bookRepository.update(book);
    }

    // FR30: Update genre
    public boolean updateGenre(int bookId, String newGenre, boolean isAdmin) {
        if (!isAdmin)
            return false;

        Optional<Book> optional = bookRepository.findById(bookId);
        if (optional.isEmpty())
            return false;

        Book book = optional.get();
        book.setGenre(newGenre);
        return bookRepository.update(book);
    }

    // FR31: Update release date
    public boolean updateReleaseDate(int bookId, LocalDate newDate, boolean isAdmin) {
        if (!isAdmin)
            return false;

        Optional<Book> optional = bookRepository.findById(bookId);
        if (optional.isEmpty())
            return false;

        Book book = optional.get();
        book.setReleasedDate(newDate);
        return bookRepository.update(book);
    }

    // FR32: Update filename
    public boolean updateFilename(int bookId, String newFilename, boolean isAdmin) {
        if (!isAdmin) {
            return false;
        }

        Optional<Book> optional = bookRepository.findById(bookId);
        if (optional.isEmpty()) {
            return false;
        }

        Book book = optional.get();
        book.setFilename(newFilename);
        return bookRepository.update(book);
    }

    // FR33: Delete book
    public boolean deleteBook(int bookId, boolean isAdmin) {
        if (!isAdmin)
            return false;
        return bookRepository.delete(bookId);
    }

    // FR21 & FR22 & FR 20: Filter + Sort + Paginate books
    public List<Book> filterSortPaginateBooks(
            String idFilter,
            String titleFilter,
            String authorFilter,
            String genreFilter,
            LocalDate releasedStart,
            LocalDate releasedEnd,
            String sortField,
            boolean ascending,
            int pageNumber,
            int pageSize) {

        List<Book> books = bookRepository.readAll();

        // Filtering
        if (idFilter != null && !idFilter.isBlank())
            books = books.stream()
                    .filter(b -> String.valueOf(b.getId()).contains(idFilter))
                    .collect(Collectors.toList());

        if (titleFilter != null && !titleFilter.isBlank())
            books = books.stream()
                    .filter(b -> b.getTitle().toLowerCase().contains(titleFilter.toLowerCase()))
                    .collect(Collectors.toList());

        if (authorFilter != null && !authorFilter.isBlank())
            books = books.stream()
                    .filter(b -> b.getAuthor().toLowerCase().contains(authorFilter.toLowerCase()))
                    .collect(Collectors.toList());

        if (genreFilter != null && !genreFilter.isBlank())
            books = books.stream()
                    .filter(b -> b.getGenre().toLowerCase().contains(genreFilter.toLowerCase()))
                    .collect(Collectors.toList());

        if (releasedStart != null)
            books = books.stream()
                    .filter(b -> !b.getReleasedDate().isBefore(releasedStart))
                    .collect(Collectors.toList());

        if (releasedEnd != null)
            books = books.stream()
                    .filter(b -> !b.getReleasedDate().isAfter(releasedEnd))
                    .collect(Collectors.toList());

        // Sorting
        Comparator<Book> comparator = switch (sortField == null ? "" : sortField.toLowerCase()) {
            case "title" -> Comparator.comparing(Book::getTitle);
            case "author" -> Comparator.comparing(Book::getAuthor);
            case "genre" -> Comparator.comparing(Book::getGenre);
            case "releaseddate" -> Comparator.comparing(Book::getReleasedDate);
            default -> Comparator.comparing(Book::getId);
        };

        if (!ascending)
            comparator = comparator.reversed();

        books.sort(comparator);
        return PaginationUtil.paginate(books, pageNumber, pageSize);
    }
    
    // Checks if a book file has content on the next page.
    public boolean hasNextPage(String filename, int currentPage) {
        Path filePath = Paths.get("books", filename + ".txt");
        if (!Files.exists(filePath)) {
            return false;
        }

        try (Stream<String> lines = Files.lines(filePath, StandardCharsets.UTF_8)) {
            long totalLines = lines.count();
            int totalPages = (int) Math.ceil((double) totalLines / PAGE_SIZE);
            return currentPage < totalPages;
        } catch (IOException e) {
            return false;
        }
    }
}