package com.spendwise.web.handler;

import com.spendwise.model.Category;
import com.spendwise.model.MonthlySummary;
import com.spendwise.service.ITransactionService;
import com.spendwise.service.TransactionServiceImpl;
import com.spendwise.web.HttpUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Endpoint Handler providing financial aggregates, monthly expense summaries, and category catalogs.
 */
public class SummaryHandler implements HttpHandler {
    private final ITransactionService transactionService;

    public SummaryHandler() {
        this(new TransactionServiceImpl());
    }

    public SummaryHandler(ITransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpUtils.handlePreflight(exchange)) return;

        String path = exchange.getRequestURI().getPath();

        try {
            if (path.startsWith("/api/categories")) {
                handleCategories(exchange);
            } else if (path.startsWith("/api/summary")) {
                handleSummary(exchange);
            } else {
                HttpUtils.sendErrorResponse(exchange, 404, "Endpoint not found");
            }
        } catch (Exception e) {
            HttpUtils.sendErrorResponse(exchange, 500, "Server error: " + e.getMessage());
        }
    }

    private void handleSummary(HttpExchange exchange) throws IOException {
        Map<String, String> q = HttpUtils.parseQueryParams(exchange);
        int userId = Integer.parseInt(q.getOrDefault("userId", "0"));
        LocalDate now = LocalDate.now();
        int month = Integer.parseInt(q.getOrDefault("month", String.valueOf(now.getMonthValue())));
        int year = Integer.parseInt(q.getOrDefault("year", String.valueOf(now.getYear())));

        if (userId <= 0) {
            HttpUtils.sendErrorResponse(exchange, 400, "Missing userId parameter");
            return;
        }

        MonthlySummary s = transactionService.getMonthlySummary(userId, month, year);
        Map<String, Object> res = new HashMap<>();
        res.put("month", s.getMonth());
        res.put("year", s.getYear());
        res.put("totalIncome", s.getTotalIncome());
        res.put("totalExpense", s.getTotalExpense());
        res.put("netSavings", s.getNetSavings());
        res.put("savingsRate", s.getSavingsRate());
        res.put("categoryExpenses", s.getCategoryExpenses());
        res.put("categoryIncomes", s.getCategoryIncomes());
        res.put("exceededBudgetsCount", s.getExceededBudgetsCount());
        res.put("warningBudgetsCount", s.getWarningBudgetsCount());

        HttpUtils.sendSuccessResponse(exchange, res);
    }

    private void handleCategories(HttpExchange exchange) throws IOException {
        Map<String, Object> res = new HashMap<>();
        res.put("incomeCategories", Category.INCOME_CATEGORIES);
        res.put("expenseCategories", Category.EXPENSE_CATEGORIES);
        HttpUtils.sendSuccessResponse(exchange, res);
    }
}
