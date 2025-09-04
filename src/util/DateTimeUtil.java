package util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {
    // Formatter for date (YYYY-MM-DD)
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Formatter for date and time (YYYY-MM-DD'T'HH:mm)
    public static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    // Parse string into LocalDate using DATE_FORMAT
    public static LocalDate parseDate(String str) {
        return LocalDate.parse(str.trim(), DATE_FORMAT);
    }

    // Parse string into LocalDateTime using DATETIME_FORMAT
    public static LocalDateTime parseDateTime(String str) {
        return LocalDateTime.parse(str.trim(), DATETIME_FORMAT);
    }

    // Format LocalDate into string (yyyy-MM-dd)
    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMAT);
    }

    // Format LocalDateTime into string (yyyy-MM-dd'T'HH:mm)
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATETIME_FORMAT);
    }
}
