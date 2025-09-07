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

        keywordSearch(c.keyword(), cb, p,
                root.get("name")
        );
        equal(root.get("model"),        c.model(),        p, cb);
        equal(root.get("plateNumber"),  c.plateNumber(),  p, cb);
        equal(root.get("chassisNumber"),c.chassisNumber(),p, cb);
        equal(root.get("type"),         c.type(),         p, cb);
        equal(root.get("color"),        c.color(),        p, cb);
        equal(root.get("engineType"),   c.engineType(),   p, cb);
        equal(root.get("origin"),       c.origin(),       p, cb);
        between(root.get("kilometers"),     c.minKm(),        c.maxKm(),        p, cb);
        between(root.get("cylinderCount"),  c.minCylinders(), c.maxCylinders(), p, cb);
        if (c.deleted() != null) {
            equal(root.get("deleted"), c.deleted(), p, cb);
        }
         equal(root.get("companyId"), c.companyId(), p, cb);

    }

}
