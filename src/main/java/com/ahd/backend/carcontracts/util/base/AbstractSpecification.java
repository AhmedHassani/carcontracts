package com.ahd.backend.carcontracts.util.base;


import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Generic superclass for building Specifications.
 * Sub-classes implement {@code build(...)} and call the protected helpers.
 *
 * @param <C> criteria-DTO type (must implement BaseCriteria)
 * @param <T> entity type
 */
public abstract class AbstractSpecification<C extends BaseCriteria, T>
        implements Specification<T> {

    protected final C c;

    protected AbstractSpecification(C criteria) {
        this.c = Objects.requireNonNull(criteria);
    }

    /* ───────────── Template method ───────────── */

    @Override
    public Predicate toPredicate(Root<T> root,
                                 CriteriaQuery<?> query,
                                 CriteriaBuilder cb) {

        List<Predicate> predicates = new ArrayList<>();
        build(root, cb, predicates);
        return cb.and(predicates.toArray(new Predicate[0]));
    }

    /** Implement entity-specific predicates here */
    protected abstract void build(Root<T> root,
                                  CriteriaBuilder cb,
                                  List<Predicate> predicates);

    /* ────────────── Common helpers ───────────── */
    /* Free-text search across multiple String columns (case-insensitive) */
    @SafeVarargs
    protected final void keywordSearch(String keyword,
                                       CriteriaBuilder cb,
                                       List<Predicate> p,
                                       Path<String>... columns) {
        if (keyword == null || keyword.isBlank() || columns.length == 0) return;
        String like = "%" + keyword.trim().toLowerCase() + "%";
        Predicate[] ors = Arrays.stream(columns)
                .map(col -> cb.like(cb.lower(col), like))
                .toArray(Predicate[]::new);
        p.add(cb.or(ors));
    }

    /* EQUAL (nullable-safe) */
    protected <V> void equal(Path<V> path, V value,
                             List<Predicate> p, CriteriaBuilder cb) {
        if (value != null) p.add(cb.equal(path, value));
    }

    /* EQUAL (case-insensitive String) */
    protected void equalIgnoreCase(Path<String> path, String value,
                                   List<Predicate> p, CriteriaBuilder cb) {
        if (value != null) p.add(cb.equal(cb.lower(path), value.toLowerCase()));
    }

    /* LIKE %pattern% (case-insensitive) */
    protected void likeIgnoreCase(Path<String> path, String pattern,
                                  List<Predicate> p, CriteriaBuilder cb) {
        if (pattern != null && !pattern.isBlank()) {
            p.add(cb.like(cb.lower(path), "%" + pattern.toLowerCase() + "%"));
        }
    }

    /* BETWEEN numeric (nullable min/max) */
    protected void between(Path<Integer> path,
                           Integer min, Integer max,
                           List<Predicate> p, CriteriaBuilder cb) {
        if (min != null) p.add(cb.ge(path, min));
        if (max != null) p.add(cb.le(path, max));
    }

    /* BETWEEN LocalDate (inclusive) */
    protected void between(Path<LocalDate> path,
                           LocalDate from, LocalDate to,
                           List<Predicate> p, CriteriaBuilder cb) {
        if (from != null) p.add(cb.greaterThanOrEqualTo(path, from));
        if (to   != null) p.add(cb.lessThanOrEqualTo(path, to));
    }

    /* BETWEEN LocalDateTime (inclusive) */
    protected void between(Path<LocalDateTime> path,
                           LocalDateTime from, LocalDateTime to,
                           List<Predicate> p, CriteriaBuilder cb) {
        if (from != null) p.add(cb.greaterThanOrEqualTo(path, from));
        if (to   != null) p.add(cb.lessThanOrEqualTo(path, to));
    }

    /* ENUM equality (nullable) */
    protected <E extends Enum<E>> void enumEqual(Path<E> path, E value,
                                                 List<Predicate> p, CriteriaBuilder cb) {
        if (value != null) p.add(cb.equal(path, value));
    }

    /* IN (...) for collections (skips empty) */
    protected <V> void in(Path<V> path, Collection<V> values,
                          List<Predicate> p, CriteriaBuilder cb) {
        if (values != null && !values.isEmpty()) {
            p.add(path.in(values));
        }
    }

    /* STARTS WITH (case-insensitive) */
    protected void startsWithIgnoreCase(Path<String> path, String prefix,
                                        List<Predicate> p, CriteriaBuilder cb) {
        if (prefix != null && !prefix.isBlank()) {
            p.add(cb.like(cb.lower(path), prefix.toLowerCase() + "%"));
        }
    }

    /* ENDS WITH (case-insensitive) */
    protected void endsWithIgnoreCase(Path<String> path, String suffix,
                                      List<Predicate> p, CriteriaBuilder cb) {
        if (suffix != null && !suffix.isBlank()) {
            p.add(cb.like(cb.lower(path), "%" + suffix.toLowerCase()));
        }
    }
}
