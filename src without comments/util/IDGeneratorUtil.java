package util;

import java.io.*;
import java.nio.file.*;
import java.util.stream.*;

public class IDGeneratorUtil {

    public static int generateId(String filePath) {
        try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
            return lines
                    .skip(1)
                    .map(line -> line.split("\\|")[0])
                    .filter(id -> id.matches("\\d+"))
                    .mapToInt(id -> Integer.parseInt(id))
                    .max()
                    .orElse(0) + 1;
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate ID from " + filePath, e);
        }
    }
}
