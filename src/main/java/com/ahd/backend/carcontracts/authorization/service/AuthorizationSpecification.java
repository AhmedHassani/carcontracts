package com.ahd.backend.carcontracts.authorization.service;


import com.ahd.backend.carcontracts.authorization.dto.AuthorizationSearchCriteria;
import com.ahd.backend.carcontracts.authorization.model.Authorization;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AuthorizationSpecification {

    public static Specification<Authorization> buildSpecification(AuthorizationSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria.authorizationDateStart() != null && criteria.authorizationDateEnd() != null) {
                predicates.add(
                        cb.between(
                                root.get("authorizationDate"),
                                criteria.authorizationDateStart(),
                                criteria.authorizationDateEnd()
                        )
                );
            }
            predicates.add(cb.equal(root.get("companyId"), criteria.companyId()));
            // Authorization number
            if (criteria.authorizationNumber() != null) {
                predicates.add(cb.equal(root.get("authorizationNumber"), criteria.authorizationNumber()));
            }
            // Company agent
            if (hasText(criteria.companyAgent())) {
                predicates.add(cb.like(cb.lower(root.get("companyAgent")),
                        "%" + criteria.companyAgent().toLowerCase() + "%"));
            }
            if (criteria.keyword() != null) {
                var buyer = root.join("buyer", JoinType.LEFT);

                Expression<String> fullName = cb.concat(
                        cb.concat(
                                cb.concat(cb.coalesce(cb.lower(buyer.get("firstName")), ""), " "),
                                cb.concat(cb.coalesce(cb.lower(buyer.get("fatherName")), ""), " ")
                        ),
                        cb.concat(
                                cb.concat(cb.coalesce(cb.lower(buyer.get("fourthName")), ""), " "),
                                cb.coalesce(cb.lower(buyer.get("surname")), "")
                        )
                );

                predicates.add(cb.like(fullName, "%" + criteria.keyword().toLowerCase() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}

