package repository;

import util.FileHandlerUtil;

import java.util.*;
import java.util.function.Predicate;

import model.base.Identifiable;

public abstract class BaseRepository<T extends Identifiable> {
    private final String filePath;
    private final String header;

    public BaseRepository(String filePath, String header) {
        this.filePath = filePath;
        this.header = header;
    }

    // Abstract methods to be implemented by subclasses
    protected abstract T mapToModel(List<String> row);

    protected abstract List<String> mapFromModel(T model);

    // Find all records
    public List<T> readAll() {
        List<T> result = new ArrayList<>();
        List<List<String>> rows = FileHandlerUtil.readData(filePath);
        for (List<String> row : rows) {
            result.add(mapToModel(row));
        }
        return result;
    }

    // Save all records
    public void saveAll(List<T> data) {
        List<List<String>> rows = new ArrayList<>();
        for (T model : data) {
            rows.add(mapFromModel(model));
        }
        FileHandlerUtil.writeData(filePath, header, rows);
    }

    // Append one record
    public void append(T model) {
        FileHandlerUtil.appendDataRow(filePath, mapFromModel(model));
    }

    // Update first matching record
    public boolean update(Predicate<T> matcher, T updatedModel) {
        List<T> data = readAll();
        for (int i = 0; i < data.size(); i++) {
            if (matcher.test(data.get(i))) {
                data.set(i, updatedModel);
                saveAll(data);
                return true;
            }
        }
        return false;
    }

    // Delete matching records
    public boolean delete(Predicate<T> matcher) {
        List<T> data = readAll();
        boolean removed = data.removeIf(matcher);
        if (removed) {
            saveAll(data);
        }
        return removed;
    }

    public Optional<T> findByKey(String key) {
        return readAll().stream()
                .filter(model -> model.getKey().equalsIgnoreCase(key))
                .findFirst();
    }

    public boolean updateByKey(T updatedModel) {
        return update(item -> item.getKey().equalsIgnoreCase(updatedModel.getKey()), updatedModel);
    }

    public boolean deleteByKey(String key) {
        return delete(item -> item.getKey().equalsIgnoreCase(key));
    }
}
