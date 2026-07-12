package com.ahd.backend.carcontracts.company.service;

import com.ahd.backend.carcontracts.appuser.services.RolePermissionService;
import com.ahd.backend.carcontracts.audit.Auditable;
import com.ahd.backend.carcontracts.util.Helper;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;

import com.ahd.backend.carcontracts.appuser.models.AppUser;
import com.ahd.backend.carcontracts.appuser.dto.CreateUserRequest;
import com.ahd.backend.carcontracts.appuser.models.Role;
import com.ahd.backend.carcontracts.appuser.repository.RoleRepository;
import com.ahd.backend.carcontracts.appuser.repository.UserRepository;
import com.ahd.backend.carcontracts.appuser.services.AuthService;
import com.ahd.backend.carcontracts.company.dto.*;
import com.ahd.backend.carcontracts.company.enums.CompanyStatus;
import com.ahd.backend.carcontracts.company.enums.CompanyUserRole;
import com.ahd.backend.carcontracts.company.mapper.CompanyMapper;
import com.ahd.backend.carcontracts.company.model.*;
import com.ahd.backend.carcontracts.company.repository.CompanyRepository;
import com.ahd.backend.carcontracts.company.repository.CompanyUserRepository;
import com.ahd.backend.carcontracts.exception.ConflictException;
import com.ahd.backend.carcontracts.exception.ResourceNotFoundException;
import com.ahd.backend.carcontracts.notification.dto.NotificationContext;
import com.ahd.backend.carcontracts.notification.service.NotificationSender;
import com.ahd.backend.carcontracts.notification.service.MessageService;
import com.ahd.backend.carcontracts.util.base.ApiResponse;
import com.ahd.backend.carcontracts.util.base.Pagination;
import com.ahd.backend.carcontracts.company.service.CompanyCodeGenerator; 
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.ahd.backend.carcontracts.company.mapper.CompanyMapper.toCreateUserRequest;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final AuthService authService;
    private final RoleRepository roleRepository;
    private final CompanyUserRepository companyUserRepository;
    private final UserRepository userRepository;
    private final CompanyMapper companyMapper;
    private final PasswordEncoder passwordEncoder;
    private final RolePermissionService rolePermissionService;
    private final Helper helper;
    private final EntityManager em;
    private final CompanyCodeGenerator companyCodeGenerator;
    
    // ✅ ADD NOTIFICATION DEPENDENCIES
    private final NotificationSender notificationSender;
    private final MessageService messageService;

    // ==================== HELPER METHODS FOR CHANGE TRACKING ====================
    
    /**
     * Copy company for change tracking
     */
    private Company copyCompany(Company company) {
        if (company == null) return null;
        
        Company copy = new Company();
        copy.setId(company.getId());
        copy.setCompanyName(company.getCompanyName());
        copy.setOwnerName(company.getOwnerName());
        copy.setOwnerContact(company.getOwnerContact());
        copy.setUserCount(company.getUserCount());
        copy.setCompanyLocation(company.getCompanyLocation());
        copy.setSubscriptionDate(company.getSubscriptionDate());
        copy.setExpirationDate(company.getExpirationDate());
        copy.setStatus(company.getStatus());
        copy.setCode(company.getCode());
        copy.setPaymentCompanyType(company.getPaymentCompanyType());
        
        return copy;
    }

   @Auditable(operation = "انشاء شركة", captureArgs = true, captureResult = true)
    public CompanyResponse createCompany(CompanyRequest request) {
        String generatedCode = companyCodeGenerator.generateNextCode();
        
        Role companyRole = roleRepository.findByName("ROLE_COMPANY")
                .orElseThrow(() -> new ResourceNotFoundException("Company role not found"));
        
        var createUser = toCreateUserRequest(request, companyRole);
        var user = authService.createUser(createUser);
        
        Company company = companyMapper.toEntity(request);
        company.setCode(generatedCode); // Set the auto-generated code
        company.setStatus(CompanyStatus.ACTIVE);
        
        Company savedCompany = companyRepository.saveAndFlush(company);
        
        CompanyUser relation = CompanyUser.builder()
                .company(savedCompany)
                .user(user)
                .role(CompanyUserRole.OWNER)
                .build();
        companyUserRepository.save(relation);
        
        NotificationContext context = notificationSender.createCompanyContext("CREATE", savedCompany);
        notificationSender.notifyCompanyOperation(context);
        
        return companyMapper.toResponse(
                savedCompany,
                request.companyPassword(),
                request.companyUsername(),
                request.companyEmail()
        );
    }

    public ApiResponse<List<CompanyResponse>> getAllCompanies(CompanySearchCriteria criteria, Pageable pageable) {
        Specification<Company> spec = CompanySpecification.buildSpecification(criteria);
        Page<CompanyResponse> pageResult = companyRepository
                .findAll(spec, pageable).map(this::mapToDto);
        return ApiResponse.<List<CompanyResponse>>builder()
                .success(true)
                .message("OK")
                .code(HttpStatus.OK.value())
                .data(pageResult.getContent())
                .pagination(new Pagination(pageResult))
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
        CompanyUser ownerLink = companyUserRepository
                .findByCompanyIdAndRole(company.getId(), CompanyUserRole.OWNER)
                .orElse(null);

        return CompanyResponse.builder()
                .id(company.getId())
                .companyName(company.getCompanyName())
                .ownerName(company.getOwnerName())
                .companyUsername(ownerLink != null && ownerLink.getUser() != null
                        ? ownerLink.getUser().getUsername()
                        : null)
                .ownerContact(company.getOwnerContact())
                .userCount(company.getUserCount())
                .subscriptionDate(company.getSubscriptionDate())
                .expirationDate(company.getExpirationDate())
                .companyLocation(company.getCompanyLocation())
                .status(company.getStatus())
                .companyEmail(ownerLink != null && ownerLink.getUser() != null
                        ? ownerLink.getUser().getEmail()
                        : null)
                .code(company.getCode())
                .paymentCompanyType(company.getPaymentCompanyType())
                .build();
    }

 //////////////
