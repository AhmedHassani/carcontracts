package com.ahd.backend.carcontracts.person.service;


import com.ahd.backend.carcontracts.person.dto.PersonSearchCriteria;
import com.ahd.backend.carcontracts.person.model.Person;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PersonSpecification {

    public static Specification<Person> buildSpecification(PersonSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Expression<String> fullName = cb.concat(
                    cb.concat(
                            cb.concat(
                                    cb.concat(
                                            cb.concat(cb.coalesce(cb.lower(root.get("firstName")), ""), " "),
                                            cb.concat(cb.coalesce(cb.lower(root.get("fatherName")), ""), " ")
                                    ),
                                    cb.concat(cb.coalesce(cb.lower(root.get("grandfatherName")), ""), " ")
                            ),
                            cb.concat(cb.coalesce(cb.lower(root.get("fourthName")), ""), " ")
                    ),
                    cb.coalesce(cb.lower(root.get("surname")), "")
            );

            if (criteria.keyword() != null && !criteria.keyword().isBlank()) {
                String pattern = "%" + criteria.keyword().toLowerCase() + "%";

                predicates.add(
                        cb.or(
                                // Full name
                                cb.like(fullName, pattern),

                                // Individual name parts
                                cb.like(cb.lower(root.get("firstName")), pattern),
                                cb.like(cb.lower(root.get("fatherName")), pattern),
                                cb.like(cb.lower(root.get("grandfatherName")), pattern),
                                cb.like(cb.lower(root.get("fourthName")), pattern),
                                cb.like(cb.lower(root.get("surname")), pattern),

                                // Phone, nationalId, residenceCardNo
                                cb.like(cb.lower(root.get("phoneNumber")), pattern),
                                cb.like(cb.lower(root.get("nationalId")), pattern),
                                cb.like(cb.lower(root.get("residenceCardNo")), pattern)
                        )
                );
            }

            if (criteria.phoneNumber() != null && !criteria.phoneNumber().isBlank()) {
                String pattern = "%" + criteria.phoneNumber().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("phoneNumber")), pattern));
            }

            if (criteria.residence() != null && !criteria.residence().isBlank()) {
                String pattern = "%" + criteria.residence().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("residence")), pattern));
            }

            if (criteria.nationalId() != null && !criteria.nationalId().isBlank()) {
                String pattern = "%" + criteria.nationalId().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("nationalId")), pattern));
            }

            if (criteria.residenceCardNo() != null && !criteria.residenceCardNo().isBlank()) {
                String pattern = "%" + criteria.residenceCardNo().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("residenceCardNo")), pattern));
            }
            //if (criteria.residenceCardNo() != null && !criteria.residenceCardNo().isBlank()) {
          //  if (criteria.companyId() != null) {
                predicates.add(cb.equal(root.get("companyId"), criteria.companyId()));
          //  }
           // }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

//private static Expression<String> concatFields(CriteriaBuilder cb, Root<Person> root, String... fields) {
//    Expression<String> result = cb.lower(root.get(fields[0]));
//    for (int i = 1; i < fields.length; i++) {
//        result = cb.concat(cb.concat(result, " "), cb.lower(root.get(fields[i])));
//    }
//    return result;
//}
//
//    public static Specification<Person> buildSpecification(PersonSearchCriteria criteria) {
//    return (root, query, cb) -> {
//        List<Predicate> predicates = new ArrayList<>();
//
//        if (criteria.keyword() != null && !criteria.keyword().isBlank()) {
//            String pattern = "%" + criteria.keyword().toLowerCase() + "%";
//
//            // single-field matches
//            predicates.add(cb.like(cb.lower(root.get("firstName")), pattern));
//            predicates.add(cb.like(cb.lower(root.get("fatherName")), pattern));
//            predicates.add(cb.like(cb.lower(root.get("grandfatherName")), pattern));
//            predicates.add(cb.like(cb.lower(root.get("fourthName")), pattern));
//            predicates.add(cb.like(cb.lower(root.get("surname")), pattern));
//
//            // multi-field combinations
//            predicates.add(cb.like(concatFields(cb, root, "firstName", "fatherName"), pattern));
//            predicates.add(cb.like(concatFields(cb, root, "fatherName", "grandfatherName"), pattern));
//            predicates.add(cb.like(concatFields(cb, root, "grandfatherName", "fourthName"), pattern));
//            predicates.add(cb.like(concatFields(cb, root, "fourthName", "surname"), pattern));
//            predicates.add(cb.like(concatFields(cb, root, "firstName", "fatherName", "grandfatherName"), pattern));
//            predicates.add(cb.like(concatFields(cb, root, "firstName", "fatherName", "grandfatherName", "fourthName"), pattern));
//            predicates.add(cb.like(concatFields(cb, root, "firstName", "fatherName", "grandfatherName", "fourthName", "surname"), pattern));
//        }
//
//        return cb.and(predicates.toArray(new Predicate[0]));
//    };
//}

}
