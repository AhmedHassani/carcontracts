package com.ahd.backend.carcontracts.util.base;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FilterCriteria {
    private final String field;
    private final FilterOperation operation;
    private final Object value;

    public FilterCriteria(String field, FilterOperation operation, Object value) {
        this.field = field;
        this.operation = operation;
        this.value = value;
    }
}