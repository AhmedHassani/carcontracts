package com.ahd.backend.carcontracts.contract.service;


import com.ahd.backend.carcontracts.contract.dto.ContractSearchCriteria;
import com.ahd.backend.carcontracts.contract.model.Contracts;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
public class ContractSpecification {

    public static Specification<Contracts> buildSpecification(ContractSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.BuyerName() != null && !criteria.BuyerName().isBlank()) {
                String pattern = "%" + criteria.BuyerName().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.join("buyer").get("firstName")), pattern),
                        cb.like(cb.lower(root.join("buyer").get("fatherName")), pattern),
                        cb.like(cb.lower(root.join("buyer").get("fourthName")), pattern),
                        cb.like(cb.lower(root.join("buyer").get("surname")), pattern)
                ));
            }

            if (criteria.BuyerPhone() != null && !criteria.BuyerPhone().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.join("buyer").get("phoneNumber")),
                        "%" + criteria.BuyerPhone().toLowerCase() + "%"
                ));
            }

            if (criteria.SellerName() != null && !criteria.SellerName().isBlank()) {
                String pattern = "%" + criteria.SellerName().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.join("seller").get("firstName")), pattern),
                        cb.like(cb.lower(root.join("seller").get("fatherName")), pattern),
                        cb.like(cb.lower(root.join("seller").get("fourthName")), pattern),
                        cb.like(cb.lower(root.join("seller").get("surname")), pattern)
                ));
            }



            if (criteria.SellerPhone() != null && !criteria.SellerPhone().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.join("seller").get("phoneNumber")),
                        "%" + criteria.SellerPhone().toLowerCase() + "%"
                ));
            }

            if (criteria.StatusPaymant() != null && !criteria.StatusPaymant().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.join("paymentPlan").get("status")),
                        "%" + criteria.StatusPaymant().toLowerCase() + "%"
                ));
            }
            if (criteria.carNumber() != null && !criteria.carNumber().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.join("car").get("plateNumber")),
                        "%" + criteria.carNumber().toLowerCase() + "%"
                ));
            }
            if (criteria.carType() != null && !criteria.carType().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.join("car").get("model")),
                        "%" + criteria.carType().toLowerCase() + "%"
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
