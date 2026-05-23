package com.ahd.backend.carcontracts.contract.repository;

import com.ahd.backend.carcontracts.contract.model.Contracts;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface ContractsRepository extends JpaRepository<Contracts, Long>, JpaSpecificationExecutor<Contracts> {

// This fetches ALL contracts (deleted AND not deleted)
@Query(value = "SELECT TOP 1 c.contract_number FROM car_contracts c WHERE c.company_id = :companyId AND c.contract_number IS NOT NULL ORDER BY CAST(c.contract_number AS BIGINT) DESC", nativeQuery = true)
String findLastContractNumberByCompanyId(@Param("companyId") Long companyId);

    Contracts findByPaymentPlanId(Long id);
    @EntityGraph(attributePaths = {
            "seller",
            "buyer",
            "guarantor",
            "car",
            "paymentPlan",
            "paymentPlan.installments"
    })
    Optional<Contracts> findByIdAndCompanyId(Long id, Long companyId);
  
    @Query("""
           SELECT COUNT(c)
           FROM Contracts c
           WHERE c.contractDate BETWEEN :startDate AND :endDate
             AND c.companyId = :companyId
           """)
    long countContractsBetweenDates(@Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate,
                                    @Param("companyId") Long companyId);

    @Query("""
           SELECT c.contractDate AS day, COUNT(c.id)
           FROM Contracts c
           WHERE c.contractDate BETWEEN :start AND :end
             AND c.companyId = :companyId
           GROUP BY c.contractDate
           ORDER BY c.contractDate
           """)
    List<Object[]> countByDay(@Param("start") LocalDate start,
                              @Param("end") LocalDate end,
                              @Param("companyId") Long companyId);

    @Query(value = """
        SELECT 
            FORMAT(contract_date, 'yyyy-MM') AS month,
            COUNT(id) AS total_contracts
        FROM car_contracts
        WHERE contract_date BETWEEN :start AND :end
          AND company_id = :companyId
        GROUP BY FORMAT(contract_date, 'yyyy-MM')
        ORDER BY month
    """, nativeQuery = true)
    List<Object[]> countByMonth(@Param("start") LocalDate start,
                                @Param("end") LocalDate end,
                                @Param("companyId") Long companyId);
}

