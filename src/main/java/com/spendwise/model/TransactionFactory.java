package com.spendwise.model;

import com.spendwise.exception.ValidationException;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * OOP / Design Pattern: FACTORY PATTERN & ABSTRACTION
 * Decouples client callers from concrete transaction instantiation.
 */
public class TransactionFactory {

    public static AbstractTransaction create(TransactionType type, int id, int userId, double amount,
                                             String category, String description, LocalDate date,
                                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        if (type == null) {
            throw new ValidationException("Transaction type cannot be null.");
        }

        switch (type) {
            case INCOME:
                return new IncomeTransaction(id, userId, amount, category, description, date, createdAt, updatedAt);
            case EXPENSE:
                return new ExpenseTransaction(id, userId, amount, category, description, date, createdAt, updatedAt);
            default:
                throw new ValidationException("Unsupported transaction type: " + type);
        }
    }

    public static AbstractTransaction create(String typeStr, int userId, double amount, String category,
                                             String description, LocalDate date) {
        TransactionType type = TransactionType.fromString(typeStr);
        return create(type, 0, userId, amount, category, description, date, LocalDateTime.now(), LocalDateTime.now());
    }
}
