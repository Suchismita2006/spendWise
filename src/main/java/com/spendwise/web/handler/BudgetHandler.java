package com.spendwise.web.handler;

import com.spendwise.exception.SpendWiseException;
import com.spendwise.model.Budget;
import com.spendwise.service.BudgetServiceImpl;
import com.spendwise.service.IBudgetService;
import com.spendwise.util.JsonUtil;
import com.spendwise.web.HttpUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * REST Endpoint Handler for Monthly Budget allocation and tracking.
 */
public class BudgetHandler implements HttpHandler {
    private final IBudgetService budgetService;

    public BudgetHandler() {
        this(new BudgetServiceImpl());
    }

    public BudgetHandler(IBudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpUtils.handlePreflight(exchange)) return;

        String method = exchange.getRequestMethod();

        try {
            if ("GET".equalsIgnoreCase(method)) {
                handleList(exchange);
            } else if ("POST".equalsIgnoreCase(method)) {
                handleSet(exchange);
            } else if ("DELETE".equalsIgnoreCase(method)) {
                handleDelete(exchange);
            } else {
                HttpUtils.sendErrorResponse(exchange, 405, "Method Not Allowed");
            }
        } catch (SpendWiseException e) {
            HttpUtils.sendErrorResponse(exchange, 400, e.getMessage());
        } catch (Exception e) {
            HttpUtils.sendErrorResponse(exchange, 500, "Server error: " + e.getMessage());
        }
    }

    private void handleList(HttpExchange exchange) throws IOException {
        Map<String, String> q = HttpUtils.parseQueryParams(exchange);
        int userId = Integer.parseInt(q.getOrDefault("userId", "0"));
        LocalDate now = LocalDate.now();
        int month = Integer.parseInt(q.getOrDefault("month", String.valueOf(now.getMonthValue())));
        int year = Integer.parseInt(q.getOrDefault("year", String.valueOf(now.getYear())));

        if (userId <= 0) {
            HttpUtils.sendErrorResponse(exchange, 400, "Missing required parameter: userId");
            return;
        }

        List<Budget> list = budgetService.getBudgetsWithProgress(userId, month, year);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Budget b : list) {
            result.add(formatBudget(b));
        }

        HttpUtils.sendSuccessResponse(exchange, result);
    }

    private void handleSet(HttpExchange exchange) throws IOException {
        String body = HttpUtils.readRequestBody(exchange);
        Map<String, Object> req = JsonUtil.parseObject(body);

        int userId = JsonUtil.getInt(req, "userId", 0);
        String category = JsonUtil.getString(req, "category", "");
        double limit = JsonUtil.getDouble(req, "monthlyLimit", 0.0);
        LocalDate now = LocalDate.now();
        int month = JsonUtil.getInt(req, "month", now.getMonthValue());
        int year = JsonUtil.getInt(req, "year", now.getYear());

        Budget saved = budgetService.setBudget(userId, category, limit, month, year);
        HttpUtils.sendSuccessResponse(exchange, formatBudget(saved));
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        Map<String, String> q = HttpUtils.parseQueryParams(exchange);
        int budgetId = Integer.parseInt(q.getOrDefault("budgetId", "0"));

        if (budgetId <= 0) {
            HttpUtils.sendErrorResponse(exchange, 400, "Missing budgetId parameter");
            return;
        }

        boolean ok = budgetService.deleteBudget(budgetId);
        Map<String, Object> res = new HashMap<>();
        res.put("deleted", ok);
        res.put("budgetId", budgetId);
        HttpUtils.sendSuccessResponse(exchange, res);
    }

    private Map<String, Object> formatBudget(Budget b) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", b.getId());
        m.put("userId", b.getUserId());
        m.put("category", b.getCategory());
        m.put("monthlyLimit", b.getMonthlyLimit());
        m.put("currentSpent", b.getCurrentSpent());
        m.put("remainingBudget", b.getRemainingBudget());
        m.put("percentageUsed", b.getPercentageUsed());
        m.put("month", b.getMonth());
        m.put("year", b.getYear());
        m.put("status", b.getStatus());
        m.put("isExceeded", b.isExceeded());
        m.put("isWarning", b.isWarning());
        return m;
    }
}
