package com.example.emptrack.util;

public class ValidationUtil {

    // Allows letters (including accented like ñ, é), spaces, hyphens, apostrophes
    // Min 2, Max 50 characters
    private static final String NAME_REGEX = "^[a-zA-ZÀ-ÿ' \\-]{2,50}$";

    // Letters, numbers, underscores, dots — no spaces
    // Min 3, Max 30 characters
    private static final String USERNAME_REGEX = "^[a-zA-Z0-9_.]{3,30}$";

    public static void validateName(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        if (!value.matches(NAME_REGEX)) {
            throw new IllegalArgumentException(
                    fieldName + " can only contain letters, spaces, hyphens, and apostrophes (2–50 characters).");
        }
    }

    public static void validateUsername(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (!value.matches(USERNAME_REGEX)) {
            throw new IllegalArgumentException(
                    "Username can only contain letters, numbers, underscores, and dots (3–30 characters, no spaces).");
        }
    }

    public static void validateDepartmentName(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Department name is required.");
        }
        if (value.trim().length() > 100) {
            throw new IllegalArgumentException("Department name cannot exceed 100 characters.");
        }
    }
}