package com.spendwise;

import com.spendwise.model.Budget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates OOP Pillar: ENCAPSULATION & Business Rules
 * Confirms Budget domain logic, progress calculations, and threshold alerting.
 */
public class BudgetDomainTest {

    @Test
    @DisplayName("Budget Encapsulation: Calculates remaining budget and percentage correctly")
    void testBudgetCalculations() {
        Budget budget = new Budget(1, "Food & Dining", 10000.0, 11, 2026);
        budget.setCurrentSpent(4000.0);

        assertEquals(6000.0, budget.getRemainingBudget(), 0.001);
        assertEquals(40.0, budget.getPercentageUsed(), 0.001);
        assertEquals("SAFE", budget.getStatus());
        assertFalse(budget.isWarning());
        assertFalse(budget.isExceeded());
    }

    @Test
    @DisplayName("Budget Alerts: Triggers WARNING status when spending reaches or exceeds 80%")
    void testBudgetWarningAlert() {
        Budget budget = new Budget(1, "Entertainment", 5000.0, 11, 2026);
        budget.setCurrentSpent(4200.0); // 84%

        assertTrue(budget.isWarning());
        assertFalse(budget.isExceeded());
        assertEquals("WARNING", budget.getStatus());
    }

    @Test
    @DisplayName("Budget Alerts: Triggers EXCEEDED status when spending exceeds 100% of limit")
    void testBudgetExceededAlert() {
        Budget budget = new Budget(1, "Shopping", 8000.0, 11, 2026);
        budget.setCurrentSpent(8500.0); // > 100%

        assertTrue(budget.isExceeded());
        assertFalse(budget.isWarning());
        assertEquals("EXCEEDED", budget.getStatus());
        assertEquals(-500.0, budget.getRemainingBudget(), 0.001);
    }
}
