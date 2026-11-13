package com.spendwise.web.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * Serves static web assets (HTML, CSS, JavaScript, SVG, favicon) to the browser.
 */
public class StaticFileHandler implements HttpHandler {
    private static final String STATIC_DIR = "src/main/resources/web";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (path == null || path.equals("/") || path.isEmpty()) {
            path = "/index.html";
        } else if (path.equals("/dashboard")) {
            path = "/dashboard.html";
        }

        // Prevent path traversal
        if (path.contains("..")) {
            send404(exchange);
            return;
        }

        // Try filesystem first
        File file = new File(STATIC_DIR + path);
        if (file.exists() && file.isFile()) {
            byte[] bytes = Files.readAllBytes(file.toPath());
            sendResponse(exchange, 200, getContentType(path), bytes);
            return;
        }

        // Try classpath resource
        String resourcePath = "web" + path;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (in != null) {
                byte[] bytes = in.readAllBytes();
                sendResponse(exchange, 200, getContentType(path), bytes);
                return;
            }
        }

        send404(exchange);
    }

    private void sendResponse(HttpExchange exchange, int status, String contentType, byte[] data) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
        exchange.sendResponseHeaders(status, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }

    private void send404(HttpExchange exchange) throws IOException {
        String msg = "404 Not Found";
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(404, msg.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(msg.getBytes());
        }
    }

    private String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=UTF-8";
        if (path.endsWith(".css")) return "text/css; charset=UTF-8";
        if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (path.endsWith(".json")) return "application/json; charset=UTF-8";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".ico")) return "image/x-icon";
        return "text/plain";
    }
}
