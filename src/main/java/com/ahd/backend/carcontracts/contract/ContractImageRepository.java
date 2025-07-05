package com.ahd.backend.carcontracts.contract;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContractImageRepository extends JpaRepository<ContractImage, Long> {
    List<ContractImage> findByContractIdAndDeletedFalse(Long contractId);
}
