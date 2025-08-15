package com.ahd.backend.carcontracts.authorization.service;


import com.ahd.backend.carcontracts.authorization.dto.AuthorizationSearchCriteria;
import com.ahd.backend.carcontracts.authorization.model.Authorization;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AuthorizationSpecification {

    public static Specification<Authorization> buildSpecification(AuthorizationSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // Authorization number
            if (criteria.authorizationNumber() != null) {
                predicates.add(cb.equal(root.get("authorizationNumber"), criteria.authorizationNumber()));
            }
            // Company agent
            if (hasText(criteria.companyAgent())) {
                predicates.add(cb.like(cb.lower(root.get("companyAgent")),
                        "%" + criteria.companyAgent().toLowerCase() + "%"));
            }
            // Buyer name (search across multiple Person fields)
            if (hasText(criteria.keyword())) {
                String pattern = "%" + criteria.keyword().toLowerCase() + "%";
                var buyer = root.join("buyer");
                predicates.add(cb.or(
                        cb.like(cb.lower(buyer.get("firstName")), pattern),
                        cb.like(cb.lower(buyer.get("fatherName")), pattern),
                        cb.like(cb.lower(buyer.get("fourthName")), pattern),
                        cb.like(cb.lower(buyer.get("surname")), pattern)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}

