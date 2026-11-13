package com.spendwise.exception;

/**
 * Thrown or alerted when an expense exceeds the allocated monthly budget.
 */
public class BudgetExceededException extends SpendWiseException {
    private final String category;
    private final double monthlyLimit;
    private final double attemptedExpense;
    private final double currentSpent;

    public BudgetExceededException(String category, double monthlyLimit, double currentSpent, double attemptedExpense) {
        super(String.format("Budget exceeded for category '%s'! Limit: %.2f, Already spent: %.2f, Attempted: %.2f",
                category, monthlyLimit, currentSpent, attemptedExpense));
        this.category = category;
        this.monthlyLimit = monthlyLimit;
        this.currentSpent = currentSpent;
        this.attemptedExpense = attemptedExpense;
    }

    public String getCategory() {
        return category;
    }

    public double getMonthlyLimit() {
        return monthlyLimit;
    }

    public double getCurrentSpent() {
        return currentSpent;
    }

    public double getAttemptedExpense() {
        return attemptedExpense;
    }
}
