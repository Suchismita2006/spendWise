package com.spendwise.service;

import com.spendwise.dao.ITransactionDAO;
import com.spendwise.dao.TransactionDAOImpl;
import com.spendwise.exception.ResourceNotFoundException;
import com.spendwise.model.*;
import com.spendwise.util.ValidationUtil;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service orchestrating transaction business logic and polymorphic domain dispatch.
 */
public class TransactionServiceImpl implements ITransactionService {
    private final ITransactionDAO transactionDAO;
    private final IBudgetService budgetService;

    public TransactionServiceImpl() {
        this(new TransactionDAOImpl(), new BudgetServiceImpl());
    }

    public TransactionServiceImpl(ITransactionDAO transactionDAO, IBudgetService budgetService) {
        this.transactionDAO = transactionDAO;
        this.budgetService = budgetService;
    }

    @Override
    public AbstractTransaction addTransaction(int userId, TransactionType type, String category,
                                             double amount, String description, LocalDate date) {
        ValidationUtil.validateAmount(amount);
        ValidationUtil.validateDate(date);

        // Factory pattern creates either IncomeTransaction or ExpenseTransaction polymorphically
        AbstractTransaction tx = TransactionFactory.create(type, 0, userId, amount, category, description,
                date, null, null);

        return transactionDAO.create(tx);
    }

    @Override
    public AbstractTransaction updateTransaction(int userId, int transactionId, TransactionType type,
                                                String category, double amount, String description, LocalDate date) {
        ValidationUtil.validateAmount(amount);
        ValidationUtil.validateDate(date);

        AbstractTransaction existing = getTransactionById(userId, transactionId);
        AbstractTransaction updated = TransactionFactory.create(type, transactionId, userId, amount, category,
                description, date, existing.getCreatedAt(), null);

        boolean ok = transactionDAO.update(updated);
        if (!ok) {
            throw new ResourceNotFoundException("Could not update transaction ID: " + transactionId);
        }
        return updated;
    }

    @Override
    public boolean deleteTransaction(int userId, int transactionId) {
        // Verify ownership first
        getTransactionById(userId, transactionId);
        return transactionDAO.delete(transactionId);
    }

    @Override
    public AbstractTransaction getTransactionById(int userId, int transactionId) {
        AbstractTransaction tx = transactionDAO.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction #" + transactionId + " not found."));

        if (tx.getUserId() != userId) {
            throw new ResourceNotFoundException("Transaction #" + transactionId + " not found for this account.");
        }
        return tx;
    }

    @Override
    public List<AbstractTransaction> getTransactions(int userId, TransactionType type, String category,
                                                    LocalDate start, LocalDate end, String search) {
        return transactionDAO.findFiltered(userId, type, category, start, end, search);
    }

    /**
     * OOP Pillar: POLYMORPHISM in action!
     * Calculates the net account balance by iterating over all transactions and invoking
     * polymorphic processAccount(account) dispatch on each instance without type-checking branching.
     */
    @Override
    public Account calculateAccountBalance(int userId) {
        Account account = new Account(userId);
        List<AbstractTransaction> allTx = transactionDAO.findByUserId(userId);

        for (AbstractTransaction tx : allTx) {
            // Polymorphic dispatch:
            // IncomeTransaction calls account.deposit(amount)
            // ExpenseTransaction calls account.withdraw(amount)
            tx.processAccount(account);
        }
        return account;
    }

    @Override
    public MonthlySummary getMonthlySummary(int userId, int month, int year) {
        ValidationUtil.validateMonthAndYear(month, year);
        MonthlySummary summary = new MonthlySummary(month, year);

        double totalIncome = transactionDAO.getTotalByTypeAndMonth(userId, TransactionType.INCOME, month, year);
        double totalExpense = transactionDAO.getTotalByTypeAndMonth(userId, TransactionType.EXPENSE, month, year);

        summary.setTotalIncome(totalIncome);
        summary.setTotalExpense(totalExpense);

        Map<String, Double> expenseBreakdown = transactionDAO.getCategoryTotals(userId, TransactionType.EXPENSE, month, year);
        expenseBreakdown.forEach(summary::addCategoryExpense);

        Map<String, Double> incomeBreakdown = transactionDAO.getCategoryTotals(userId, TransactionType.INCOME, month, year);
        incomeBreakdown.forEach(summary::addCategoryIncome);

        // Analyze budget health for the month
        List<Budget> budgets = budgetService.getBudgetsWithProgress(userId, month, year);
        int warningCount = 0;
        int exceededCount = 0;
        for (Budget b : budgets) {
            if (b.isExceeded()) {
                exceededCount++;
            } else if (b.isWarning()) {
                warningCount++;
            }
        }
        summary.setExceededBudgetsCount(exceededCount);
        summary.setWarningBudgetsCount(warningCount);

        return summary;
    }
}
