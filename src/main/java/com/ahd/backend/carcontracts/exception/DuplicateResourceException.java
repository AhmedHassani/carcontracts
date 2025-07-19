package com.ahd.backend.carcontracts.exception;


import lombok.Getter;

/** Thrown when a record already exists with a unique field value. */
@Getter
public class DuplicateResourceException extends RuntimeException {

    private final String field;
    private final Object value;

    public DuplicateResourceException(String field, Object value, String message) {
        super(message);
        this.field = field;
        this.value = value;
    }
}
