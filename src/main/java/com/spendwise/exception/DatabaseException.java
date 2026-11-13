package com.spendwise.exception;

/**
 * Wraps low-level JDBC SQLExceptions into runtime exceptions for the service layer.
 */
public class DatabaseException extends SpendWiseException {
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
