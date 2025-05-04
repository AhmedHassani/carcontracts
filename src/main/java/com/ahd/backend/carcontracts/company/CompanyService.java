package com.ahd.backend.carcontracts.company;


import com.ahd.backend.carcontracts.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    /* ----------------------------------------------------
     * إنشاء شركة جديدة
     * -------------------------------------------------- */
    @Transactional
    public CompanyResponse createCompany(CompanyRequest dto) {
        Company company = Company.builder()
                .companyName(dto.companyName())
                .ownerName(dto.ownerName())
                .ownerContact(dto.ownerContact())
                .userCount(dto.userCount())
                .subscriptionDate(dto.subscriptionDate())
                .expirationDate(dto.expirationDate())
                .companyLocation(dto.companyLocation())
                .status(dto.status() != null ? dto.status() : CompanyStatus.ACTIVE)
                .build();
        Company savedCompany = companyRepository.save(company);
        return mapToDto(savedCompany);
    }

    /* ----------------------------------------------------
     * جلب جميع الشركات
     * -------------------------------------------------- */
    public List<CompanyResponse> getAllCompanies() {
        return companyRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /* ----------------------------------------------------
     * جلب شركة بحسب المعرّف
     * -------------------------------------------------- */
    public CompanyResponse getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + id));

        return mapToDto(company);
    }

    /* ----------------------------------------------------
     * Mapping: Entity → ResponseDTO
     * -------------------------------------------------- */
    private CompanyResponse mapToDto(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .companyName(company.getCompanyName())
                .ownerName(company.getOwnerName())
                .ownerContact(company.getOwnerContact())
                .userCount(company.getUserCount())
                .subscriptionDate(company.getSubscriptionDate())
                .expirationDate(company.getExpirationDate())
                .companyLocation(company.getCompanyLocation())
                .status(company.getStatus())
                .build();
    }
}
