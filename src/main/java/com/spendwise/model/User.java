package com.spendwise.model;

import com.spendwise.exception.ValidationException;
import java.time.LocalDateTime;

/**
 * OOP Pillar: ENCAPSULATION & INHERITANCE
 * Encapsulates confidential user credentials, cryptographic salt, and preferences.
 */
public class User extends BaseUser {
    private String passwordHash;
    private String salt;
    private String currency = "INR";

    public User() {
        super();
    }

    public User(int id, String fullName, String email, String passwordHash, String salt, String currency,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, fullName, email, createdAt, updatedAt);
        this.passwordHash = passwordHash;
        this.salt = salt;
        setCurrency(currency);
    }

    public User(String fullName, String email, String passwordHash, String salt) {
        super(0, fullName, email, LocalDateTime.now(), LocalDateTime.now());
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.currency = "INR";
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.trim().isEmpty()) {
            throw new ValidationException("Password hash cannot be empty.");
        }
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        if (salt == null || salt.trim().isEmpty()) {
            throw new ValidationException("Salt cannot be empty.");
        }
        this.salt = salt;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        if (currency == null || currency.trim().isEmpty()) {
            this.currency = "INR";
        } else {
            this.currency = currency.trim().toUpperCase();
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", currency='" + currency + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
