package com.ahd.backend.carcontracts.contract.service;


import com.ahd.backend.carcontracts.contract.dto.ContractSearchCriteria;
import com.ahd.backend.carcontracts.contract.model.Contracts;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
public class ContractSpecification {

    public static Specification<Contracts> buildSpecification(ContractSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.keyword() != null && !criteria.keyword().isBlank()) {
                String pattern = "%" + criteria.keyword().toLowerCase() + "%";
                var buyer = root.join("buyer", JoinType.LEFT);

                // concatenate buyer's full name
                Expression<String> buyerFullName = cb.concat(
                        cb.concat(
                                cb.concat(cb.coalesce(cb.lower(buyer.get("firstName")), ""), " "),
                                cb.concat(cb.coalesce(cb.lower(buyer.get("fatherName")), ""), " ")
                        ),
                        cb.concat(
                                cb.concat(cb.coalesce(cb.lower(buyer.get("fourthName")), ""), " "),
                                cb.coalesce(cb.lower(buyer.get("surname")), "")
                        )
                );

                predicates.add(cb.like(buyerFullName, pattern));
            }

            if (criteria.SellerName() != null && !criteria.SellerName().isBlank()) {
                String pattern = "%" + criteria.SellerName().toLowerCase() + "%";
                var seller = root.join("seller", JoinType.LEFT);

                // concatenate seller's full name
                Expression<String> sellerFullName = cb.concat(
                        cb.concat(
                                cb.concat(cb.coalesce(cb.lower(seller.get("firstName")), ""), " "),
                                cb.concat(cb.coalesce(cb.lower(seller.get("fatherName")), ""), " ")
                        ),
                        cb.concat(
                                cb.concat(cb.coalesce(cb.lower(seller.get("fourthName")), ""), " "),
                                cb.coalesce(cb.lower(seller.get("surname")), "")
                        )
                );

                predicates.add(cb.like(sellerFullName, pattern));
            }

            if (criteria.BuyerPhone() != null && !criteria.BuyerPhone().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.join("buyer").get("phoneNumber")),
                        "%" + criteria.BuyerPhone().toLowerCase() + "%"
                ));
            }
            if (criteria.status() != null && !criteria.status().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.join("paymentPlan").get("status")),
                         criteria.status().toLowerCase()
                ));
            }
//            if (criteria.buyerNationalId() != null && !criteria.buyerNationalId().isBlank()) {
//                predicates.add(cb.like(
//                        cb.lower(root.join("buyer").get("buyerNationalId")),
//                        "%" + criteria.buyerNationalId().toLowerCase() + "%"
//                ));
//            }

            if (criteria.SellerPhone() != null && !criteria.SellerPhone().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.join("seller").get("phoneNumber")),
                        "%" + criteria.SellerPhone().toLowerCase() + "%"
                ));
            }
//            if (criteria.sellerNationalId() != null && !criteria.sellerNationalId().isBlank()) {
//                predicates.add(cb.like(
//                        cb.lower(root.join("seller").get("sellerNationalId")),
//                        "%" + criteria.sellerNationalId().toLowerCase() + "%"
//                ));
//            }

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
            if (criteria.name() != null && !criteria.name().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.join("car").get("name")),
                        "%" + criteria.name().toLowerCase() + "%"
                ));
            }
            if (criteria.chassisNumber() != null && !criteria.chassisNumber().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.join("car").get("chassisNumber")),
                        "%" + criteria.chassisNumber().toLowerCase() + "%"
                ));
            }
            if (criteria.carType() != null && !criteria.carType().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.join("car").get("model")),
                        "%" + criteria.carType().toLowerCase() + "%"
                ));
            }


            if (criteria.id() != null) {
                predicates.add(cb.equal(root.get("id"), criteria.id()));
            }


            predicates.add(cb.equal(root.get("companyId"), criteria.companyId()));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
