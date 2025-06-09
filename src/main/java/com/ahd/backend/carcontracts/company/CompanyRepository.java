package com.ahd.backend.carcontracts.company;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CompanyRepository extends JpaRepository<Company, Long> , JpaSpecificationExecutor<Company> {
}
