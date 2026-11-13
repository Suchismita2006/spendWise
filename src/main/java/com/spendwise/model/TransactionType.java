package com.spendwise.model;

/**
 * Supported transaction classifications.
 */
public enum TransactionType {
    INCOME,
    EXPENSE;

    public static TransactionType fromString(String val) {
        if (val == null) return null;
        val = val.trim().toUpperCase();
        if ("INCOME".equals(val)) return INCOME;
        if ("EXPENSE".equals(val)) return EXPENSE;
        throw new IllegalArgumentException("Unknown transaction type: " + val);
    }
}
