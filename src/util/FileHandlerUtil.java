package util;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileHandlerUtil {
    private static final String DELIMITER = "\\|";
    private static final String SEPERATOR = "|";

    // Reads data lines (excluding header) and splits them
    public static List<List<String>> readData(String filePath) {
        try (Stream<String> lines = Files.lines(Path.of(filePath))) {
            return lines
                    .skip(1) // skip header
                    .map(line -> Arrays.stream(line.trim().split(DELIMITER))
                            .map(String::trim)
                            .collect(Collectors.toList()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Error reading " + filePath + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Reads only the header
    public static String readHeader(String filePath) {
        try (Stream<String> lines = Files.lines(Path.of(filePath))) {
            return lines.findFirst().orElse("");
        } catch (IOException e) {
            System.err.println("Error reading header from " + filePath + ": " + e.getMessage());
            return "";
        }
    }

    // Writes header and all records
    public static void writeData(String filePath, String header, List<List<String>> records) {
        List<String> lines = new ArrayList<>();
        lines.add(header);
        for (List<String> row : records) {
            lines.add(String.join(SEPERATOR, row));
        }
        try {
            Files.write(Path.of(filePath), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error writing " + filePath + ": " + e.getMessage());
        }
    }

    // Appends a new data row
    public static void appendDataRow(String filePath, List<String> row) {
        String line = String.join(SEPERATOR, row);
        try {
            Files.write(Path.of(filePath), Collections.singletonList(line), StandardOpenOption.APPEND);
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
