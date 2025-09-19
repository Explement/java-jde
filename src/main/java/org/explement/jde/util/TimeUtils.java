package org.explement.jde.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtils {

    private TimeUtils() {} // Create private constructor

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss"); // HH - 24-hour format

    public static String now() { // Return current time formatted with FORMATTER
        return LocalDateTime.now().format(FORMATTER);
    }
}
