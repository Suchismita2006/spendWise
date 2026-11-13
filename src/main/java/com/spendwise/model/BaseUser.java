package com.spendwise.model;

import com.spendwise.exception.ValidationException;
import java.time.LocalDateTime;

/**
 * OOP Pillar: INHERITANCE
 * Reusable user base class encapsulating standard profile attributes and invariant checks.
 */
public abstract class BaseUser extends BaseEntity {
    protected String fullName;
    protected String email;

    public BaseUser() {
        super();
    }

    public BaseUser(int id, String fullName, String email, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, createdAt, updatedAt);
        setFullName(fullName);
        setEmail(email);
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("Full name cannot be null or empty.");
        }
        this.fullName = fullName.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ValidationException("Invalid email format: " + email);
        }
        this.email = email.trim().toLowerCase();
    }
}
