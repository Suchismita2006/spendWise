package com.spendwise.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * OOP Pillar: INHERITANCE & POLYMORPHISM
 * Represents an inflow of capital. Implements polymorphic processing by depositing
 * funds into an account and providing positive signed amounts.
 */
public class IncomeTransaction extends AbstractTransaction {

    public IncomeTransaction() {
        super();
    }

    public IncomeTransaction(int id, int userId, double amount, String category, String description,
                             LocalDate transactionDate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, userId, amount, category, description, transactionDate, createdAt, updatedAt);
    }

    public IncomeTransaction(int userId, double amount, String category, String description, LocalDate transactionDate) {
        this(0, userId, amount, category, description, transactionDate, LocalDateTime.now(), LocalDateTime.now());
    }

    @Override
    public TransactionType getType() {
        return TransactionType.INCOME;
    }

    @Override
    public double getSignedAmount() {
        return this.amount; // Incomes are positive additions to net worth
    }

    @Override
    public void processAccount(Account account) {
        if (account != null) {
            account.deposit(this.amount);
        }
    }

    @Override
    public String toString() {
        return "IncomeTransaction{" +
                "id=" + id +
                ", userId=" + userId +
                ", amount=+" + amount +
                ", category='" + category + '\'' +
                ", date=" + transactionDate +
                '}';
    }
}
