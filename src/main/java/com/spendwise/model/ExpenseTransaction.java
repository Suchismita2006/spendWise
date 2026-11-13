package com.spendwise.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * OOP Pillar: INHERITANCE & POLYMORPHISM
 * Represents an outflow of capital. Implements polymorphic processing by debiting
 * funds from an account and providing negative signed amounts.
 */
public class ExpenseTransaction extends AbstractTransaction {

    public ExpenseTransaction() {
        super();
    }

    public ExpenseTransaction(int id, int userId, double amount, String category, String description,
                              LocalDate transactionDate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, userId, amount, category, description, transactionDate, createdAt, updatedAt);
    }

    public ExpenseTransaction(int userId, double amount, String category, String description, LocalDate transactionDate) {
        this(0, userId, amount, category, description, transactionDate, LocalDateTime.now(), LocalDateTime.now());
    }

    @Override
    public TransactionType getType() {
        return TransactionType.EXPENSE;
    }

    @Override
    public double getSignedAmount() {
        return -this.amount; // Expenses are negative deductions to net worth
    }

    @Override
    public void processAccount(Account account) {
        if (account != null) {
            account.withdraw(this.amount);
        }
    }

    @Override
    public String toString() {
        return "ExpenseTransaction{" +
                "id=" + id +
                ", userId=" + userId +
                ", amount=-" + amount +
                ", category='" + category + '\'' +
                ", date=" + transactionDate +
                '}';
    }
}
