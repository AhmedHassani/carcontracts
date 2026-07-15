package com.ahd.backend.carcontracts.authorization.repository;

import com.ahd.backend.carcontracts.authorization.model.Authorization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AuthorizationRepository extends JpaRepository<Authorization, Long>, JpaSpecificationExecutor<Authorization> {
    boolean existsByAuthorizationNumberAndCompanyId(Long authorizationNumber, Long companyId);
    Optional<Authorization> findByIdAndCompanyId(Long id, Long companyId);
    boolean  existsByIdAndCompanyId(Long id , Long companyId);

    @Query("SELECT MAX(a.authorizationNumber) FROM Authorization a WHERE a.companyId = :companyId")
    Long findMaxAuthorizationNumberByCompanyId(@Param("companyId") Long companyId);

}
