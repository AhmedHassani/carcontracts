package com.ahd.backend.carcontracts.company.repository;



import com.ahd.backend.carcontracts.company.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> , JpaSpecificationExecutor<Company> {
    List<Company> findByExpirationDateBefore(LocalDate date);
}
