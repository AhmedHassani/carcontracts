package com.ahd.backend.carcontracts.person;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long>,
        JpaSpecificationExecutor<Person> {
    Optional<Person> findByNationalIdAndDeletedFalse(String nationalId);
}
