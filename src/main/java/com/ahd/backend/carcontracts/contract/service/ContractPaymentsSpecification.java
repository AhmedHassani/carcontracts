package com.ahd.backend.carcontracts.contract.service;

import com.ahd.backend.carcontracts.contract.dto.ContractPaymentsSearchCriteria;
import com.ahd.backend.carcontracts.contract.model.Contracts;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ContractPaymentsSpecification {

    public static Specification<Contracts> buildSpecification(ContractPaymentsSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            query.distinct(true);

            var seller = root.join("seller", JoinType.LEFT);
            var paymentPlan = root.join("paymentPlan", JoinType.LEFT);

            // keyword: match seller full name = firstName + " " + fatherName
            if (criteria.keyword() != null && !criteria.keyword().isBlank()) {
                String kw = "%" + criteria.keyword().toLowerCase().trim() + "%";

                Expression<String> first = cb.coalesce(seller.get("firstName"), cb.literal(""));
                Expression<String> father = cb.coalesce(seller.get("fatherName"), cb.literal(""));
                Expression<String> fullName = cb.concat(cb.concat(first, cb.literal(" ")), father);

                predicates.add(cb.like(cb.lower(fullName), kw));
            }

            // status: filter on paymentPlan.status (enum PaymentStatus)
            if (criteria.status() != null) {
                predicates.add(cb.equal(paymentPlan.get("status"), criteria.status()));
            }

            // date range: installments.dueDate
            LocalDate start = parseDate(criteria.startDate());
            LocalDate end = parseDate(criteria.endDate());
            if (start != null || end != null) {
                var installments = paymentPlan.join("installments", JoinType.INNER);

                if (start != null && end != null) {
                    predicates.add(cb.between(installments.get("dueDate"), start, end));
                } else if (start != null) {
                    predicates.add(cb.greaterThanOrEqualTo(installments.get("dueDate"), start));
                } else {
                    predicates.add(cb.lessThanOrEqualTo(installments.get("dueDate"), end));
                }
            }
            predicates.add(cb.equal(root.get("companyId"), criteria.companyId()));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        try { return LocalDate.parse(value); } catch (Exception e) { return null; }
    }
}
