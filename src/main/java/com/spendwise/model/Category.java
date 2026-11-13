package com.spendwise.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Standard income and expense categories.
 */
public class Category {
    public static final List<String> INCOME_CATEGORIES = Collections.unmodifiableList(Arrays.asList(
            "Salary",
            "Freelance",
            "Business",
            "Investments",
            "Rental",
            "Gifts & Grants",
            "Refunds",
            "Other Income"
    ));

    public static final List<String> EXPENSE_CATEGORIES = Collections.unmodifiableList(Arrays.asList(
            "Food & Dining",
            "Rent & Housing",
            "Utilities",
            "Transportation",
            "Healthcare & Medical",
            "Entertainment & Leisure",
            "Shopping & Apparel",
            "Education",
            "Personal Care",
            "Debt & Loans",
            "Investments & Savings",
            "Other Expense"
    ));

    public static boolean isValidIncomeCategory(String category) {
        if (category == null) return false;
        return INCOME_CATEGORIES.stream().anyMatch(c -> c.equalsIgnoreCase(category.trim()));
    }

    public static boolean isValidExpenseCategory(String category) {
        if (category == null) return false;
        return EXPENSE_CATEGORIES.stream().anyMatch(c -> c.equalsIgnoreCase(category.trim()));
    }
}
