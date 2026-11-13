package com.spendwise;

import com.spendwise.db.DatabaseInitializer;
import com.spendwise.model.TransactionType;
import com.spendwise.model.User;
import com.spendwise.service.BudgetServiceImpl;
import com.spendwise.service.IBudgetService;
import com.spendwise.service.ITransactionService;
import com.spendwise.service.IUserService;
import com.spendwise.service.TransactionServiceImpl;
import com.spendwise.service.UserServiceImpl;
import com.spendwise.web.HttpServerApp;
import java.time.LocalDate;

/**
 * SpendWise Application Entry Point.
 * Initialises MySQL Database tables, verifies connectivity, seeds sample data,
 * and launches the embedded HTTP server powering the HTML/CSS/JavaScript client.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("   SpendWise – Personal Expense Management System (Java Core/OOP)");
        System.out.println("==================================================================");

        // 1. Initialize Database Schema
        try {
            DatabaseInitializer.initializeDatabase();
        } catch (Exception e) {
            System.err.println("[WARN] Database init note: " + e.getMessage());
        }

        // 2. Seed default demo data if needed
        seedInitialDataIfEmpty();

        // 3. Start Web Server
        try {
            HttpServerApp app = new HttpServerApp();
            app.start();
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to start SpendWise server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedInitialDataIfEmpty() {
        try {
            IUserService userService = new UserServiceImpl();
            ITransactionService transactionService = new TransactionServiceImpl();
            IBudgetService budgetService = new BudgetServiceImpl();

            User demoUser = null;
            try {
                demoUser = userService.login("demo@spendwise.com", "password123");
            } catch (Exception ignored) {
                // Not registered yet
            }

            if (demoUser == null) {
                try {
                    demoUser = userService.register("Demo User", "demo@spendwise.com", "password123");
                    System.out.println("[INFO] Seeded default user: demo@spendwise.com / password123");

                    LocalDate now = LocalDate.now();
                    int month = now.getMonthValue();
                    int year = now.getYear();

                    // Seed Incomes
                    transactionService.addTransaction(demoUser.getId(), TransactionType.INCOME, "Salary", 75000.0, "Monthly Salary", now.withDayOfMonth(1));
                    transactionService.addTransaction(demoUser.getId(), TransactionType.INCOME, "Freelance", 18500.0, "Web Development Project", now.withDayOfMonth(5));

                    // Seed Expenses
                    transactionService.addTransaction(demoUser.getId(), TransactionType.EXPENSE, "Rent & Housing", 22000.0, "Apartment Monthly Rent", now.withDayOfMonth(2));
                    transactionService.addTransaction(demoUser.getId(), TransactionType.EXPENSE, "Food & Dining", 6400.0, "Supermarket & Groceries", now.withDayOfMonth(4));
                    transactionService.addTransaction(demoUser.getId(), TransactionType.EXPENSE, "Utilities", 3200.0, "Electricity & High-speed Internet", now.withDayOfMonth(6));
                    transactionService.addTransaction(demoUser.getId(), TransactionType.EXPENSE, "Transportation", 2800.0, "Metro pass & fuel", now.withDayOfMonth(8));
                    transactionService.addTransaction(demoUser.getId(), TransactionType.EXPENSE, "Entertainment & Leisure", 1500.0, "Weekend Movie & Dinner", now.withDayOfMonth(10));

                    // Seed Budgets
                    budgetService.setBudget(demoUser.getId(), "Food & Dining", 10000.0, month, year);
                    budgetService.setBudget(demoUser.getId(), "Rent & Housing", 25000.0, month, year);
                    budgetService.setBudget(demoUser.getId(), "Utilities", 4000.0, month, year);
                    budgetService.setBudget(demoUser.getId(), "Transportation", 3500.0, month, year);
                    budgetService.setBudget(demoUser.getId(), "Entertainment & Leisure", 3000.0, month, year);

                    System.out.println("[INFO] Seeded initial transactions and monthly budgets for demo account.");
                } catch (Exception ex) {
                    System.out.println("[INFO] Note on seed data: " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("[WARN] Skipping demo seed: " + e.getMessage());
        }
    }
}
