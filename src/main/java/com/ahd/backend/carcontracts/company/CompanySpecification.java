package com.ahd.backend.carcontracts.company;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CompanySpecification {
    
    public static Specification<Company> buildSpecification(CompanySearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // Search term (companyName or ownerName)
            if (criteria.getSearchTerm() != null && !criteria.getSearchTerm().trim().isEmpty()) {
                String searchPattern = "%" + criteria.getSearchTerm().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("companyName")), searchPattern),
                    cb.like(cb.lower(root.get("ownerName")), searchPattern)
                ));
            }
            // Status
            if (criteria.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
            }
            // Subscription date range
            if (criteria.getSubscriptionDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("subscriptionDate"), 
                    criteria.getSubscriptionDateFrom()));
            }
            if (criteria.getSubscriptionDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("subscriptionDate"), 
                    criteria.getSubscriptionDateTo()));
            }
            // Expiration date range
            if (criteria.getExpirationDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("expirationDate"), 
                    criteria.getExpirationDateFrom()));
            }
            if (criteria.getExpirationDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("expirationDate"), 
                    criteria.getExpirationDateTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
} 