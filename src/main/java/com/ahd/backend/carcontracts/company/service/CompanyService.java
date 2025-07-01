package com.ahd.backend.carcontracts.company.service;

import com.ahd.backend.carcontracts.appuser.models.AppUser;
import com.ahd.backend.carcontracts.appuser.models.CreateUserRequest;
import com.ahd.backend.carcontracts.appuser.models.Role;
import com.ahd.backend.carcontracts.appuser.repository.RoleRepository;
import com.ahd.backend.carcontracts.appuser.repository.UserRepository;
import com.ahd.backend.carcontracts.appuser.services.AuthService;
import com.ahd.backend.carcontracts.company.model.UpdateCompanyRequest;
import com.ahd.backend.carcontracts.company.mapper.CompanyMapper;
import com.ahd.backend.carcontracts.company.model.*;
import com.ahd.backend.carcontracts.company.repository.CompanyRepository;
import com.ahd.backend.carcontracts.company.repository.CompanyUserRepository;
import com.ahd.backend.carcontracts.exception.ResourceNotFoundException;
import com.ahd.backend.carcontracts.util.base.ApiResponse;
import com.ahd.backend.carcontracts.util.base.Pagination;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final AuthService authService;
    private final RoleRepository roleRepository;
    private final CompanyUserRepository companyUserRepository;
    private final UserRepository userRepository;
    private final CompanyMapper companyMapper;
    private final PasswordEncoder passwordEncoder;


    public CompanyResponse createCompany(CompanyRequest request) {
        //log.info("Creating company: {}", request.companyName());
        Role companyRole = roleRepository.findByName("ROLE_COMPANY")
                .orElseThrow(() -> new ResourceNotFoundException("Company role not found"));
        var createUser = CreateUserRequest.builder()
                .username(request.companyUsername())
                .password(request.companyPassword())
                .email(request.companyEmail())
                .phone(request.ownerContact())
                .fullName(request.ownerName())
                .roleIds(Set.of(companyRole.getId()))
                .build();
        var user = authService.createUser(createUser);
        Company company = companyMapper.toEntity(request);
        company.setStatus(CompanyStatus.ACTIVE);
        Company savedCompany = companyRepository.saveAndFlush(company);
        CompanyUser relation = CompanyUser.builder()
                .company(savedCompany)
                .user(user)
                .role(CompanyUserRole.OWNER)
                .build();
        companyUserRepository.save(relation);
        return companyMapper.toResponse(savedCompany,request.companyUsername(), request.companyPassword(), request.companyEmail());
    }


    public ApiResponse<List<CompanyResponse>> getAllCompanies(CompanySearchCriteria criteria, Pageable pageable) {
        Sort sort = Sort.by(
            criteria.getSortDirection().equalsIgnoreCase("DESC") ? 
            Sort.Direction.DESC : Sort.Direction.ASC,
            criteria.getSortBy()
        );
        // Build specification and get results
        Specification<Company> spec = CompanySpecification.buildSpecification(criteria);
        Page<CompanyResponse> pageResult = companyRepository
                .findAll(spec, PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        sort
                )).map(this::mapToDto);
        // Return response
        return ApiResponse.<List<CompanyResponse>>builder()
                .success(true)
                .message("OK")
                .code(HttpStatus.OK.value())
                .data(pageResult.getContent())
                .pagination(new Pagination(
                        pageResult.getNumber(),
                        pageResult.getTotalPages(),
                        pageResult.getTotalElements()
                ))
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
    public CompanyResponse updateCompany(Long companyId, UpdateCompanyRequest request) {
        final Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + companyId));
        applyBasicUpdates(company, request);
        boolean expirationChanged = applyDateUpdates(company, request);
        if (expirationChanged) {
            recalculateStatus(company);
        }
        AppUser user = updateOwnerUser(company, request);
        userRepository.save(user);
        Company saved = companyRepository.save(company);
        return companyMapper.toResponse(
                saved,
                request.companyUsername().orElse(null),
                request.companyPassword().orElse(null),
                request.companyEmail().orElse(null)
        );
    }

    private void applyBasicUpdates(Company company, UpdateCompanyRequest req) {
        req.companyName().ifPresent(company::setCompanyName);
        req.ownerName().ifPresent(company::setOwnerName);
        req.ownerContact().ifPresent(company::setOwnerContact);
        req.userCount().ifPresent(company::setUserCount);
        req.companyLocation().ifPresent(company::setCompanyLocation);
    }

    private void recalculateStatus(Company company) {
        LocalDate expiry = company.getExpirationDate();
        CompanyStatus status = (expiry != null && !expiry.isBefore(LocalDate.now()))
                ? CompanyStatus.ACTIVE
                : CompanyStatus.EXPIRED;
        company.setStatus(status);
    }

    private AppUser updateOwnerUser(Company company, UpdateCompanyRequest req) {
        CompanyUser ownerLink = companyUserRepository
                .findByCompanyAndRole(company, CompanyUserRole.OWNER)
                .orElseThrow(() -> new ResourceNotFoundException("Owner user not found for company: " + company.getId()));
        AppUser user = ownerLink.getUser();
        req.companyUsername().ifPresent(user::setUsername);
        req.companyPassword().ifPresent(raw -> user.setPassword(passwordEncoder.encode(raw)));
        req.companyEmail().ifPresent(user::setEmail);
        user.setFullName(req.ownerName().orElse(user.getFullName()));
        user.setPhone(req.ownerContact().orElse(user.getPhone()));
        return user;
    }


    private boolean applyDateUpdates(Company company, UpdateCompanyRequest req) {
        if (req.expirationDate().isEmpty()) return false;
        company.setExpirationDate(req.expirationDate().get());
        company.setSubscriptionDate(LocalDate.now());
        return true;
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

    /* ----------------------------------------------------
     * Add a user to a company
     * -------------------------------------------------- */
//    @Transactional
//    public void addUserToCompany(Long companyId, Long userId, CompanyUserRole role) {
//        if (companyUserRepository.existsByCompanyIdAndUserId(companyId, userId)) {
//            throw new IllegalArgumentException("User is already associated with this company");
//        }
//
//        Company company = companyRepository.findById(companyId)
//                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + companyId));
//
//        AppUser user = authService.getUserById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
//
//        CompanyUser companyUser = CompanyUser.builder()
//                .company(company)
//                .user(user)
//                .role(role)
//                .build();
//
//        companyUserRepository.save(companyUser);
//    }
    @Transactional
    public void addUserToCompany(Long companyId, Long userId, CompanyUserRole role) {
        if (companyUserRepository.existsByCompanyIdAndUserId(companyId, userId)) {
            throw new IllegalArgumentException("User is already associated with this company");
        }
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + companyId));
        long currentUserCount = companyUserRepository.countByCompanyId(companyId);
        if (currentUserCount >= company.getUserCount()) {
            throw new IllegalStateException("The company has reached its maximum allowed users.");
        }
        AppUser user = authService.getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        CompanyUser companyUser = CompanyUser.builder()
                .company(company)
                .user(user)
                .role(role)
                .build();
        companyUserRepository.save(companyUser);
    }
    /* ----------------------------------------------------
     * Remove a user from a company
     * -------------------------------------------------- */
    @Transactional
    public void removeUserFromCompany(Long companyId, Long userId) {
        CompanyUser companyUser = companyUserRepository.findByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not associated with this company"));
        // Don't allow removing the company owner
        if (companyUser.getRole() == CompanyUserRole.OWNER) {
            throw new IllegalArgumentException("Cannot remove the company owner");
        }
        companyUserRepository.delete(companyUser);
    }

    /* ----------------------------------------------------
     * Update a user's role in a company
     * -------------------------------------------------- */
    @Transactional
    public void updateUserCompanyRole(Long companyId, Long userId, CompanyUserRole newRole) {
        CompanyUser companyUser = companyUserRepository.findByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not associated with this company"));
        // Don't allow changing the company owner's role
        if (companyUser.getRole() == CompanyUserRole.OWNER) {
            throw new IllegalArgumentException("Cannot change the company owner's role");
        }
        companyUser.setRole(newRole);
        companyUserRepository.save(companyUser);
    }

    /* ----------------------------------------------------
     * Get all users in a company
     * -------------------------------------------------- */
    public List<CompanyUser> getCompanyUsers(Long companyId) {
        return companyUserRepository.findByCompanyId(companyId);
    }

    /**
     * Check if a user is in a company
     */
    public boolean isUserInCompany(String username, Long companyId) {
        return userRepository.findByUsername(username)
                .map(user -> companyUserRepository.existsByCompanyIdAndUserId(companyId, user.getId()))
                .orElse(false);
    }

    /**
     * Check if a user has a specific role in a company
     */
    public boolean isUserInCompanyWithRole(String username, Long companyId, CompanyUserRole role) {
        return userRepository.findByUsername(username)
                .map(user -> companyUserRepository.findByCompanyIdAndUserId(companyId, user.getId())
                        .map(cu -> cu.getRole() == role)
                        .orElse(false))
                .orElse(false);
    }
}
