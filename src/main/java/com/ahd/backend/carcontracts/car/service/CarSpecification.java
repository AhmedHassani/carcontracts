package com.ahd.backend.carcontracts.car.service;

import com.ahd.backend.carcontracts.car.dto.CarSearchCriteria;
import com.ahd.backend.carcontracts.car.model.Car;
import com.ahd.backend.carcontracts.util.base.AbstractSpecification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;

public class CarSpecification extends AbstractSpecification<CarSearchCriteria, Car> {

    public CarSpecification(CarSearchCriteria criteria) {
        super(criteria);
    }

    @Override
    protected void build(Root<Car> root, CriteriaBuilder cb, List<Predicate> p) {

        // Keyword search across multiple fields
        if (c.keyword() != null && !c.keyword().isBlank()) {
            String pattern = "%" + c.keyword().toLowerCase() + "%";
            Predicate keywordPredicate = cb.or(
                    cb.like(cb.lower(root.get("plateNumber")), pattern),
                    cb.like(cb.lower(root.get("chassisNumber")), pattern),
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("model")), pattern)
            );
            p.add(keywordPredicate);
        }

        // Individual field searches with partial matching
        if (c.plateNumber() != null && !c.plateNumber().isBlank()) {
            String pattern = "%" + c.plateNumber().toLowerCase() + "%";
            p.add(cb.like(cb.lower(root.get("plateNumber")), pattern));
        }

        if (c.chassisNumber() != null && !c.chassisNumber().isBlank()) {
            String pattern = "%" + c.chassisNumber().toLowerCase() + "%";
            p.add(cb.like(cb.lower(root.get("chassisNumber")), pattern));
        }

        // Exact matches for other fields
        if (c.model() != null && !c.model().isBlank()) {
            p.add(cb.equal(cb.lower(root.get("model")), c.model().toLowerCase()));
        }

        if (c.type() != null && !c.type().isBlank()) {
            p.add(cb.equal(cb.lower(root.get("type")), c.type().toLowerCase()));
        }

        if (c.color() != null && !c.color().isBlank()) {
            p.add(cb.equal(cb.lower(root.get("color")), c.color().toLowerCase()));
        }

        if (c.engineType() != null && !c.engineType().isBlank()) {
            p.add(cb.equal(cb.lower(root.get("engineType")), c.engineType().toLowerCase()));
        }

        if (c.origin() != null && !c.origin().isBlank()) {
            p.add(cb.equal(cb.lower(root.get("origin")), c.origin().toLowerCase()));
        }

        // Range searches
        if (c.minKm() != null || c.maxKm() != null) {
            if (c.minKm() != null && c.maxKm() != null) {
                p.add(cb.between(root.get("kilometers"), c.minKm(), c.maxKm()));
            } else if (c.minKm() != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("kilometers"), c.minKm()));
            } else if (c.maxKm() != null) {
                p.add(cb.lessThanOrEqualTo(root.get("kilometers"), c.maxKm()));
            }
        }

        if (c.minCylinders() != null || c.maxCylinders() != null) {
            if (c.minCylinders() != null && c.maxCylinders() != null) {
                p.add(cb.between(root.get("cylinderCount"), c.minCylinders(), c.maxCylinders()));
            } else if (c.minCylinders() != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("cylinderCount"), c.minCylinders()));
            } else if (c.maxCylinders() != null) {
                p.add(cb.lessThanOrEqualTo(root.get("cylinderCount"), c.maxCylinders()));
            }
        }

        // Boolean/deleted filter
        if (c.deleted() != null) {
            p.add(cb.equal(root.get("deleted"), c.deleted()));
        }
        if (c.status() != null) {
            p.add(cb.equal(root.get("status"), c.status()));
        }

        // Company ID filter
        if (c.companyId() != null) {
            p.add(cb.equal(root.get("companyId"), c.companyId()));
        }
    }
}