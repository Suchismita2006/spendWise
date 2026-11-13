package com.spendwise.web.handler;

import com.spendwise.exception.AuthenticationException;
import com.spendwise.exception.ValidationException;
import com.spendwise.model.User;
import com.spendwise.service.IUserService;
import com.spendwise.service.UserServiceImpl;
import com.spendwise.util.JsonUtil;
import com.spendwise.web.HttpUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Endpoint Handler for User Registration and Authentication.
 */
public class AuthHandler implements HttpHandler {
    private final IUserService userService;

    public AuthHandler() {
        this(new UserServiceImpl());
    }

    public AuthHandler(IUserService userService) {
        this.userService = userService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpUtils.handlePreflight(exchange)) return;

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if ("POST".equalsIgnoreCase(method) && path.endsWith("/register")) {
                handleRegister(exchange);
            } else if ("POST".equalsIgnoreCase(method) && path.endsWith("/login")) {
                handleLogin(exchange);
            } else if ("GET".equalsIgnoreCase(method) && path.endsWith("/profile")) {
                handleProfile(exchange);
            } else {
                HttpUtils.sendErrorResponse(exchange, 404, "Endpoint not found: " + path);
            }
        } catch (ValidationException | AuthenticationException e) {
            HttpUtils.sendErrorResponse(exchange, 400, e.getMessage());
        } catch (Exception e) {
            HttpUtils.sendErrorResponse(exchange, 500, "Server error: " + e.getMessage());
        }
    }

    private void handleRegister(HttpExchange exchange) throws IOException {
        String body = HttpUtils.readRequestBody(exchange);
        Map<String, Object> req = JsonUtil.parseObject(body);

        String fullName = JsonUtil.getString(req, "fullName", "");
        String email = JsonUtil.getString(req, "email", "");
        String password = JsonUtil.getString(req, "password", "");

        User created = userService.register(fullName, email, password);

        Map<String, Object> res = new HashMap<>();
        res.put("userId", created.getId());
        res.put("fullName", created.getFullName());
        res.put("email", created.getEmail());
        res.put("currency", created.getCurrency());
        res.put("message", "Registration successful!");

        HttpUtils.sendSuccessResponse(exchange, res);
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        String body = HttpUtils.readRequestBody(exchange);
        Map<String, Object> req = JsonUtil.parseObject(body);

        String email = JsonUtil.getString(req, "email", "");
        String password = JsonUtil.getString(req, "password", "");

        User user = userService.login(email, password);

        Map<String, Object> res = new HashMap<>();
        res.put("userId", user.getId());
        res.put("fullName", user.getFullName());
        res.put("email", user.getEmail());
        res.put("currency", user.getCurrency());
        res.put("message", "Login successful!");

        HttpUtils.sendSuccessResponse(exchange, res);
    }

    private void handleProfile(HttpExchange exchange) throws IOException {
        Map<String, String> q = HttpUtils.parseQueryParams(exchange);
        int userId = Integer.parseInt(q.getOrDefault("userId", "0"));
        if (userId <= 0) {
            HttpUtils.sendErrorResponse(exchange, 400, "Missing or invalid userId.");
            return;
        }

        User user = userService.getUserById(userId);
        Map<String, Object> res = new HashMap<>();
        res.put("userId", user.getId());
        res.put("fullName", user.getFullName());
        res.put("email", user.getEmail());
        res.put("currency", user.getCurrency());

        HttpUtils.sendSuccessResponse(exchange, res);
    }
}
