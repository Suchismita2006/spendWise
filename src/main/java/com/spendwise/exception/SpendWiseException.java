package com.spendwise.exception;

/**
 * Base custom runtime exception for SpendWise application.
 */
public class SpendWiseException extends RuntimeException {
    public SpendWiseException(String message) {
        super(message);
    }

    public SpendWiseException(String message, Throwable cause) {
        super(message, cause);
    }
}
