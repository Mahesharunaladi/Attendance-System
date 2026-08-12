package com.waste.management.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for common operations
 */
public class AppUtil {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Format LocalDateTime to string
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_FORMATTER) : "N/A";
    }

    /**
     * Calculate attendance percentage
     */
    public static double calculateAttendancePercentage(long present, long total) {
        return total > 0 ? (present * 100.0 / total) : 0.0;
    }

    /**
     * Calculate task completion percentage
     */
    public static double calculateCompletionPercentage(long completed, long total) {
        return total > 0 ? (completed * 100.0 / total) : 0.0;
    }

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email != null && email.matches(emailRegex);
    }

    /**
     * Validate phone number format
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^[0-9]{10}$");
    }

    /**
     * Generate unique ID
     */
    public static String generateUniqueId(String prefix) {
        return prefix + "_" + System.currentTimeMillis();
    }
}
