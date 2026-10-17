package com.demo.transitfarecalculatorservice.helper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeHelper {

    public static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private DateTimeHelper() {
    }

    public static LocalDateTime parse(String text) {
        return LocalDateTime.parse(text.trim(), FORMAT);
    }
}
