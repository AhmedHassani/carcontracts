package com.ahd.backend.carcontracts.contract;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractImageService {

    private final ContractImageRepository contractImageRepository;

    public ContractImage updateContractImage(Long id, ContractImageDTO dto) {
        ContractImage existing = contractImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Image not found with id " + id));

        existing.setImage(dto.getImage());
        existing.setDeleted(dto.isDeleted());

        return contractImageRepository.save(existing);
    }

    public void deleteContractImage(Long id) {
        ContractImage existing = contractImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Image not found with id " + id));

        existing.setDeleted(true); // soft delete
        contractImageRepository.save(existing);
    }

    public List<ContractImage> getImagesByContractId(Long contractId) {
        return contractImageRepository.findByContractIdAndDeletedFalse(contractId);
    }

}
