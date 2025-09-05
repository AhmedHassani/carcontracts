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
        return appUserRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
    }

    public long getCurrentCompanyId(){
        long userId = this.getCurrentUser().getId();
        CompanyUser companyUser = companyUserRepository.findByUserId(userId);
        return companyUser.getCompany().getId();
    }

}