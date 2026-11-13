package com.spendwise.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Value Object / DTO capturing monthly financial aggregation and analytics.
 */
public class MonthlySummary {
    private final int month;
    private final int year;
    private double totalIncome;
    private double totalExpense;
    private final Map<String, Double> categoryExpenses;
    private final Map<String, Double> categoryIncomes;
    private int exceededBudgetsCount;
    private int warningBudgetsCount;

    public MonthlySummary(int month, int year) {
        this.month = month;
        this.year = year;
        this.totalIncome = 0.0;
        this.totalExpense = 0.0;
        this.categoryExpenses = new HashMap<>();
        this.categoryIncomes = new HashMap<>();
        this.exceededBudgetsCount = 0;
        this.warningBudgetsCount = 0;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public double getTotalIncome() {
        return Math.round(totalIncome * 100.0) / 100.0;
    }

    public void setTotalIncome(double totalIncome) {
        this.totalIncome = totalIncome;
    }

    public double getTotalExpense() {
        return Math.round(totalExpense * 100.0) / 100.0;
    }

    public void setTotalExpense(double totalExpense) {
        this.totalExpense = totalExpense;
    }

    public double getNetSavings() {
        return Math.round((totalIncome - totalExpense) * 100.0) / 100.0;
    }

    public double getSavingsRate() {
        if (totalIncome <= 0) return 0.0;
        double rate = ((totalIncome - totalExpense) / totalIncome) * 100.0;
        return Math.round(rate * 10.0) / 10.0;
    }

    public Map<String, Double> getCategoryExpenses() {
        return Collections.unmodifiableMap(categoryExpenses);
    }

    public void addCategoryExpense(String category, double amount) {
        categoryExpenses.put(category, categoryExpenses.getOrDefault(category, 0.0) + amount);
    }

    public Map<String, Double> getCategoryIncomes() {
        return Collections.unmodifiableMap(categoryIncomes);
    }

    public void addCategoryIncome(String category, double amount) {
        categoryIncomes.put(category, categoryIncomes.getOrDefault(category, 0.0) + amount);
    }

    public int getExceededBudgetsCount() {
        return exceededBudgetsCount;
    }

    public void setExceededBudgetsCount(int exceededBudgetsCount) {
        this.exceededBudgetsCount = exceededBudgetsCount;
    }

    public int getWarningBudgetsCount() {
        return warningBudgetsCount;
    }

    public void setWarningBudgetsCount(int warningBudgetsCount) {
        this.warningBudgetsCount = warningBudgetsCount;
    }
}
