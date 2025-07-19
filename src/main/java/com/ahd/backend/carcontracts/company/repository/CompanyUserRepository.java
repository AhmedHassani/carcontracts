package com.ahd.backend.carcontracts.company.repository;

import com.ahd.backend.carcontracts.company.model.Company;
import com.ahd.backend.carcontracts.company.enums.CompanyUserRole;

import com.ahd.backend.carcontracts.company.model.CompanyUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CompanyUserRepository extends JpaRepository<CompanyUser, CompanyUser.CompanyUserId>,JpaSpecificationExecutor<CompanyUser> {
    long countByCompanyId(Long companyId);
    Optional<CompanyUser> findByCompanyAndRole(Company company, CompanyUserRole role);
    Page<CompanyUser> findByCompanyId(Long companyId, Pageable pageable);
    CompanyUser findByUserId(Long userId);
    Optional<CompanyUser> findByCompanyIdAndUserId(Long companyId, Long userId);
    @Query("SELECT cu FROM CompanyUser cu WHERE cu.company.id = ?1 AND cu.role = 'OWNER'")
    Optional<CompanyUser> findCompanyOwner(Long companyId);
    boolean existsByCompanyIdAndUserId(Long companyId, Long userId);
} 