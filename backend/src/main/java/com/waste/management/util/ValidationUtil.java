package com.waste.management.util;

/**
 * Validation utility for input validation
 */
public class ValidationUtil {

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * Validate phone number (10 digits)
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }
        return phoneNumber.matches("^[0-9]{10}$");
    }

    /**
     * Validate employee ID format
     */
    public static boolean isValidEmployeeId(String employeeId) {
        if (employeeId == null || employeeId.isEmpty()) {
            return false;
        }
        return employeeId.matches("^[A-Z0-9]+$") && employeeId.length() <= 50;
    }

    /**
     * Validate full name
     */
    public static boolean isValidFullName(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            return false;
        }
        return fullName.matches("^[a-zA-Z\\s]{2,100}$");
    }

    /**
     * Validate file path
     */
    public static boolean isValidFilePath(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        return filePath.length() <= 255;
    }

    /**
     * Validate GPS coordinates
     */
    public static boolean isValidLatitude(Double latitude) {
        return latitude != null && latitude >= -90.0 && latitude <= 90.0;
    }

    /**
     * Validate GPS longitude
     */
    public static boolean isValidLongitude(Double longitude) {
        return longitude != null && longitude >= -180.0 && longitude <= 180.0;
    }

    /**
     * Validate weight
     */
    public static boolean isValidWeight(Double weight) {
        return weight != null && weight > 0;
    }

    /**
     * Validate face match confidence score
     */
    public static boolean isValidConfidenceScore(Double score) {
        return score != null && score >= 0.0 && score <= 1.0;
    }

    /**
     * Validate waste type
     */
    public static boolean isValidWasteType(String wasteType) {
        if (wasteType == null || wasteType.isEmpty()) {
            return false;
        }
        String[] validTypes = {"Organic", "Inorganic", "Hazardous", "Mixed", "Medical", "Electronic"};
        for (String type : validTypes) {
            if (type.equalsIgnoreCase(wasteType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sanitize input string
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        return input.trim().replaceAll("[^a-zA-Z0-9\\s@.\\-_]", "");
    }

    /**
     * Check if string is null or empty
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Validate data length
     */
    public static boolean isValidLength(String data, int minLength, int maxLength) {
        if (data == null) {
            return false;
        }
        int length = data.length();
        return length >= minLength && length <= maxLength;
    }
}
