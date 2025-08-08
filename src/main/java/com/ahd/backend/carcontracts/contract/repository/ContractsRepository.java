package com.ahd.backend.carcontracts.contract.repository;

import com.ahd.backend.carcontracts.contract.model.Contracts;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

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

}
