package com.ahd.backend.carcontracts.authorization.repository;

import com.ahd.backend.carcontracts.authorization.model.Authorization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AuthorizationRepository extends JpaRepository<Authorization, Long>, JpaSpecificationExecutor<Authorization> {
    boolean existsByAuthorizationNumber(Long authorizationNumber);
    Optional<Authorization> findByAuthorizationNumber(Long authorizationNumber);

}