@Transactional
@Auditable(operation = "تحديث معلومات الشركة", captureArgs = true, captureResult = true)
public CompanyResponse updateCompany(Long companyId, UpdateCompanyRequest request) {
    final Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + companyId));
    
    // ✅ Store old company for change tracking
    Company oldCompany = copyCompany(company);
    
    applyBasicUpdates(company, request);
    boolean expirationChanged = applyDateUpdates(company, request);
    if (expirationChanged) {
        recalculateStatus(company);
    }
    AppUser user = updateOwnerUser(company, request);
    userRepository.save(user);
    Company saved = companyRepository.save(company);
    
    // ✅ ADD NOTIFICATION FOR COMPANY UPDATE
    String changeDetails = notificationSender.generateCompanyChangeDetails(oldCompany, saved);
    NotificationContext context = notificationSender.createCompanyContext("UPDATE", saved, changeDetails);
    notificationSender.notifyCompanyOperation(context);
    
    // ✅ ADD SEPARATE NOTIFICATION FOR STATUS CHANGE IF STATUS CHANGED
    if (oldCompany.getStatus() != saved.getStatus()) {
        String statusChangeDetails = messageService.getMessage("notification.company.status.details",
            oldCompany.getStatus() != null ? oldCompany.getStatus().toString() : "غير محدد",
            saved.getStatus() != null ? saved.getStatus().toString() : "غير محدد");
        NotificationContext statusContext = notificationSender.createCompanyContext("STATUS_CHANGE", saved, statusChangeDetails);
        notificationSender.notifyCompanyOperation(statusContext);
    }

    // Get the password to return (either the new one or the existing one)
    String passwordToReturn = request.companyPassword()
            .orElseGet(() -> companyUserRepository.findByCompanyAndRole(company, CompanyUserRole.OWNER)
                    .map(CompanyUser::getUser)
                    .map(AppUser::getPassword)
                    .orElse(null));

    return companyMapper.toResponse(
            saved,
            request.companyUsername().orElse(null),
            passwordToReturn,
            request.companyEmail().orElse(null)
    );
}

