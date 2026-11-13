package com.spendwise.web.handler;

import com.spendwise.exception.SpendWiseException;
import com.spendwise.model.AbstractTransaction;
import com.spendwise.model.Account;
import com.spendwise.model.TransactionType;
import com.spendwise.service.ITransactionService;
import com.spendwise.service.TransactionServiceImpl;
import com.spendwise.util.JsonUtil;
import com.spendwise.web.HttpUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * REST Endpoint Handler for Transaction Management and Account Balance calculation.
 */
public class TransactionHandler implements HttpHandler {
    private final ITransactionService transactionService;

    public TransactionHandler() {
        this(new TransactionServiceImpl());
    }

    public TransactionHandler(ITransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpUtils.handlePreflight(exchange)) return;

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if (path.startsWith("/api/account/balance")) {
                handleBalance(exchange);
            } else if ("GET".equalsIgnoreCase(method)) {
                handleList(exchange);
            } else if ("POST".equalsIgnoreCase(method)) {
                handleCreate(exchange);
            } else if ("PUT".equalsIgnoreCase(method)) {
                handleUpdate(exchange);
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
        if (userId <= 0) {
            HttpUtils.sendErrorResponse(exchange, 400, "Missing required query parameter: userId");
            return;
        }

        TransactionType type = null;
        String typeStr = q.get("type");
        if (typeStr != null && !typeStr.trim().isEmpty() && !"ALL".equalsIgnoreCase(typeStr)) {
            type = TransactionType.fromString(typeStr);
        }

        String category = q.get("category");
        String search = q.get("search");

        LocalDate start = null;
        String startStr = q.get("startDate");
        if (startStr != null && !startStr.trim().isEmpty()) {
            start = LocalDate.parse(startStr.trim());
        }

        LocalDate end = null;
        String endStr = q.get("endDate");
        if (endStr != null && !endStr.trim().isEmpty()) {
            end = LocalDate.parse(endStr.trim());
        }

        List<AbstractTransaction> list = transactionService.getTransactions(userId, type, category, start, end, search);
        List<Map<String, Object>> result = new ArrayList<>();
        for (AbstractTransaction tx : list) {
            result.add(formatTransaction(tx));
        }

        HttpUtils.sendSuccessResponse(exchange, result);
    }

    private void handleCreate(HttpExchange exchange) throws IOException {
        String body = HttpUtils.readRequestBody(exchange);
        Map<String, Object> req = JsonUtil.parseObject(body);

        int userId = JsonUtil.getInt(req, "userId", 0);
        String typeStr = JsonUtil.getString(req, "type", "EXPENSE");
        String category = JsonUtil.getString(req, "category", "General");
        double amount = JsonUtil.getDouble(req, "amount", 0.0);
        String description = JsonUtil.getString(req, "description", "");
        String dateStr = JsonUtil.getString(req, "transactionDate", LocalDate.now().toString());

        LocalDate date = LocalDate.parse(dateStr);
        TransactionType type = TransactionType.fromString(typeStr);

        AbstractTransaction created = transactionService.addTransaction(userId, type, category, amount, description, date);
        HttpUtils.sendSuccessResponse(exchange, formatTransaction(created));
    }

    private void handleUpdate(HttpExchange exchange) throws IOException {
        String body = HttpUtils.readRequestBody(exchange);
        Map<String, Object> req = JsonUtil.parseObject(body);

        int userId = JsonUtil.getInt(req, "userId", 0);
        int transactionId = JsonUtil.getInt(req, "transactionId", 0);
        String typeStr = JsonUtil.getString(req, "type", "EXPENSE");
        String category = JsonUtil.getString(req, "category", "General");
        double amount = JsonUtil.getDouble(req, "amount", 0.0);
        String description = JsonUtil.getString(req, "description", "");
        String dateStr = JsonUtil.getString(req, "transactionDate", LocalDate.now().toString());

        LocalDate date = LocalDate.parse(dateStr);
        TransactionType type = TransactionType.fromString(typeStr);

        AbstractTransaction updated = transactionService.updateTransaction(
                userId, transactionId, type, category, amount, description, date);
        HttpUtils.sendSuccessResponse(exchange, formatTransaction(updated));
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        Map<String, String> q = HttpUtils.parseQueryParams(exchange);
        int userId = Integer.parseInt(q.getOrDefault("userId", "0"));
        int transactionId = Integer.parseInt(q.getOrDefault("transactionId", "0"));

        if (userId <= 0 || transactionId <= 0) {
            HttpUtils.sendErrorResponse(exchange, 400, "Missing required parameters: userId and transactionId");
            return;
        }

        boolean ok = transactionService.deleteTransaction(userId, transactionId);
        Map<String, Object> res = new HashMap<>();
        res.put("deleted", ok);
        res.put("transactionId", transactionId);
        HttpUtils.sendSuccessResponse(exchange, res);
    }

    private void handleBalance(HttpExchange exchange) throws IOException {
        Map<String, String> q = HttpUtils.parseQueryParams(exchange);
        int userId = Integer.parseInt(q.getOrDefault("userId", "0"));
        if (userId <= 0) {
            HttpUtils.sendErrorResponse(exchange, 400, "Missing userId");
            return;
        }

        Account account = transactionService.calculateAccountBalance(userId);
        Map<String, Object> res = new HashMap<>();
        res.put("userId", account.getUserId());
        res.put("balance", Math.round(account.getBalance() * 100.0) / 100.0);
        res.put("currency", account.getCurrency());
        HttpUtils.sendSuccessResponse(exchange, res);
    }

    private Map<String, Object> formatTransaction(AbstractTransaction tx) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", tx.getId());
        m.put("userId", tx.getUserId());
        m.put("type", tx.getType().name());
        m.put("category", tx.getCategory());
        m.put("amount", tx.getAmount());
        m.put("signedAmount", tx.getSignedAmount());
        m.put("description", tx.getDescription());
        m.put("transactionDate", tx.getTransactionDate().toString());
        m.put("createdAt", tx.getCreatedAt() != null ? tx.getCreatedAt().toString() : "");
        return m;
    }
}
