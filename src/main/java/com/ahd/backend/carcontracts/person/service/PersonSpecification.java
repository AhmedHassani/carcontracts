package com.ahd.backend.carcontracts.person.service;


import com.ahd.backend.carcontracts.person.dto.PersonSearchCriteria;
import com.ahd.backend.carcontracts.person.model.Person;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PersonSpecification {

    public static Specification<Person> buildSpecification(PersonSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.keyword() != null && !criteria.keyword().isBlank()) {
                String pattern = "%" + criteria.keyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), pattern),
                        cb.like(cb.lower(root.get("fatherName")), pattern),
                        cb.like(cb.lower(root.get("fourthName")), pattern),
                        cb.like(cb.lower(root.get("surname")), pattern)
                ));
            }
            if (criteria.phoneNumber() != null && !criteria.phoneNumber().isBlank()) {
                String pattern = "%" + criteria.phoneNumber().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("phoneNumber")), pattern));
            }
            if (criteria.nationalId() != null && !criteria.nationalId().isBlank()) {
                String pattern = "%" + criteria.nationalId().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("nationalId")), pattern));
            }
            if (criteria.residenceCardNo() != null && !criteria.residenceCardNo().isBlank()) {
                String pattern = "%" + criteria.residenceCardNo().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("residenceCardNo")), pattern));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
