package com.ahd.backend.carcontracts.util;


import com.ahd.backend.carcontracts.appuser.models.AppUser;
import com.ahd.backend.carcontracts.appuser.models.Role;
import com.ahd.backend.carcontracts.appuser.repository.UserRepository;
import com.ahd.backend.carcontracts.company.model.CompanyUser;
import com.ahd.backend.carcontracts.company.repository.CompanyRepository;
import com.ahd.backend.carcontracts.company.repository.CompanyUserRepository;
import com.ahd.backend.carcontracts.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RequiredArgsConstructor
@Component
public class Helper {

    private final UserRepository appUserRepository;
    private final CompanyUserRepository companyUserRepository;
    private final CompanyRepository companyRepository;

    public AppUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("auth data : "+ auth);
        return appUserRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
    }

    public long getCurrentCompanyId(){
        AppUser currentUser = this.getCurrentUser();
        LocalDate today = LocalDate.now();
        List<String> roleNames = currentUser.getRoles() == null ? List.of()
                : currentUser.getRoles().stream().map(Role::getName).toList();
      //  System.out.println("the roll of the user : " + roleNames);
        if(!roleNames.stream().anyMatch("ROLE_SUPER_ADMIN"::equals)){
            CompanyUser companyUser = companyUserRepository.findByUserId(currentUser.getId());
            boolean isCompanyActive = companyRepository.findByIdAndDeletedFalseAndExpirationDateGreaterThanEqual(companyUser.getCompany().getId(), today).isPresent();
          //  System.out.println("test 1");
            if ( !isCompanyActive) {
               // System.out.println("test 2");
                throw new ResponseStatusException(BAD_REQUEST, "Company expire or deleted");
            }
            return companyUser.getCompany().getId();

        }
        return 0 ;
    }
    public long getCurrentUserId(){
     //  System.out.println("testing");
        AppUser currentUser = this.getCurrentUser();
    //    System.out.println("Auth object: " + currentUser);
        return currentUser.getId();
    }
}