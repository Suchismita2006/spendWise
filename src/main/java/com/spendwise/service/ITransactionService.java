package com.spendwise.service;

import com.spendwise.model.AbstractTransaction;
import com.spendwise.model.Account;
import com.spendwise.model.MonthlySummary;
import com.spendwise.model.TransactionType;
import java.time.LocalDate;
import java.util.List;

/**
 * OOP Pillar: ABSTRACTION
 * Interface contract declaring business logic for transaction processing, balance computation, and summaries.
 */
public interface ITransactionService {
    AbstractTransaction addTransaction(int userId, TransactionType type, String category,
                                      double amount, String description, LocalDate date);
    AbstractTransaction updateTransaction(int userId, int transactionId, TransactionType type,
                                         String category, double amount, String description, LocalDate date);
    boolean deleteTransaction(int userId, int transactionId);
    AbstractTransaction getTransactionById(int userId, int transactionId);
    List<AbstractTransaction> getTransactions(int userId, TransactionType type, String category,
                                             LocalDate start, LocalDate end, String search);
    Account calculateAccountBalance(int userId);
    MonthlySummary getMonthlySummary(int userId, int month, int year);
}
