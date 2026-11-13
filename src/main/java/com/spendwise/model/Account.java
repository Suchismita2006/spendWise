package com.spendwise.model;

import com.spendwise.exception.ValidationException;

/**
 * OOP Pillar: ENCAPSULATION
 * Encapsulates financial balance state. Balance mutations are strictly regulated
 * through deposit and withdraw domain operations rather than arbitrary setters.
 */
public class Account {
    private final int userId;
    private double balance;
    private String currency;

    public Account(int userId, double initialBalance, String currency) {
        if (userId <= 0) {
            throw new ValidationException("Invalid user ID for account.");
        }
        this.userId = userId;
        this.balance = initialBalance;
        this.currency = (currency != null && !currency.trim().isEmpty()) ? currency.trim().toUpperCase() : "INR";
    }

    public Account(int userId) {
        this(userId, 0.0, "INR");
    }

    public int getUserId() {
        return userId;
    }

    public double getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        if (currency != null && !currency.trim().isEmpty()) {
            this.currency = currency.trim().toUpperCase();
        }
    }

    /**
     * Credit funds to the account.
     */
    public synchronized void deposit(double amount) {
        if (amount <= 0) {
            throw new ValidationException("Deposit amount must be strictly greater than 0.");
        }
        this.balance += amount;
    }

    /**
     * Debit funds from the account.
     */
    public synchronized void withdraw(double amount) {
        if (amount <= 0) {
            throw new ValidationException("Withdrawal amount must be strictly greater than 0.");
        }
        this.balance -= amount;
    }

    @Override
    public String toString() {
        return "Account{" +
                "userId=" + userId +
                ", balance=" + balance +
                ", currency='" + currency + '\'' +
                '}';
    }
}
