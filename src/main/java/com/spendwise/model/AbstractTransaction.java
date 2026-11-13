package com.spendwise.model;

import com.spendwise.exception.ValidationException;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * OOP Pillars: ABSTRACTION, INHERITANCE & POLYMORPHISM
 * Abstract base class capturing transaction attributes and mandating polymorphic processing behavior.
 */
public abstract class AbstractTransaction extends BaseEntity {
    protected int userId;
    protected double amount;
    protected String category;
    protected String description;
    protected LocalDate transactionDate;

    public AbstractTransaction() {
        super();
        this.transactionDate = LocalDate.now();
    }

    public AbstractTransaction(int id, int userId, double amount, String category, String description,
                               LocalDate transactionDate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, createdAt, updatedAt);
        setUserId(userId);
        setAmount(amount);
        setCategory(category);
        this.description = description != null ? description.trim() : "";
        this.transactionDate = transactionDate != null ? transactionDate : LocalDate.now();
    }

    // ==========================================
    // POLYMORPHIC ABSTRACT METHODS
    // ==========================================

    /**
     * Identifies the transaction type.
     */
    public abstract TransactionType getType();

    /**
     * Polymorphic calculation of the transaction's net signed impact on balances.
     * Incomes return +amount; Expenses return -amount.
     */
    public abstract double getSignedAmount();

    /**
     * Polymorphic mutation of an account balance.
     * Dispatches appropriate credit or debit operation on the Account encapsulation.
     */
    public abstract void processAccount(Account account);

    // ==========================================
    // GETTERS & ENCAPSULATED SETTERS
    // ==========================================

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        if (userId <= 0) {
            throw new ValidationException("User ID must be a positive integer.");
        }
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        if (amount <= 0) {
            throw new ValidationException("Transaction amount must be strictly greater than zero.");
        }
        // Round to 2 decimal places for financial precision
        this.amount = Math.round(amount * 100.0) / 100.0;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new ValidationException("Transaction category cannot be empty.");
        }
        this.category = category.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : "";
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        if (transactionDate == null) {
            this.transactionDate = LocalDate.now();
        } else {
            this.transactionDate = transactionDate;
        }
    }
}
