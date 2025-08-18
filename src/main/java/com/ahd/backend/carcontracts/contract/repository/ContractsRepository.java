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
    @EntityGraph(attributePaths = {
            "seller",
            "buyer",
            "guarantor",
            "car",
            "paymentPlan",
            "paymentPlan.installments"
    })
    Optional<Contracts> findById(Long id);
    @Query("SELECT COUNT(c) FROM Contracts c WHERE c.contractDate BETWEEN :startDate AND :endDate")
    long countContractsBetweenDates(@Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);

    @Query("""
     SELECT contractDate as day, COUNT(id)
                    FROM Contracts
                    WHERE  contractDate BETWEEN :start AND :end
                    GROUP BY contractDate
                    ORDER BY contractDate
""")
    List<Object[]> countByDay(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);
    @Query(value = """
    SELECT 
        FORMAT(contract_date, 'yyyy-MM') AS month,
        COUNT(id) AS total_contracts
    FROM car_contracts
    WHERE contract_date BETWEEN :start AND :end
    GROUP BY FORMAT(contract_date, 'yyyy-MM')
    ORDER BY month
""", nativeQuery = true)
    List<Object[]> countByMonth(@Param("start") LocalDate start, @Param("end") LocalDate end);

}
