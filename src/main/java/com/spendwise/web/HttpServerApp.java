package com.spendwise.web;

import com.spendwise.config.DatabaseConfig;
import com.spendwise.web.handler.*;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Embedded HTTP Server orchestrating API routes and serving the HTML/CSS/JavaScript client interface.
 */
public class HttpServerApp {
    private HttpServer server;
    private final int port;

    public HttpServerApp() {
        this(DatabaseConfig.getServerPort());
    }

    public HttpServerApp(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Bind REST API Handlers
        AuthHandler authHandler = new AuthHandler();
        server.createContext("/api/auth", authHandler);

        TransactionHandler transactionHandler = new TransactionHandler();
        server.createContext("/api/transactions", transactionHandler);
        server.createContext("/api/account/balance", transactionHandler);

        BudgetHandler budgetHandler = new BudgetHandler();
        server.createContext("/api/budgets", budgetHandler);

        SummaryHandler summaryHandler = new SummaryHandler();
        server.createContext("/api/summary", summaryHandler);
        server.createContext("/api/categories", summaryHandler);

        // Bind Static Frontend Web Handler
        server.createContext("/", new StaticFileHandler());

        // Multi-threaded executor
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("==================================================================");
        System.out.println("   SpendWise - Personal Expense Management System is ACTIVE!      ");
        System.out.println("==================================================================");
        System.out.println(" Web UI available at: http://localhost:" + port);
        System.out.println(" REST API available at: http://localhost:" + port + "/api");
        System.out.println(" Press Ctrl+C to terminate the application.");
        System.out.println("==================================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("[INFO] SpendWise Server stopped.");
        }
    }
}
