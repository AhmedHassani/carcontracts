package com.ahd.backend.carcontracts.person.service;

import com.ahd.backend.carcontracts.person.dto.PersonSearchCriteria;
import com.ahd.backend.carcontracts.person.model.Person;
import com.ahd.backend.carcontracts.util.base.AbstractSpecification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;


public class PersonSpecification extends AbstractSpecification<PersonSearchCriteria, Person> {

    public PersonSpecification(PersonSearchCriteria criteria) {
        super(criteria);
    }

    @Override
    protected void build(Root<Person> root, CriteriaBuilder cb, List<Predicate> predicates) {
        keywordSearch(c.keyword(), cb, predicates,
                root.get("firstName"),
                root.get("fatherName"),
                root.get("fourthName"),
                root.get("surname"));
        equal(root.get("phoneNumber"), c.phoneNumber(),predicates, cb);
        equal(root.get("nationalId"),c.nationalId(),predicates, cb);
        equal(root.get("residenceCardNo"),c.residenceCardNo(),predicates, cb);
    }

}
