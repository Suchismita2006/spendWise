package com.spendwise;

import com.spendwise.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates OOP Pillars: POLYMORPHISM, ABSTRACTION & INHERITANCE
 * Confirms polymorphic transaction dispatch on financial accounts.
 */
public class TransactionPolymorphismTest {

    @Test
    @DisplayName("OOP Polymorphism: IncomeTransaction returns positive signed amount")
    void testIncomeSignedAmount() {
        AbstractTransaction income = new IncomeTransaction(1, 5000.0, "Salary", "Monthly Salary", LocalDate.now());
        assertEquals(5000.0, income.getSignedAmount(), 0.001);
        assertEquals(TransactionType.INCOME, income.getType());
    }

    @Test
    @DisplayName("OOP Polymorphism: ExpenseTransaction returns negative signed amount")
    void testExpenseSignedAmount() {
        AbstractTransaction expense = new ExpenseTransaction(1, 1200.0, "Food & Dining", "Groceries", LocalDate.now());
        assertEquals(-1200.0, expense.getSignedAmount(), 0.001);
        assertEquals(TransactionType.EXPENSE, expense.getType());
    }

    @Test
    @DisplayName("OOP Polymorphism: processAccount polymorphically updates Account balance without type checks")
    void testPolymorphicAccountProcessing() {
        Account account = new Account(1, 10000.0, "INR");

        List<AbstractTransaction> transactions = new ArrayList<>();
        transactions.add(new IncomeTransaction(1, 25000.0, "Salary", "Bonus", LocalDate.now()));
        transactions.add(new ExpenseTransaction(1, 5000.0, "Rent & Housing", "Apartment", LocalDate.now()));
        transactions.add(new ExpenseTransaction(1, 2000.0, "Utilities", "Electricity", LocalDate.now()));

        // Polymorphic iteration: each transaction processes account state according to its subtype
        for (AbstractTransaction tx : transactions) {
            tx.processAccount(account);
        }

        // Expected: 10000 (initial) + 25000 (income) - 5000 (expense) - 2000 (expense) = 28000
        assertEquals(28000.0, account.getBalance(), 0.001);
    }

    @Test
    @DisplayName("OOP Abstraction: TransactionFactory produces correct concrete subtype")
    void testTransactionFactory() {
        AbstractTransaction inc = TransactionFactory.create("INCOME", 1, 1500.0, "Freelance", "Project", LocalDate.now());
        assertTrue(inc instanceof IncomeTransaction);

        AbstractTransaction exp = TransactionFactory.create("EXPENSE", 1, 300.0, "Coffee", "Cafe", LocalDate.now());
        assertTrue(exp instanceof ExpenseTransaction);
    }
}
