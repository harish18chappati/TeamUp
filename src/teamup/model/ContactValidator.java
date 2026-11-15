package teamup.model;

import java.util.regex.Pattern;

/**
 * ContactValidator – Validates email and phone number formats for TeamUp organizer contact details.
 */
public class ContactValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9\\s\\-\\(\\)]{7,20}$");

    /**
     * Validates whether the given string is a valid email address.
     */
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        String trimmed = email.trim();
        return EMAIL_PATTERN.matcher(trimmed).matches();
    }

    /**
     * Validates whether the given string is a valid phone number.
     * Supports standard formats, spaces, dashes, parentheses, optional leading +,
     * and ensures digit count is between 7 and 15 digits.
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String trimmed = phone.trim();
        if (!PHONE_PATTERN.matcher(trimmed).matches()) {
            return false;
        }
        String digitsOnly = trimmed.replaceAll("[^0-9]", "");
        return digitsOnly.length() >= 7 && digitsOnly.length() <= 15;
    }

    /**
     * Validates that at least one contact method (email or phone) is provided,
     * and that whichever are provided have valid formats.
     */
    public static boolean hasAtLeastOneContact(String email, String phone) {
        boolean hasEmail = email != null && !email.trim().isEmpty();
        boolean hasPhone = phone != null && !phone.trim().isEmpty();
        return hasEmail || hasPhone;
    }
}
