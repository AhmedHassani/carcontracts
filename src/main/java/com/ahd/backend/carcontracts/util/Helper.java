package com.ahd.backend.carcontracts.util;


import com.ahd.backend.carcontracts.appuser.models.AppUser;
import com.ahd.backend.carcontracts.appuser.repository.UserRepository;
import com.ahd.backend.carcontracts.company.model.CompanyUser;
import com.ahd.backend.carcontracts.company.repository.CompanyUserRepository;
import com.ahd.backend.carcontracts.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class Helper {

    private final UserRepository appUserRepository;
    private final CompanyUserRepository companyUserRepository;

    public AppUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("auth "+ auth);
        return appUserRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
    }

    public long getCurrentCompanyId(){
        AppUser currentUser = this.getCurrentUser();
        System.out.println("Auth object: " + currentUser);
        CompanyUser companyUser = companyUserRepository.findByUserId(currentUser.getId());
        System.out.println("the company id : "+ companyUser.getCompany().getId());
        return companyUser.getCompany().getId();
    }
    public long getCurrentUserId(){
        System.out.println("testing");
        AppUser currentUser = this.getCurrentUser();
        System.out.println("Auth object: " + currentUser);
        if (currentUser != null) {
            System.out.println("Auth name: " + currentUser.getFullName());
        }
        System.out.println("the user id : "+ currentUser.getId());
        return currentUser.getId();
    }
}