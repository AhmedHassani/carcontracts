package com.ahd.backend.carcontracts.car.service;

import com.ahd.backend.carcontracts.car.dto.CarSearchCriteria;
import com.ahd.backend.carcontracts.car.model.Car;
import com.ahd.backend.carcontracts.util.base.AbstractSpecification;
import com.ahd.backend.carcontracts.util.base.BaseCriteria;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CarSpecification extends AbstractSpecification<CarSearchCriteria, Car> {
    
    private final CarSearchCriteria criteria;
    
    public CarSpecification(CarSearchCriteria criteria) {
        super(criteria);
        this.criteria = criteria;
    }
    
    @Override
    protected void build(Root<Car> root, CriteriaBuilder cb, List<Predicate> p) {
        Join<Object, Object> possessorJoin = root.join("currentPossessor", JoinType.LEFT);
        
        // Use the stored criteria directly instead of casting
        if (criteria.keyword() != null && !criteria.keyword().isBlank()) {
            String pattern = "%" + criteria.keyword().toLowerCase() + "%";
            Predicate keywordPredicate = cb.or(
                cb.like(cb.lower(root.get("plateNumber")), pattern),
                cb.like(cb.lower(root.get("chassisNumber")), pattern),
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("model")), pattern),
                cb.like(cb.lower(possessorJoin.get("firstName")), pattern),
                cb.like(cb.lower(possessorJoin.get("fatherName")), pattern),
                cb.like(cb.lower(possessorJoin.get("grandfatherName")), pattern),
                cb.like(cb.lower(possessorJoin.get("fourthName")), pattern),
                cb.like(cb.lower(possessorJoin.get("surname")), pattern),
                cb.like(cb.lower(possessorJoin.get("phoneNumber")), pattern)
            );
            p.add(keywordPredicate);
        }
        
        if (criteria.possessorName() != null && !criteria.possessorName().isBlank()) {
            String pattern = "%" + criteria.possessorName().toLowerCase() + "%";
            Predicate namePredicate = cb.or(
                cb.like(cb.lower(possessorJoin.get("firstName")), pattern),
                cb.like(cb.lower(possessorJoin.get("fatherName")), pattern),
                cb.like(cb.lower(possessorJoin.get("grandfatherName")), pattern),
                cb.like(cb.lower(possessorJoin.get("fourthName")), pattern),
                cb.like(cb.lower(possessorJoin.get("surname")), pattern),
                cb.like(cb.lower(cb.concat(possessorJoin.get("firstName"), 
                    cb.concat(" ", possessorJoin.get("fatherName")))), pattern)
            );
            p.add(namePredicate);
        }
        
        if (criteria.possessorPhone() != null && !criteria.possessorPhone().isBlank()) {
            String pattern = "%" + criteria.possessorPhone() + "%";
            p.add(cb.like(possessorJoin.get("phoneNumber"), pattern));
        }
        
        if (criteria.carPrice() != null) {
            p.add(cb.equal(
                cb.function("CONVERT", Long.class, 
                    root.get("carPrice"), cb.literal("SIGNED")), 
                criteria.carPrice()
            ));
        }
        
        if (criteria.plateNumber() != null && !criteria.plateNumber().isBlank()) {
            String pattern = "%" + criteria.plateNumber().toLowerCase() + "%";
            p.add(cb.like(cb.lower(root.get("plateNumber")), pattern));
        }
        
        if (criteria.chassisNumber() != null && !criteria.chassisNumber().isBlank()) {
            String pattern = "%" + criteria.chassisNumber().toLowerCase() + "%";
            p.add(cb.like(cb.lower(root.get("chassisNumber")), pattern));
        }
        
        if (criteria.description() != null && !criteria.description().isBlank()) {
            String pattern = "%" + criteria.description().toLowerCase() + "%";
            p.add(cb.like(cb.lower(root.get("description")), pattern));
        }
        
        if (criteria.name() != null && !criteria.name().isBlank()) {
            String pattern = "%" + criteria.name().toLowerCase() + "%";
            p.add(cb.like(cb.lower(root.get("name")), pattern));
        }
        
        if (criteria.model() != null && !criteria.model().isBlank()) {
            p.add(cb.equal(cb.lower(root.get("model")), criteria.model().toLowerCase()));
        }
        
        if (criteria.type() != null && !criteria.type().isBlank()) {
            p.add(cb.equal(cb.lower(root.get("type")), criteria.type().toLowerCase()));
        }
        
        if (criteria.color() != null && !criteria.color().isBlank()) {
            p.add(cb.equal(cb.lower(root.get("color")), criteria.color().toLowerCase()));
        }
        
        if (criteria.engineType() != null && !criteria.engineType().isBlank()) {
            p.add(cb.equal(cb.lower(root.get("engineType")), criteria.engineType().toLowerCase()));
        }
        
        if (criteria.origin() != null && !criteria.origin().isBlank()) {
            p.add(cb.equal(cb.lower(root.get("origin")), criteria.origin().toLowerCase()));
        }
        
        if (criteria.minKm() != null || criteria.maxKm() != null) {
            if (criteria.minKm() != null && criteria.maxKm() != null) {
                p.add(cb.between(root.get("kilometers"), criteria.minKm(), criteria.maxKm()));
            } else if (criteria.minKm() != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("kilometers"), criteria.minKm()));
            } else if (criteria.maxKm() != null) {
                p.add(cb.lessThanOrEqualTo(root.get("kilometers"), criteria.maxKm()));
            }
        }
        
        if (criteria.minCylinders() != null || criteria.maxCylinders() != null) {
            if (criteria.minCylinders() != null && criteria.maxCylinders() != null) {
                p.add(cb.between(root.get("cylinderCount"), criteria.minCylinders(), criteria.maxCylinders()));
            } else if (criteria.minCylinders() != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("cylinderCount"), criteria.minCylinders()));
            } else if (criteria.maxCylinders() != null) {
                p.add(cb.lessThanOrEqualTo(root.get("cylinderCount"), criteria.maxCylinders()));
            }
        }
        
        if (criteria.deleted() != null) {
            p.add(cb.equal(root.get("deleted"), criteria.deleted()));
        }
        
        if (criteria.status() != null && !criteria.status().isBlank()) {
            if (criteria.status().contains(",")) {
                List<String> statusList = Arrays.stream(criteria.status().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
                List<Predicate> statusPredicates = new ArrayList<>();
                for (String status : statusList) {
                    statusPredicates.add(cb.equal(root.get("status"), status));
                }
                p.add(cb.or(statusPredicates.toArray(new Predicate[0])));
            } else {
                p.add(cb.equal(root.get("status"), criteria.status()));
            }
        }
        
        if (criteria.companyId() != null) {
            p.add(cb.equal(root.get("companyId"), criteria.companyId()));
        }
    }
}