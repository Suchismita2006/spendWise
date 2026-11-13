package com.spendwise.exception;

/**
 * Thrown when user or transaction input fails validation rules.
 */
public class ValidationException extends SpendWiseException {
    public ValidationException(String message) {
        super(message);
    }
}
