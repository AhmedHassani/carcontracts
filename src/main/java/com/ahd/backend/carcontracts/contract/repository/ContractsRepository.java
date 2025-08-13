package com.ahd.backend.carcontracts.contract.repository;

import com.ahd.backend.carcontracts.contract.model.Contracts;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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

}
