package com.spendwise.db;

import com.spendwise.config.DatabaseConfig;
import com.spendwise.exception.DatabaseException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * JDBC Connection Factory implementing resilient connectivity to MySQL.
 */
public class DBConnection {
    private static volatile boolean driverLoaded = false;
    private static Boolean mySQLAvailable = null;

    private static synchronized void loadDriver() {
        if (!driverLoaded) {
            try {
                Class.forName(DatabaseConfig.getDriver());
                driverLoaded = true;
            } catch (ClassNotFoundException e) {
                System.err.println("[WARN] MySQL Driver not found on classpath: " + e.getMessage());
            }
        }
    }

    /**
     * Checks if MySQL database is active and reachable with configured credentials.
     */
    public static boolean isMySQLAvailable() {
        if (mySQLAvailable != null) {
            return mySQLAvailable;
        }
        loadDriver();
        try (Connection conn = DriverManager.getConnection(
                DatabaseConfig.getUrl(),
                DatabaseConfig.getUsername(),
                DatabaseConfig.getPassword())) {
            mySQLAvailable = (conn != null && !conn.isClosed());
            return mySQLAvailable;
        } catch (SQLException e) {
            System.err.println("[INFO] MySQL connection check: " + e.getMessage());
            mySQLAvailable = false;
            return false;
        }
    }

    /**
     * Obtains a fresh JDBC connection to MySQL.
     *
     * @return active java.sql.Connection
     * @throws DatabaseException if connection cannot be opened
     */
    public static Connection getConnection() {
        loadDriver();
        try {
            return DriverManager.getConnection(
                    DatabaseConfig.getUrl(),
                    DatabaseConfig.getUsername(),
                    DatabaseConfig.getPassword()
            );
        } catch (SQLException e) {
            throw new DatabaseException("Failed to establish JDBC connection to MySQL: " + e.getMessage(), e);
        }
    }
}
