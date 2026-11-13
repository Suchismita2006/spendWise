package com.spendwise.exception;

/**
 * Thrown during invalid login credentials, expired sessions, or duplicate registrations.
 */
public class AuthenticationException extends SpendWiseException {
    public AuthenticationException(String message) {
        super(message);
    }
}
