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
                root.get("name"),
                root.get("model"),
                root.get("plateNumber"),
                root.get("chassisNumber"));
        equal(root.get("type"), c.type(),p, cb);
        equal(root.get("color"),c.color(),p, cb);
        between(root.get("kilometers"),
                c.minKm(), c.maxKm(),
                p, cb);
        if (c.deleted() != null)
            equal(root.get("deleted"), c.deleted(), p, cb);
    }
}
