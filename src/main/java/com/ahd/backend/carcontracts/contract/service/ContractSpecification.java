package com.ahd.backend.carcontracts.contract.service;

import com.ahd.backend.carcontracts.contract.dto.ContractSearchCriteria;
import com.ahd.backend.carcontracts.contract.model.Contracts;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ContractSpecification {

    public static Specification<Contracts> buildSpecification(ContractSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // ADD THIS: Possessor join (needed for possessor filters)
            Join<Object, Object> possessor = root.join("possessor", JoinType.LEFT);

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

            // ADD THIS: Possessor name filter
            if (criteria.possessorName() != null && !criteria.possessorName().isBlank()) {
                String pattern = "%" + criteria.possessorName().toLowerCase().trim() + "%";
                
                Expression<String> possessorFirst = cb.coalesce(possessor.get("firstName"), cb.literal(""));
                Expression<String> possessorFather = cb.coalesce(possessor.get("fatherName"), cb.literal(""));
                Expression<String> possessorGrandfather = cb.coalesce(possessor.get("grandfatherName"), cb.literal(""));
                Expression<String> possessorFourth = cb.coalesce(possessor.get("fourthName"), cb.literal(""));
                Expression<String> possessorSurname = cb.coalesce(possessor.get("surname"), cb.literal(""));
                
                // Build full name: firstName + fatherName + grandfatherName + fourthName + surname
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

            if (criteria.id() != null) {
                predicates.add(cb.equal(root.get("id"), criteria.id()));
            }
            if (criteria.onus()) {
                predicates.add(cb.isTrue(root.get("onus")));
            }

            predicates.add(cb.equal(root.get("companyId"), criteria.companyId()));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}