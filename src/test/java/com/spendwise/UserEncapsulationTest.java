package com.spendwise;

import com.spendwise.exception.ValidationException;
import com.spendwise.model.User;
import com.spendwise.util.PasswordUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates OOP Pillar: ENCAPSULATION & INHERITANCE
 * Confirms user profile boundaries, validation invariants, and cryptographic hashing.
 */
public class UserEncapsulationTest {

    @Test
    @DisplayName("OOP: User correctly encapsulates valid profile and inherits BaseUser fields")
    void testValidUserCreation() {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword("secret123", salt);

        User user = new User("Suchismita Saha", "suchismita@test.com", hash, salt);

        assertEquals("Suchismita Saha", user.getFullName());
        assertEquals("suchismita@test.com", user.getEmail());
        assertEquals(hash, user.getPasswordHash());
        assertEquals(salt, user.getSalt());
        assertEquals("INR", user.getCurrency());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    @DisplayName("OOP Encapsulation: Invalid email format throws ValidationException")
    void testInvalidEmailThrowsException() {
        assertThrows(ValidationException.class, () -> {
            new User("Invalid User", "notanemail", "hash", "salt");
        });
    }

    @Test
    @DisplayName("OOP Encapsulation: Empty name throws ValidationException")
    void testEmptyNameThrowsException() {
        assertThrows(ValidationException.class, () -> {
            new User("  ", "valid@email.com", "hash", "salt");
        });
    }

    @Test
    @DisplayName("Security: Password hashing with salt verifies correctly and rejects wrong passwords")
    void testPasswordHashVerification() {
        String password = "MySecurePassword2026!";
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(password, salt);

        assertTrue(PasswordUtil.verifyPassword(password, salt, hash));
        assertFalse(PasswordUtil.verifyPassword("WrongPassword", salt, hash));
    }
}
