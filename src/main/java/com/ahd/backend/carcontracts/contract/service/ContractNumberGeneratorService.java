package com.ahd.backend.carcontracts.contract.service;

import com.ahd.backend.carcontracts.company.model.Company;
import com.ahd.backend.carcontracts.company.repository.CompanyRepository;
import com.ahd.backend.carcontracts.contract.repository.ContractsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContractNumberGeneratorService {

    private final ContractsRepository contractRepository;
    private final CompanyRepository companyRepository;

    /**
     * Generate contract number in format: {companyCode}{9-digit sequence}
     * Example: code=999 -> 999000000001, 999000000002, etc.
     */
    @Transactional
    public String generateContractNumber(Long companyId) {
        // Get company code
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + companyId));
        
        String companyCode = company.getCode();
        
        // Validate company code is 3 digits
        if (companyCode == null || !companyCode.matches("\\d{3}")) {
            throw new RuntimeException("Company code must be 3 digits. Current: " + companyCode);
        }
        
        // Find the last contract number for this company
        String lastContractNumber = contractRepository.findLastContractNumberByCompanyId(companyId);
        
        int nextSequence = 1; // Start from 1
        
        if (lastContractNumber != null && lastContractNumber.startsWith(companyCode)) {
            // Extract the 9-digit sequence part
            String sequencePart = lastContractNumber.substring(3); // After the 3-digit code
            try {
                int lastSequence = Integer.parseInt(sequencePart);
                nextSequence = lastSequence + 1;
            } catch (NumberFormatException e) {
                // Fallback to 1 if parsing fails
                nextSequence = 1;
            }
        }
        
        // Validate sequence doesn't exceed 999,999,999 (9 digits max)
        if (nextSequence > 999_999_999) {
            throw new RuntimeException("Maximum contract limit reached for company code: " + companyCode);
        }
        
        // Format: code (3 digits) + sequence (9 digits with leading zeros)
        return companyCode + String.format("%09d", nextSequence);
    }
}