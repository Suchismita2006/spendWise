package com.spendwise.util;

import com.spendwise.exception.ValidationException;
import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Robust input validation utility enforcing format constraints across the system.
 */
public class ValidationUtil {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,63}$"
    );

    public static void validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Please provide a valid email address (e.g. user@example.com).");
        }
    }

    public static void validatePasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters long.");
        }
    }

    public static void validateName(String name) {
        if (name == null || name.trim().length() < 2) {
            throw new ValidationException("Full name must contain at least 2 characters.");
        }
    }

    public static void validateAmount(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0.0) {
            throw new ValidationException("Amount must be a positive number greater than 0.");
        }
        if (amount > 1_000_000_000.0) {
            throw new ValidationException("Amount exceeds system threshold (1,000,000,000).");
        }
    }

    public static void validateDate(LocalDate date) {
        if (date == null) {
            throw new ValidationException("Date cannot be null.");
        }
        LocalDate minDate = LocalDate.of(2000, 1, 1);
        LocalDate maxDate = LocalDate.now().plusYears(1);
        if (date.isBefore(minDate) || date.isAfter(maxDate)) {
            throw new ValidationException("Transaction date must be between 2000-01-01 and " + maxDate);
        }
    }

    public static void validateMonthAndYear(int month, int year) {
        if (month < 1 || month > 12) {
            throw new ValidationException("Month must be between 1 and 12.");
        }
        if (year < 2000 || year > 2100) {
            throw new ValidationException("Year must be between 2000 and 2100.");
        }
    }
}
