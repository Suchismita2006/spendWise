package com.spendwise;

import com.spendwise.exception.ValidationException;
import com.spendwise.util.ValidationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates Exception Handling and Input Sanitation constraints.
 */
public class ValidationUtilTest {

    @Test
    @DisplayName("Validation: Valid email passes and invalid emails throw ValidationException")
    void testEmailValidation() {
        assertDoesNotThrow(() -> ValidationUtil.validateEmail("user@example.com"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateEmail("plainaddress"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateEmail("missing@domain"));
    }

    @Test
    @DisplayName("Validation: Negative or zero amounts throw ValidationException")
    void testAmountValidation() {
        assertDoesNotThrow(() -> ValidationUtil.validateAmount(100.50));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateAmount(0.0));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateAmount(-50.0));
    }

    @Test
    @DisplayName("Validation: Month and Year bounds validation")
    void testMonthAndYearValidation() {
        assertDoesNotThrow(() -> ValidationUtil.validateMonthAndYear(12, 2026));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateMonthAndYear(13, 2026));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateMonthAndYear(0, 2026));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateMonthAndYear(5, 1990));
    }

    @Test
    @DisplayName("Validation: Null date throws ValidationException")
    void testDateValidation() {
        assertDoesNotThrow(() -> ValidationUtil.validateDate(LocalDate.now()));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateDate(null));
    }
}
