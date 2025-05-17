package com.ahd.backend.carcontracts.util.base;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public class SpecificationBuilder<T> {

    private final Class<T> entityClass;

    public SpecificationBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public Specification<T> build(Map<String, String> params) {
        Specification<T> spec = Specification.where(null);

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String[] parts = entry.getKey().split("_", 2);
            if (parts.length != 2) continue; // skip non-filter params

            String field = parts[0];
            FilterOperation op = FilterOperation.fromString(parts[1]);
            Object value = castValue(field, entry.getValue());

            FilterCriteria criteria = new FilterCriteria(field, op, value);
            spec = spec.and(new GenericSpecification<>(criteria));
        }

        return spec;
    }

    private Object castValue(String fieldName, String stringValue) {
        try {
            Field field = ReflectionUtils.findField(entityClass, fieldName);
            Class<?> type = field.getType();
            if (type == String.class)        return stringValue;
            if (type == Integer.class || type == int.class)
                return Integer.valueOf(stringValue);
            if (type == Long.class   || type == long.class)
                return Long.valueOf(stringValue);
            if (type == BigDecimal.class)    return new BigDecimal(stringValue);
            if (type == LocalDateTime.class) return LocalDateTime.parse(stringValue);
            if (type == LocalDate.class)     return LocalDate.parse(stringValue);
            // add more as needed...
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot cast filter value for field " + fieldName, e);
        }
        return stringValue;
    }
}
