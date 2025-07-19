package com.ahd.backend.carcontracts.config;

import com.ahd.backend.carcontracts.appuser.models.AppUser;
import com.ahd.backend.carcontracts.appuser.models.Permission;
import com.ahd.backend.carcontracts.appuser.models.Role;
import com.ahd.backend.carcontracts.appuser.models.SecuredEndpoint;
import com.ahd.backend.carcontracts.appuser.repository.PermissionRepository;
import com.ahd.backend.carcontracts.appuser.repository.RoleRepository;
import com.ahd.backend.carcontracts.appuser.repository.SecuredEndpointRepository;
import com.ahd.backend.carcontracts.appuser.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner init(
            RoleRepository roleRepo,
            PermissionRepository permRepo,
            UserRepository userRepo,
            SecuredEndpointRepository epRepo) {
        return args -> {

            // 3) Create a default super-admin user
            if (userRepo.findByUsername("super1").isEmpty()) {
                var superAdmin  = roleRepo.findByName("ROLE_SUPER_ADMIN").get();
                var enc = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
                var u = AppUser.builder()
                        .username("super1")
                        .password(enc.encode("Pass1@ssss"))
                        .phone("964783284383")
                        .fullName("super admin")
                        .email("superadmin@gmail.com")
                        .username("super1")
                        .roles(Set.of(superAdmin))
                        .build();
                userRepo.save(u);
            }

        };
    }
}
