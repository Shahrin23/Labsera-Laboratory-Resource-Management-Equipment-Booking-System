package com.labresa.ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Consistent, readable date/time formatting used across the whole UI. */
public final class DateTimeFormat {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm");

    private DateTimeFormat() { }

    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(FORMATTER);
    }
}
