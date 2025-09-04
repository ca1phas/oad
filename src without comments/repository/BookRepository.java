package repository;

import model.Book;
import util.DateTimeUtil;

import java.util.*;

public class BookRepository extends BaseRepository<Book> {

    public BookRepository() {
        super("data/books.txt", "id|title|author|genre|releasedDate|filename");
    }

    @Override
    protected Book mapToModel(List<String> row) {
        return new Book(
                Integer.parseInt(row.get(0)),
                row.get(1),
                row.get(2),
                row.get(3),
                DateTimeUtil.parseDate(row.get(4)),
                row.get(5));
    }

    @Override
    protected List<String> mapFromModel(Book book) {
        return Arrays.asList(
                String.valueOf(book.getId()),
                book.getTitle(),
                book.getAuthor(),
                book.getGenre(),
                DateTimeUtil.formatDate(book.getReleasedDate()),
                book.getFilename());
    }

    public Optional<Book> findById(int id) {
        return findByKey(String.valueOf(id));
    }

    public boolean update(Book updated) {
        return updateByKey(updated);
    }

    public boolean delete(int id) {
        return deleteByKey(String.valueOf(id));
    }
}
