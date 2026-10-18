package com.demo.transitfarecalculatorservice.helper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeHelper {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private DateTimeHelper() {
    }

    public static LocalDateTime parse(String text) {
        return LocalDateTime.parse(text.trim(), FORMAT);
    }

    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return FORMAT.format(dateTime);
    }
}
