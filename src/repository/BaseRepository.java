package repository;

import util.FileHandlerUtil;

import java.util.*;
import java.util.function.Predicate;

import model.base.Identifiable;

public abstract class BaseRepository<T extends Identifiable> {
    private final String filePath; // Path to the storage file
    private final String header; // File header schema (column names)

    // Constructor sets file path and header for subclasses
    public BaseRepository(String filePath, String header) {
        this.filePath = filePath;
        this.header = header;
    }

    // Get the file path
    public String getFilePath() {
        return filePath;
    }

    // Get the header (used when writing to file)
    public String getHeader() {
        return header;
    }

    // Abstract methods for mapping data between file rows and model objects
    protected abstract T mapToModel(List<String> row); // Convert row → model

    protected abstract List<String> mapFromModel(T model); // Convert model → row

    // Read all records from the file and map them into model objects
    public List<T> readAll() {
        List<T> result = new ArrayList<>();
        List<List<String>> rows = FileHandlerUtil.readData(filePath); // Read raw data rows
        for (List<String> row : rows) {
            result.add(mapToModel(row)); // Convert each row into model object
        }
        return result;
    }

    // Save all records (overwrite file with given list)
    public void saveAll(List<T> data) {
        List<List<String>> rows = new ArrayList<>();
        for (T model : data) {
            rows.add(mapFromModel(model)); // Convert each model into row format
        }
        FileHandlerUtil.writeData(filePath, header, rows); // Write back to file
    }

    // Append one new record (does not overwrite)
    public void append(T model) {
        FileHandlerUtil.appendDataRow(filePath, mapFromModel(model));
    }

    // Update first record that matches a given condition
    public boolean update(Predicate<T> matcher, T updatedModel) {
        List<T> data = readAll();
        for (int i = 0; i < data.size(); i++) {
            if (matcher.test(data.get(i))) { // If record matches condition
                data.set(i, updatedModel); // Replace with updated record
                saveAll(data); // Save all changes
                return true;
            }
        }
        return false;
    }

    // Delete records that match a given condition
    public boolean delete(Predicate<T> matcher) {
        List<T> data = readAll();
        boolean removed = data.removeIf(matcher); // Remove matching records
        if (removed) {
            saveAll(data); // Save file after deletion
        }
        return removed;
    }

    // Find a single record by its unique key
    public Optional<T> findByKey(String key) {
        return readAll().stream()
                .filter(model -> model.getKey().equalsIgnoreCase(key)) // Match by key
                .findFirst();
    }

    // Update a record by key (replace existing record with updated model)
    public boolean updateByKey(T updatedModel) {
        return update(item -> item.getKey().equalsIgnoreCase(updatedModel.getKey()), updatedModel);
    }

    // Delete a record by key
    public boolean deleteByKey(String key) {
        return delete(item -> item.getKey().equalsIgnoreCase(key));
    }
}
