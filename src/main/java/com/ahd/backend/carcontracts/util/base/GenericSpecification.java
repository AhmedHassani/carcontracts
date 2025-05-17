package com.ahd.backend.carcontracts.util.base;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;

public class GenericSpecification<T> implements Specification<T> {
    private final FilterCriteria criteria;

    public GenericSpecification(FilterCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(Root<T> root,
                                 CriteriaQuery<?> query,
                                 CriteriaBuilder builder) {
        Path<?> path = root.get(criteria.getField());
        switch (criteria.getOperation()) {
            case EQUALS:
                return builder.equal(path, criteria.getValue());
            case NOT_EQUALS:
                return builder.notEqual(path, criteria.getValue());
            case GREATER_THAN:
                if (Number.class.isAssignableFrom(path.getJavaType())) {
                    return builder.gt(root.get(criteria.getField()), (Number) criteria.getValue());
                }
                if (Comparable.class.isAssignableFrom(path.getJavaType())) {
                    // e.g. LocalDateTime, String, etc.
                    @SuppressWarnings("unchecked")
                    Comparable<Object> val = (Comparable<Object>) criteria.getValue();
                    return builder.greaterThan(root.get(criteria.getField()), val);
                }
                break;
            case LESS_THAN:
                if (Number.class.isAssignableFrom(path.getJavaType())) {
                    return builder.lt(root.get(criteria.getField()), (Number) criteria.getValue());
                }
                if (Comparable.class.isAssignableFrom(path.getJavaType())) {
                    @SuppressWarnings("unchecked")
                    Comparable<Object> val = (Comparable<Object>) criteria.getValue();
                    return builder.lessThan(root.get(criteria.getField()), val);
                }
                break;
            case LIKE:
                return builder.like(root.get(criteria.getField()), "%" + criteria.getValue() + "%");
            case IN:
                return path.in((Collection<?>) criteria.getValue());
        }
        return null;
    }
}
