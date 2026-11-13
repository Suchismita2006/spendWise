package com.spendwise.model;

import com.spendwise.exception.ValidationException;
import java.time.LocalDateTime;

/**
 * OOP Pillar: ENCAPSULATION
 * Encapsulates spending boundaries for a category within a given month/year cycle.
 * Provides internal calculation logic for remaining budget, consumption percentage, and health alerts.
 */
public class Budget extends BaseEntity {
    private int userId;
    private String category;
    private double monthlyLimit;
    private double currentSpent;
    private int month; // 1 - 12
    private int year;

    public Budget() {
        super();
    }

    public Budget(int id, int userId, String category, double monthlyLimit, int month, int year,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, createdAt, updatedAt);
        setUserId(userId);
        setCategory(category);
        setMonthlyLimit(monthlyLimit);
        setMonth(month);
        setYear(year);
        this.currentSpent = 0.0;
    }

    public Budget(int userId, String category, double monthlyLimit, int month, int year) {
        this(0, userId, category, monthlyLimit, month, year, LocalDateTime.now(), LocalDateTime.now());
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        if (userId <= 0) {
            throw new ValidationException("User ID must be positive.");
        }
        this.userId = userId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new ValidationException("Category cannot be empty.");
        }
        this.category = category.trim();
    }

    public double getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(double monthlyLimit) {
        if (monthlyLimit <= 0) {
            throw new ValidationException("Budget limit must be strictly greater than 0.");
        }
        this.monthlyLimit = Math.round(monthlyLimit * 100.0) / 100.0;
    }

    public double getCurrentSpent() {
        return currentSpent;
    }

    public void setCurrentSpent(double currentSpent) {
        this.currentSpent = Math.max(0.0, Math.round(currentSpent * 100.0) / 100.0);
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        if (month < 1 || month > 12) {
            throw new ValidationException("Month must be between 1 and 12.");
        }
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        if (year < 2000 || year > 2100) {
            throw new ValidationException("Year must be a realistic calendar year between 2000 and 2100.");
        }
        this.year = year;
    }

    // ==========================================
    // DOMAIN HELPER METHODS
    // ==========================================

    public double getRemainingBudget() {
        return Math.round((monthlyLimit - currentSpent) * 100.0) / 100.0;
    }

    public double getPercentageUsed() {
        if (monthlyLimit <= 0) return 0.0;
        double pct = (currentSpent / monthlyLimit) * 100.0;
        return Math.round(pct * 10.0) / 10.0;
    }

    public boolean isWarning() {
        return getPercentageUsed() >= 80.0 && !isExceeded();
    }

    public boolean isExceeded() {
        return currentSpent > monthlyLimit;
    }

    public String getStatus() {
        if (isExceeded()) return "EXCEEDED";
        if (isWarning()) return "WARNING";
        return "SAFE";
    }

    @Override
    public String toString() {
        return "Budget{" +
                "id=" + id +
                ", userId=" + userId +
                ", category='" + category + '\'' +
                ", monthlyLimit=" + monthlyLimit +
                ", currentSpent=" + currentSpent +
                ", month=" + month +
                ", year=" + year +
                ", status=" + getStatus() +
                '}';
    }
}
