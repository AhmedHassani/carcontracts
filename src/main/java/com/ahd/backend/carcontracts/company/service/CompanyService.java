package com.ahd.backend.carcontracts.company.service;
import com.ahd.backend.carcontracts.appuser.services.RolePermissionService;
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
import com.ahd.backend.carcontracts.notification.model.AppNotification;
import com.ahd.backend.carcontracts.util.base.ApiResponse;
import com.ahd.backend.carcontracts.util.base.Pagination;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import com.ahd.backend.carcontracts.notification.service.NotificationService;


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
    private final NotificationService notificationService;
    private final RolePermissionService rolePermissionService;
    private final Helper helper;
    private final EntityManager em;
    public CompanyResponse createCompany(CompanyRequest request) {
        //log.info("Creating company: {}", request.companyName());
        Role companyRole = roleRepository.findByName("ROLE_COMPANY")
                .orElseThrow(() -> new ResourceNotFoundException("Company role not found"));
        var createUser = toCreateUserRequest(request,companyRole);
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
        notificationService.sendNotificationToDevice(
                "إضافة شركة جديدة",
                "تم إضافة شركة " + savedCompany.getCompanyName() + " بنجاح"
        );
        AppNotification notif = new AppNotification();
        notif.setTitle("إضافة شركة جديدة");
        notif.setBody("تم إضافة شركة " + savedCompany.getCompanyName() + " بنجاح");
        notif.setNotificationDate(LocalDateTime.now());
        notif.setPermisson("ADMIN");
        notificationService.insertNotificationAsync(notif);
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
                .findAll(spec,pageable).map(this::mapToDto);
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
        notificationService.sendNotificationToDevice(
                "التعديل معلومات الشركة",
                "لقد تغير معلومات شركة" + company.getCompanyName() + "بنجاح "
        );
        AppNotification notif = new AppNotification();
        notif.setTitle("لتعديل معلومات الشركة");
        notif.setBody("لقد تغير معلومات شركة" + company.getCompanyName() + " بنجاح");
        notif.setNotificationDate(LocalDateTime.now());
      //  notif.setCompany(company);
        notif.setPermisson("ADMIN");
        notificationService.insertNotificationAsync(notif);

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
        notificationService.sendNotificationToDevice(
                "تغير تاريخ نفاذ الصلاحية",
                "تم تغير تاريخ انتهاء صلاحية شركة " + company.getCompanyName()
        );
        AppNotification notif = new AppNotification();
        notif.setTitle("تغير تاريخ نفاذ الصلاحية");
        notif.setBody("تم تغير تاريخ انتهاء صلاحية شركة " + company.getCompanyName() );
        notif.setNotificationDate(LocalDateTime.now());
        //  notif.setCompany(company);
        notif.setPermisson("ADMIN");
        notificationService.insertNotificationAsync(notif);

        return true;
    }

    @Transactional
    public ApiResponse<Void> deleteCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + id));
        companyRepository.delete(company);
        notificationService.sendNotificationToDevice(
                "حذف شركة",
                "تم حذف شركة " + company.getCompanyName() + " بنجاح"
        );
        AppNotification notif = new AppNotification();
        notif.setTitle("حذف شركة");
        notif.setBody("تم حذف شركة " + company.getCompanyName() + " بنجاح");
        notif.setNotificationDate(LocalDateTime.now());
     //   notif.setCompany(company);
        notif.setPermisson("ADMIN");
        notificationService.insertNotificationAsync(notif);

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
            company.setStatus(CompanyStatus.EXPIRED);
            companyRepository.save(company);

            notificationService.sendNotificationToDevice(
                    "انتهاء صلاحية ",
                    "لقد نفذت صلاحية شركة" + company.getCompanyName()
            );
        }
    }
    /* ----------------------------------------------------
     * Add a user to a company
     * -------------------------------------------------- */
    @Transactional
    public void addUserToCompany(AddUserToCompanyRequest request) {

        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + request.companyId()));
        if(!getCompanyId(request.companyId())){
            throw new ResourceNotFoundException("Company authorization not found: " + request.companyId());
        }
        long currentUserCount = companyUserRepository.countByCompanyId(request.companyId());
        if (currentUserCount >= company.getUserCount()) {
            throw new ConflictException("The company has reached its maximum allowed users.");
        }
        Role companyRole = roleRepository.findByName("ROLE_STAFF")
                .orElseThrow(() -> new ResourceNotFoundException("Company role not found"));
        var userInfo = CreateUserRequest.builder()
                .email(request.email())
                .password(request.password())
                .username(request.username())
                .fullName(request.fullName())
                .phone(request.phone())
                .roleIds(Set.of(companyRole.getId()))
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
    }

    @Transactional
    public void removeUserFromCompany(Long companyId, Long userId) {
        CompanyUser companyUser = companyUserRepository.findByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not associated with this company"));
        if(!getCompanyId(companyId)){
            throw new ResourceNotFoundException("Company authorization not found: " + companyId);
        }
        if (companyUser.getRole() == CompanyUserRole.OWNER) {
            throw new IllegalArgumentException("Cannot remove the company owner");
        }
        companyUserRepository.delete(companyUser);
    }


    @Transactional
    public void updateUserCompanyRole(Long companyId, Long userId, CompanyUserRole newRole) {
        CompanyUser companyUser = companyUserRepository.findByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not associated with this company"));
        if(!getCompanyId(companyId)){
            throw new ResourceNotFoundException("Company authorization not found: " + companyId);
        }
        if (companyUser.getRole() == CompanyUserRole.OWNER) {
            throw new IllegalArgumentException("Cannot change the company owner's role");
        }
        companyUser.setRole(newRole);
        companyUserRepository.save(companyUser);
    }


    public ApiResponse getCompanyUsers(
            Long companyId,
            CompanyUserSearchCriteria criteria,
            Pageable pageable
    ) {
        if(!getCompanyId(companyId)){

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

        // 2) Build the Specification
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
            Expression<String> nameExpr  = cb.lower(root.get("user").get("fullName"));
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


    public void updateUserInCompany(UpdateUserInCompanyRequest req) {
        CompanyUser cu = companyUserRepository
                .findByCompanyIdAndUserId(req.companyId(), req.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User " + req.userId() + " not in company " + req.companyId()));

        if(!getCompanyId(req.companyId())){
            throw new ResourceNotFoundException("Company authorization not found: " + req.companyId());
        }
        AppUser appUser = cu.getUser();
        req.email().ifPresent(appUser::setEmail);
        req.username().ifPresent(appUser::setUsername);
        req.fullName().ifPresent(appUser::setFullName);
        req.phone().ifPresent(appUser::setPhone);
        req.password().ifPresent(appUser::setPassword);
        authService.updateUser(appUser);
        req.companyUserRole().ifPresent(newRole -> {
            cu.setRole(newRole);
            companyUserRepository.save(cu);
        });
    }
    public Boolean getCompanyId (Long companyId){
        if(companyId == helper.getCurrentCompanyId() ){
            return true;
        }
        return false ;
    }
}
