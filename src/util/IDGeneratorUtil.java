package util;

import java.io.*;
import java.nio.file.*;
import java.util.stream.*;

public class IDGeneratorUtil {
    // Assumes ID is the first column in each line (after header)
    public static int generateId(String filePath) {
        try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
            return lines
                    .skip(1) // skip header
                    .map(line -> line.split("\\|")[0]) // get ID column
                    .filter(id -> id.matches("\\d+")) // ensure it's numeric
                    .mapToInt(id -> Integer.parseInt(id))
                    .max()
                    .orElse(0) + 1;
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate ID from " + filePath, e);
        }
    }
}
