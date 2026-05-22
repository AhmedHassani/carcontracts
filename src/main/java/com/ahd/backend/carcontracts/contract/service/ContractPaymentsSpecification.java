package com.ahd.backend.carcontracts.contract.service;

import com.ahd.backend.carcontracts.contract.dto.ContractPaymentsSearchCriteria;
import com.ahd.backend.carcontracts.contract.model.Contracts;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ContractPaymentsSpecification {

    public static Specification<Contracts> buildSpecification(ContractPaymentsSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            query.distinct(true);

            // Joins
            Join<Object, Object> seller = root.join("seller", JoinType.LEFT);
            Join<Object, Object> paymentPlan = root.join("paymentPlan", JoinType.LEFT);
            Join<Object, Object> possessor = root.join("possessor", JoinType.LEFT);  // ADD THIS

            // Keyword: match seller full name
            if (criteria.keyword() != null && !criteria.keyword().isBlank()) {
                String kw = "%" + criteria.keyword().toLowerCase().trim() + "%";

                Expression<String> first = cb.coalesce(seller.get("firstName"), cb.literal(""));
                Expression<String> father = cb.coalesce(seller.get("fatherName"), cb.literal(""));
                Expression<String> fullName = cb.concat(cb.concat(first, cb.literal(" ")), father);

                predicates.add(cb.like(cb.lower(fullName), kw));
            }

            // Status filter on paymentPlan.status
            if (criteria.status() != null) {
                predicates.add(cb.equal(paymentPlan.get("status"), criteria.status()));
            }

            // Contract ID filter
            if (criteria.contractId() != null) {
                predicates.add(cb.equal(paymentPlan.get("id"), criteria.contractId()));
            }

            // ADD THIS: Possessor name filter
            if (criteria.possessorName() != null && !criteria.possessorName().isBlank()) {
                String pattern = "%" + criteria.possessorName().toLowerCase().trim() + "%";
                
                Expression<String> possessorFirst = cb.coalesce(possessor.get("firstName"), cb.literal(""));
                Expression<String> possessorFather = cb.coalesce(possessor.get("fatherName"), cb.literal(""));
                Expression<String> possessorGrandfather = cb.coalesce(possessor.get("grandfatherName"), cb.literal(""));
                Expression<String> possessorFourth = cb.coalesce(possessor.get("fourthName"), cb.literal(""));
                Expression<String> possessorSurname = cb.coalesce(possessor.get("surname"), cb.literal(""));
                
                // Build full name: first + father + grandfather + fourth + surname
                Expression<String> possessorFullName = cb.concat(
                    cb.concat(cb.concat(possessorFirst, cb.literal(" ")), possessorFather),
                    cb.concat(cb.literal(" "), cb.concat(
                        cb.concat(possessorGrandfather, cb.literal(" ")),
                        cb.concat(possessorFourth, cb.concat(cb.literal(" "), possessorSurname))
                    ))
                );
                
                predicates.add(cb.like(cb.lower(possessorFullName), pattern));
            }

            // ADD THIS: Possessor phone filter
            if (criteria.possessorPhone() != null && !criteria.possessorPhone().isBlank()) {
                String pattern = "%" + criteria.possessorPhone() + "%";
                predicates.add(cb.like(possessor.get("phoneNumber"), pattern));
            }

            // Date range filter on installments.dueDate
            LocalDate start = parseDate(criteria.startDate());
            LocalDate end = parseDate(criteria.endDate());
            
            if (start != null || end != null) {
                var installments = paymentPlan.join("installments", JoinType.INNER);

                if (start != null && end != null) {
                    predicates.add(cb.between(installments.get("dueDate"), start, end));
                } else if (start != null) {
                    predicates.add(cb.greaterThanOrEqualTo(installments.get("dueDate"), start));
                } else if (end != null) {
                    predicates.add(cb.lessThanOrEqualTo(installments.get("dueDate"), end));
                }
            }
            
            // Company ID filter
            predicates.add(cb.equal(root.get("companyId"), criteria.companyId()));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        try { 
            return LocalDate.parse(value); 
        } catch (Exception e) { 
            return null; 
        }
    }
}