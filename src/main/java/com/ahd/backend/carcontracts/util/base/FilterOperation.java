package com.ahd.backend.carcontracts.util.base;

public enum FilterOperation {
    EQUALS,
    NOT_EQUALS,
    GREATER_THAN,
    LESS_THAN,
    LIKE,
    IN;

    public static FilterOperation fromString(String op) {
        return valueOf(op.toUpperCase());
    }
}