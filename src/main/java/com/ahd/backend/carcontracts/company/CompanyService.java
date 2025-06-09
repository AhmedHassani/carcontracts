package com.ahd.backend.carcontracts.company;

import com.ahd.backend.carcontracts.exception.ResourceNotFoundException;
import com.ahd.backend.carcontracts.util.base.ApiResponse;
import com.ahd.backend.carcontracts.util.base.Pagination;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

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
     * جلب جميع الشركات مع البحث والفلترة والترتيب
     * -------------------------------------------------- */
    public ApiResponse<List<CompanyResponse>> getAllCompanies(CompanySearchCriteria criteria, Pageable pageable) {
        // Create sort object based on criteria
        Sort sort = Sort.by(
            criteria.getSortDirection().equalsIgnoreCase("DESC") ? 
            Sort.Direction.DESC : Sort.Direction.ASC,
            criteria.getSortBy()
        );
        
        // Create pageable with sort
        PageRequest pageRequest = PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            sort
        );
        
        // Build specification and get results
        Specification<Company> spec = CompanySpecification.buildSpecification(criteria);
        Page<CompanyResponse> pageResult = companyRepository
                .findAll(spec, pageRequest)
                .map(this::mapToDto);

        // Create pagination info
        Pagination pagination = new Pagination(
                pageResult.getNumber(),
                pageResult.getTotalPages(),
                pageResult.getTotalElements()
        );

        // Return response
        return ApiResponse.<List<CompanyResponse>>builder()
                .success(true)
                .message("OK")
                .code(HttpStatus.OK.value())
                .data(pageResult.getContent())
                .pagination(pagination)
                .date(Instant.now())
                .build();
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

    @Transactional
    public CompanyResponse updateCompany(Long id, UpdateCompanyRequest request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + id));

        request.companyName().ifPresent(company::setCompanyName);
        request.ownerName().ifPresent(company::setOwnerName);
        request.ownerContact().ifPresent(company::setOwnerContact);
        request.userCount().ifPresent(company::setUserCount);
        request.subscriptionDate().ifPresent(company::setSubscriptionDate);
        request.expirationDate().ifPresent(company::setExpirationDate);
        request.companyLocation().ifPresent(company::setCompanyLocation);
        request.status().ifPresent(company::setStatus);

        Company updatedCompany = companyRepository.save(company);
        return mapToDto(updatedCompany);
    }

    @Transactional
    public ApiResponse<Void> deleteCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + id));
        companyRepository.delete(company);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Company deleted successfully.")
                .code(HttpStatus.OK.value())
                .date(Instant.now())
                .build();
    }
}
