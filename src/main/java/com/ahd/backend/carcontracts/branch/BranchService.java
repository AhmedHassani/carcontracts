package com.ahd.backend.carcontracts.branch;



import com.ahd.backend.carcontracts.company.Company;
import com.ahd.backend.carcontracts.company.CompanyRepository;
import com.ahd.backend.carcontracts.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;
    private final CompanyRepository companyRepository;

    public BranchResponseDTO createBranch(BranchRequestDTO dto) {
        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        Branch branch = Branch.builder()
                .name(dto.getName())
                .address(dto.getAddress())
                .company(company)
                .build();

        branch = branchRepository.save(branch);

        return BranchResponseDTO.builder()
                .id(branch.getId())
                .name(branch.getName())
                .address(branch.getAddress())
                .companyId(company.getId())
                .build();
    }

    public List<BranchResponseDTO> getAllBranches() {
        return branchRepository.findAll()
                .stream().map(branch -> BranchResponseDTO.builder()
                        .id(branch.getId())
                        .name(branch.getName())
                        .address(branch.getAddress())
                        .companyId(branch.getCompany().getId())
                        .build())
                .collect(Collectors.toList());
    }


}

