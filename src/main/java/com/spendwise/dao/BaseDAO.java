package com.spendwise.dao;

import com.spendwise.db.DBConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * OOP Pillar: INHERITANCE
 * Abstract base DAO providing connection handling, resource cleanup, and JDBC lifecycle helpers.
 */
public abstract class BaseDAO {

    protected Connection getConnection() {
        return DBConnection.getConnection();
    }

    protected boolean isDBAvailable() {
        return DBConnection.isMySQLAvailable();
    }

    protected void close(AutoCloseable... resources) {
        for (AutoCloseable res : resources) {
            if (res != null) {
                try {
                    res.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
