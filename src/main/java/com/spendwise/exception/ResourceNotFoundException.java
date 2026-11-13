package com.spendwise.exception;

/**
 * Thrown when requested entity (User, Transaction, Budget) is not found.
 */
public class ResourceNotFoundException extends SpendWiseException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