private AppUser updateOwnerUser(Company company, UpdateCompanyRequest req) {
    CompanyUser ownerLink = companyUserRepository
            .findByCompanyAndRole(company, CompanyUserRole.OWNER)
            .orElseThrow(() -> new ResourceNotFoundException("Owner user not found for company: " + company.getId()));
    AppUser user = ownerLink.getUser();
    req.companyUsername().ifPresent(user::setUsername);
    
    // Only update password if a new one is provided in the request
    if (req.companyPassword().isPresent()) {
        String newPassword = req.companyPassword().get();
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }
    }
    
    req.companyEmail().ifPresent(user::setEmail);
    user.setFullName(req.ownerName().orElse(user.getFullName()));
    user.setPhone(req.ownerContact().orElse(user.getPhone()));
    return user;
}
 ////////////////

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

    // private AppUser updateOwnerUser(Company company, UpdateCompanyRequest req) {
    //     CompanyUser ownerLink = companyUserRepository
    //             .findByCompanyAndRole(company, CompanyUserRole.OWNER)
    //             .orElseThrow(() -> new ResourceNotFoundException("Owner user not found for company: " + company.getId()));
    //     AppUser user = ownerLink.getUser();
    //     req.companyUsername().ifPresent(user::setUsername);
    //     req.companyPassword().ifPresent(raw -> user.setPassword(passwordEncoder.encode(raw)));
    //     req.companyEmail().ifPresent(user::setEmail);
    //     user.setFullName(req.ownerName().orElse(user.getFullName()));
    //     user.setPhone(req.ownerContact().orElse(user.getPhone()));
    //     return user;
    // }

    private boolean applyDateUpdates(Company company, UpdateCompanyRequest req) {
        if (req.expirationDate().isEmpty()) return false;
        company.setExpirationDate(req.expirationDate().get());
        company.setSubscriptionDate(LocalDate.now());
        return true;
    }

    @Transactional
    @Auditable(operation = "حذف الشركة", captureArgs = true, captureResult = true)
    public ApiResponse<Void> deleteCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + id));
        
        // ✅ Store company info before deletion for notification
        Company companyToDelete = copyCompany(company);
        
        companyRepository.delete(company);
        
        // ✅ ADD NOTIFICATION FOR COMPANY DELETION
        NotificationContext context = notificationSender.createCompanyContext("DELETE", companyToDelete);
        notificationSender.notifyCompanyOperation(context);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Company deleted successfully.")
                .code(HttpStatus.OK.value())
                .date(Instant.now())
                .build();
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void checkExpiredCompanies() {
        LocalDate today = LocalDate.now();
        List<Company> expiredCompanies = companyRepository.findByExpirationDateBefore(today);

        for (Company company : expiredCompanies) {
            Company oldCompany = copyCompany(company);
            company.setStatus(CompanyStatus.EXPIRED);
            companyRepository.save(company);
            
            // ✅ ADD NOTIFICATION FOR COMPANY EXPIRATION
            String statusChangeDetails = messageService.getMessage("notification.company.status.details",
                oldCompany.getStatus() != null ? oldCompany.getStatus().toString() : "غير محدد",
                CompanyStatus.EXPIRED.toString());
            NotificationContext context = notificationSender.createCompanyContext("STATUS_CHANGE", company, statusChangeDetails);
            notificationSender.notifyCompanyOperation(context);
        }
    }
    
    /* ----------------------------------------------------
     * Add a user to a company
     * -------------------------------------------------- */
    @Transactional
    @Auditable(operation = "اضافة موظف للشركة", captureArgs = true, captureResult = true)
    public void addUserToCompany(AddUserToCompanyRequest request) {
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + request.companyId()));
        if (!getCompanyId(request.companyId())) {
            throw new ResourceNotFoundException("Company authorization not found: " + request.companyId());
        }
        long currentUserCount = companyUserRepository.countByCompanyId(request.companyId());
        if (currentUserCount >= company.getUserCount()) {
            throw new ConflictException("The company has reached its maximum allowed users.");
        }
        
        var userInfo = CreateUserRequest.builder()
                .email(request.email())
                .password(request.password())
                .username(request.username())
                .fullName(request.fullName())
                .phone(request.phone())
                .roleIds(Collections.emptySet())
                .build();
        var user = authService.createUser(userInfo);
        em.flush();
        CompanyUser companyUser = CompanyUser.builder()
                .company(company)
                .user(user)
                .role(CompanyUserRole.EMPLOYEE)
                .build();
        companyUserRepository.save(companyUser);
        em.flush();
        
        // ✅ ADD NOTIFICATION FOR USER ADDED TO COMPANY
        String userDetails = messageService.getMessage("notification.company.user.details",
            request.fullName(), request.username(), request.email());
        NotificationContext context = notificationSender.createCompanyContext("USER_ADD", company, userDetails);
        notificationSender.notifyCompanyOperation(context);
    }

    @Transactional
    @Auditable(operation = "حذف موظف من الشركة", captureArgs = true, captureResult = true)
    public void removeUserFromCompany(Long companyId, Long userId) {
        CompanyUser companyUser = companyUserRepository.findByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not associated with this company"));
        if (!getCompanyId(companyId)) {
            throw new ResourceNotFoundException("Company authorization not found: " + companyId);
        }
        if (companyUser.getRole() == CompanyUserRole.OWNER) {
            throw new IllegalArgumentException("Cannot remove the company owner");
        }
        
        String userName = companyUser.getUser().getFullName() != null ? 
            companyUser.getUser().getFullName() : companyUser.getUser().getUsername();
        
        companyUserRepository.delete(companyUser);
        
        // ✅ ADD NOTIFICATION FOR USER REMOVED FROM COMPANY
        Company company = companyRepository.findById(companyId).orElse(null);
        if (company != null) {
            String userDetails = messageService.getMessage("notification.company.user.details",
                userName, "", "");
            NotificationContext context = notificationSender.createCompanyContext("USER_REMOVE", company, userDetails);
            notificationSender.notifyCompanyOperation(context);
        }
    }

    @Transactional
    @Auditable(operation = "تحديث صلاحيات المستخدم", captureArgs = true, captureResult = true)
    public void updateUserCompanyRole(Long companyId, Long userId, CompanyUserRole newRole) {
        CompanyUser companyUser = companyUserRepository.findByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not associated with this company"));
        if (!getCompanyId(companyId)) {
            throw new ResourceNotFoundException("Company authorization not found: " + companyId);
        }
        if (companyUser.getRole() == CompanyUserRole.OWNER) {
            throw new IllegalArgumentException("Cannot change the company owner's role");
        }
        
        String oldRoleName = companyUser.getRole() != null ? companyUser.getRole().toString() : "غير محدد";
        String newRoleName = newRole.toString();
        String userName = companyUser.getUser().getFullName() != null ? 
            companyUser.getUser().getFullName() : companyUser.getUser().getUsername();
        
        companyUser.setRole(newRole);
        companyUserRepository.save(companyUser);
        
        // ✅ ADD NOTIFICATION FOR USER ROLE UPDATED
        Company company = companyRepository.findById(companyId).orElse(null);
        if (company != null) {
            String roleDetails = notificationSender.generateUserChangeDetails(userName, oldRoleName, newRoleName);
            NotificationContext context = notificationSender.createCompanyContext("USER_ROLE_UPDATE", company, roleDetails);
            notificationSender.notifyCompanyOperation(context);
        }
    }

    public ApiResponse getCompanyUsers(
            Long companyId,
            CompanyUserSearchCriteria criteria,
            Pageable pageable
    ) {
        if (!getCompanyId(companyId)) {
            throw new ResourceNotFoundException("Company authorization not found: " + companyId);
        }
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(
                        Sort.Direction.fromString(
                                Optional.ofNullable(criteria.getSortDirection())
                                        .filter(d -> !d.isBlank())
                                        .orElse("asc")
                        ),
                        Optional.ofNullable(criteria.getSortBy())
                                .orElse("user.fullName")
                )
        );

        Specification<CompanyUser> spec = (root, query, cb) -> {
            Predicate companyPred = cb.equal(root.get("company").get("id"), companyId);

            String kw = criteria.getKeyword();
            if (kw == null || kw.isBlank()) {
                return companyPred;
            }

            kw = kw.trim();
            String likePattern = "%" + kw.toLowerCase() + "%";
            Expression<String> emailExpr = cb.lower(root.get("user").get("email"));
            Expression<String> phoneExpr = root.get("user").get("phone");
            Expression<String> nameExpr = cb.lower(root.get("user").get("fullName"));
            Predicate keywordPred;
            if (kw.matches("^[\\w\\-.]+@[\\w\\-]+\\.[A-Za-z]{2,}$")) {
                keywordPred = cb.like(emailExpr, likePattern);
            } else if (kw.matches("^\\+?\\d+$")) {
                keywordPred = cb.like(phoneExpr, "%" + kw + "%");
            } else {
                keywordPred = cb.like(nameExpr, likePattern);
            }

            return cb.and(companyPred, keywordPred);
        };
        Page<CompanyUserList> page = companyUserRepository
                .findAll(spec, sortedPageable)
                .map(CompanyUserList::fromCompanyUser);
        List<CompanyUserList> companyUserLists = page.getContent();
        return ApiResponse.builder()
                .success(true)
                .message("OK")
                .code(200)
                .date(Instant.now())
                .pagination(new Pagination(page))
                .data(companyUserLists)
                .build();
    }

    public boolean isUserInCompany(String username, Long companyId) {
        return userRepository.findByUsername(username)
                .map(user -> companyUserRepository.existsByCompanyIdAndUserId(companyId, user.getId()))
                .orElse(false);
    }

    public boolean isUserInCompanyWithRole(String username, Long companyId, CompanyUserRole role) {
        return userRepository.findByUsername(username)
                .map(user -> companyUserRepository.findByCompanyIdAndUserId(companyId, user.getId())
                        .map(cu -> cu.getRole() == role)
                        .orElse(false))
                .orElse(false);
    }

    public Optional<Company> findCompanyByUserName(String username) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    CompanyUser companyUser = companyUserRepository.findByUserId(user.getId());
                    return companyUser != null ? companyUser.getCompany() : null;
                });
    }

    @Auditable(operation = "تغير معلومات موظف ", captureArgs = true, captureResult = true)
    public void updateUserInCompany(UpdateUserInCompanyRequest req) {
        CompanyUser cu = companyUserRepository
                .findByCompanyIdAndUserId(req.companyId(), req.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User " + req.userId() + " not in company " + req.companyId()));

        if (!getCompanyId(req.companyId())) {
            throw new ResourceNotFoundException("Company authorization not found: " + req.companyId());
        }
        AppUser appUser = cu.getUser();
        
        // Track old values for notification
        String oldFullName = appUser.getFullName();
        String oldUsername = appUser.getUsername();
        String oldEmail = appUser.getEmail();
        String oldPhone = appUser.getPhone();
        CompanyUserRole oldRole = cu.getRole();
        
        // Apply updates
        req.email().ifPresent(appUser::setEmail);
        req.username().ifPresent(appUser::setUsername);
        req.fullName().ifPresent(appUser::setFullName);
        req.phone().ifPresent(appUser::setPhone);
        req.password().ifPresent(appUser::setPassword);
        authService.updateUser(appUser);
        
        StringBuilder changes = new StringBuilder();
        if (req.fullName().isPresent() && !req.fullName().get().equals(oldFullName)) {
            changes.append("• تغير الاسم من '").append(oldFullName).append("' إلى '").append(req.fullName().get()).append("'\n");
        }
        if (req.username().isPresent() && !req.username().get().equals(oldUsername)) {
            changes.append("• تغير اسم المستخدم من '").append(oldUsername).append("' إلى '").append(req.username().get()).append("'\n");
        }
        if (req.email().isPresent() && !req.email().get().equals(oldEmail)) {
            changes.append("• تغير البريد الإلكتروني من '").append(oldEmail).append("' إلى '").append(req.email().get()).append("'\n");
        }
        if (req.phone().isPresent() && !req.phone().get().equals(oldPhone)) {
            changes.append("• تغير رقم الهاتف من '").append(oldPhone).append("' إلى '").append(req.phone().get()).append("'\n");
        }
        
        req.companyUserRole().ifPresent(newRole -> {
            if (newRole != oldRole) {
                changes.append("• تغير الصلاحية من '").append(oldRole).append("' إلى '").append(newRole).append("'\n");
                cu.setRole(newRole);
                companyUserRepository.save(cu);
            }
        });
        
        // ✅ ADD NOTIFICATION FOR USER UPDATE
        if (changes.length() > 0) {
            Company company = companyRepository.findById(req.companyId()).orElse(null);
            if (company != null) {
                String userDetails = messageService.getMessage("notification.company.user.update.details",
                    req.fullName().orElse(appUser.getFullName()), changes.toString());
                NotificationContext context = notificationSender.createCompanyContext("USER_ROLE_UPDATE", company, userDetails);
                notificationSender.notifyCompanyOperation(context);
            }
        }
    }
    
    public Boolean getCompanyId(Long companyId) {
        if (companyId == helper.getCurrentCompanyId()) {
            return true;
        }
        return false;
    }
}