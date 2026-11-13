package com.spendwise.service;

import com.spendwise.dao.BudgetDAOImpl;
import com.spendwise.dao.IBudgetDAO;
import com.spendwise.dao.ITransactionDAO;
import com.spendwise.dao.TransactionDAOImpl;
import com.spendwise.model.Budget;
import com.spendwise.model.TransactionType;
import com.spendwise.util.ValidationUtil;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service managing budget thresholds, consumption calculations, and alerting.
 */
public class BudgetServiceImpl implements IBudgetService {
    private final IBudgetDAO budgetDAO;
    private final ITransactionDAO transactionDAO;

    public BudgetServiceImpl() {
        this(new BudgetDAOImpl(), new TransactionDAOImpl());
    }

    public BudgetServiceImpl(IBudgetDAO budgetDAO, ITransactionDAO transactionDAO) {
        this.budgetDAO = budgetDAO;
        this.transactionDAO = transactionDAO;
    }

    @Override
    public Budget setBudget(int userId, String category, double monthlyLimit, int month, int year) {
        ValidationUtil.validateAmount(monthlyLimit);
        ValidationUtil.validateMonthAndYear(month, year);

        Budget budget = new Budget(userId, category, monthlyLimit, month, year);
        Budget saved = budgetDAO.upsert(budget);

        // Attach current spent calculation
        double spent = getSpentForCategory(userId, category, month, year);
        saved.setCurrentSpent(spent);
        return saved;
    }

    @Override
    public List<Budget> getBudgetsWithProgress(int userId, int month, int year) {
        ValidationUtil.validateMonthAndYear(month, year);
        List<Budget> budgets = budgetDAO.findByUserAndPeriod(userId, month, year);
        Map<String, Double> expenses = transactionDAO.getCategoryTotals(userId, TransactionType.EXPENSE, month, year);

        for (Budget b : budgets) {
            double spent = 0.0;
            // Case-insensitive category lookup
            for (Map.Entry<String, Double> entry : expenses.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(b.getCategory())) {
                    spent += entry.getValue();
                }
            }
            b.setCurrentSpent(spent);
        }
        return budgets;
    }

    @Override
    public Optional<Budget> getBudget(int userId, String category, int month, int year) {
        Optional<Budget> opt = budgetDAO.findByUserCategoryPeriod(userId, category, month, year);
        opt.ifPresent(b -> b.setCurrentSpent(getSpentForCategory(userId, category, month, year)));
        return opt;
    }

    @Override
    public boolean deleteBudget(int budgetId) {
        return budgetDAO.delete(budgetId);
    }

    private double getSpentForCategory(int userId, String category, int month, int year) {
        Map<String, Double> totals = transactionDAO.getCategoryTotals(userId, TransactionType.EXPENSE, month, year);
        for (Map.Entry<String, Double> entry : totals.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(category.trim())) {
                return entry.getValue();
            }
        }
        return 0.0;
    }
}
