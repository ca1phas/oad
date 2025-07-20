package util;

import java.io.*;
import java.util.*;

public class FileHandlerUtil {

    private static final String DELIMITER = "\\|";

    // Reads data lines (excluding header) and splits them
    public static List<List<String>> readData(String filePath) {
        List<List<String>> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split(DELIMITER);
                List<String> fields = new ArrayList<>();
                for (String part : parts) {
                    fields.add(part.trim());
                }
                records.add(fields);
            }
        } catch (IOException e) {
            System.err.println("Error reading " + filePath + ": " + e.getMessage());
        }
        return records;
    }

    // Reads only the header
    public static String readHeader(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            return reader.readLine();
        } catch (IOException e) {
            System.err.println("Error reading header from " + filePath + ": " + e.getMessage());
            return "";
        }
    }

    // Writes header and all records
    public static void writeData(String filePath, String header, List<List<String>> records) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println(header);
            for (List<String> row : records) {
                writer.println(String.join("|", row));
            }
        } catch (IOException e) {
            System.err.println("Error writing " + filePath + ": " + e.getMessage());
        }
    }

    // Appends a new data row
    public static void appendDataRow(String filePath, List<String> row) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath, true))) {
            writer.println(String.join("|", row));
        } catch (IOException e) {
            System.err.println("Error appending to " + filePath + ": " + e.getMessage());
        }
    }

    // Clears file and rewrites only the header
    public static void clearFile(String filePath, String header) {
        writeData(filePath, header, new ArrayList<>());
    }

    // UPDATE a row by index
    public static void updateDataRow(String filePath, int rowIndex, List<String> newRow) {
        List<List<String>> records = readData(filePath);
        if (rowIndex >= 0 && rowIndex < records.size()) {
            records.set(rowIndex, newRow);
            String header = readHeader(filePath);
            writeData(filePath, header, records);
        } else {
            System.err.println("Invalid row index for update: " + rowIndex);
        }
    }

    // DELETE a row by index
    public static void deleteDataRow(String filePath, int rowIndex) {
        List<List<String>> records = readData(filePath);
        if (rowIndex >= 0 && rowIndex < records.size()) {
            records.remove(rowIndex);
            String header = readHeader(filePath);
            writeData(filePath, header, records);
        } else {
            System.err.println("Invalid row index for delete: " + rowIndex);
        }
    }
}
