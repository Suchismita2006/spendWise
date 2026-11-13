package com.spendwise.config;

import java.io.InputStream;
import java.util.Properties;

/**
 * Loads and encapsulates database and server connection parameters.
 */
public class DatabaseConfig {
    private static final Properties props = new Properties();

    static {
        try (InputStream in = DatabaseConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception ignored) {
            // Fallback to default properties below
        }
    }

    public static String getUrl() {
        return System.getProperty("spendwise.db.url",
                props.getProperty("db.url", "jdbc:mysql://localhost:3306/spendwise?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true"));
    }

    public static String getUsername() {
        return System.getProperty("spendwise.db.username",
                props.getProperty("db.username", "root"));
    }

    public static String getPassword() {
        return System.getProperty("spendwise.db.password",
                props.getProperty("db.password", "root"));
    }

    public static String getDriver() {
        return props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
    }

    public static int getServerPort() {
        String p = System.getProperty("spendwise.server.port", props.getProperty("server.port", "8080"));
        try {
            return Integer.parseInt(p);
        } catch (NumberFormatException e) {
            return 8080;
        }
    }
}
