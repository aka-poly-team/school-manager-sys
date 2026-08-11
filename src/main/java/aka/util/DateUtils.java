package aka.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utility library for Date formatting and operations.
 */
public class DateUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static String today() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    public static String formatToday() {
        return today();
    }

    public static String format(LocalDate date) {
        return (date != null) ? date.format(DATE_FORMATTER) : "";
    }

    public static String formatDate(LocalDate date) {
        return format(date);
    }

    public static String dayText(Integer day) {
        if (day == null) return "Chưa xác định";
        return switch (day) {
            case 2 -> "Thứ 2";
            case 3 -> "Thứ 3";
            case 4 -> "Thứ 4";
            case 5 -> "Thứ 5";
            case 6 -> "Thứ 6";
            case 7 -> "Thứ 7";
            case 8 -> "Chủ nhật";
            default -> "Thứ " + day;
        };
    }

    public static String getDayOfWeekText(Integer day) {
        return dayText(day);
    }

    /**
     * Convert LocalDate to custom DayOfWeek number (Monday=2, ..., Sunday=8).
     */
    public static int toCustomDayOfWeek(LocalDate date) {
        if (date == null) return 2;
        int dow = date.getDayOfWeek().getValue();
        return (dow == 7) ? 8 : dow + 1;
    }
}
