package com.spendwise.service;

import com.spendwise.model.Budget;
import java.util.List;
import java.util.Optional;

/**
 * OOP Pillar: ABSTRACTION
 * Interface contract declaring budget management operations and consumption tracking.
 */
public interface IBudgetService {
    Budget setBudget(int userId, String category, double monthlyLimit, int month, int year);
    List<Budget> getBudgetsWithProgress(int userId, int month, int year);
    Optional<Budget> getBudget(int userId, String category, int month, int year);
    boolean deleteBudget(int budgetId);
}
