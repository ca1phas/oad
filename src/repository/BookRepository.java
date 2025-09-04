package repository;

import model.Book;
import util.DateTimeUtil;

import java.util.*;

public class BookRepository extends BaseRepository<Book> {

    // Constructor sets file path and schema for books.txt
    public BookRepository() {
        super("data/books.txt", "id|title|author|genre|releasedDate|filename");
    }

    // Convert a row from the file into a Book object
    @Override
    protected Book mapToModel(List<String> row) {
        return new Book(
                Integer.parseInt(row.get(0)), // Book ID
                row.get(1), // Title
                row.get(2), // Author
                row.get(3), // Genre
                DateTimeUtil.parseDate(row.get(4)), // Released Date (LocalDate)
                row.get(5) // Filename (e.g., eBook file name)
        );
    }

    // Convert a Book object into a list of strings for file storage
    @Override
    protected List<String> mapFromModel(Book book) {
        return Arrays.asList(
                String.valueOf(book.getId()), // Book ID
                book.getTitle(), // Title
                book.getAuthor(), // Author
                book.getGenre(), // Genre
                DateTimeUtil.formatDate(book.getReleasedDate()), // Released Date formatted (yyyy-MM-dd)
                book.getFilename() // Filename
        );
    }

    // Find book by ID
    public Optional<Book> findById(int id) {
        return findByKey(String.valueOf(id));
    }

    // Update an existing book in the file
    public boolean update(Book updated) {
        return updateByKey(updated);
    }

    // Delete book by ID
    public boolean delete(int id) {
        return deleteByKey(String.valueOf(id));
    }
}
