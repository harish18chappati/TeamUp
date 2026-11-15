package teamup.model;

import java.util.regex.Pattern;

/**
 * StudentValidator – Validation rules for the Student Welcome & Identification screen.
 *
 * Rules:
 *   1. Full Name: Required, 2-60 characters, letters, spaces, dots, hyphens, apostrophes.
 *   2. SRM Registration Number: Required, follows standard SRM University format
 *      (e.g., "AP21110010001" or other standard SRM campus formats).
 *
 * Decoupled from JavaFX UI classes so it can run cleanly in any test environment.
 */
public class StudentValidator {

    // SRM registration pattern:
    // Standard SRM-AP: "AP" followed by 11 digits (e.g. AP21110010001)
    // Also accepts standard SRM university "RA" format (e.g. RA2111003010123)
    private static final Pattern SRM_REG_PATTERN = Pattern.compile(
            "^(AP\\d{11}|RA\\d{11,13})$",
            Pattern.CASE_INSENSITIVE
    );

    // Full name pattern: letters, spaces, dots, hyphens, and apostrophes
    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^[a-zA-Z][a-zA-Z\\s.'-]{1,59}$"
    );

    /**
     * Checks if the student name is non-empty and contains valid characters.
     */
    public static boolean isValidName(String name) {
        if (name == null) return false;
        String trimmed = name.trim();
        return trimmed.length() >= 2 && trimmed.length() <= 60 && NAME_PATTERN.matcher(trimmed).matches();
    }

    /**
     * Checks if the registration number follows standard SRM format.
     */
    public static boolean isValidRegistrationNumber(String regNumber) {
        if (regNumber == null) return false;
        String trimmed = regNumber.trim();
        return SRM_REG_PATTERN.matcher(trimmed).matches();
    }

    /**
     * Validates both fields and returns a user-facing error message, or null if valid.
     */
    public static String validate(String name, String regNumber) {
        if (name == null || name.trim().isEmpty()) {
            return "Full Name is required. Please enter your name.";
        }
        if (!isValidName(name)) {
            return "Please enter a valid full name (at least 2 letters, no numbers or special symbols).";
        }
        if (regNumber == null || regNumber.trim().isEmpty()) {
            return "SRM Registration Number is required.";
        }
        if (!isValidRegistrationNumber(regNumber)) {
            return "Invalid Registration Number format. Expected SRM format (e.g. AP21110010001).";
        }
        return null;
    }

    /**
     * Normalizes the registration number to trimmed uppercase.
     */
    public static String normalizeRegNumber(String regNumber) {
        return regNumber != null ? regNumber.trim().toUpperCase() : "";
    }

    /**
     * Normalizes the full name to trimmed title-cased or cleaned spaces.
     */
    public static String normalizeName(String name) {
        if (name == null) return "";
        return name.trim().replaceAll("\\s+", " ");
    }
}
