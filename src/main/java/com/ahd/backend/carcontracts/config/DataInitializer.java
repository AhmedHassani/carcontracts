package com.ahd.backend.carcontracts.config;


import com.ahd.backend.carcontracts.appuser.repository.PermissionRepository;
import com.ahd.backend.carcontracts.appuser.repository.RoleRepository;
import com.ahd.backend.carcontracts.appuser.repository.SecuredEndpointRepository;
import com.ahd.backend.carcontracts.appuser.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner init(
            RoleRepository roleRepo,
            PermissionRepository permRepo,
            UserRepository userRepo,
            SecuredEndpointRepository epRepo) {
        return args -> {
            // 1) Create permissions
            /*
            var pCreateUser = permRepo.save(new Permission(null, "USER_CREATE", "Create users"));
            var pReadUser   = permRepo.save(new Permission(null, "USER_READ",   "Read users"));
            // 2) Create roles
            var superAdmin = roleRepo.save(
                    new Role(null, "ROLE_SUPER_ADMIN", Set.of(pCreateUser, pReadUser))
            );
            var admin = roleRepo.save(
                    new Role(null, "ROLE_ADMIN", Set.of(pReadUser))
            );

            // 3) Create a default super-admin user
            if (userRepo.findByUsername("super").isEmpty()) {
                var enc = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
                var u = AppUser.builder()
                        .username("super")
                        .password(enc.encode("ChangeMe123!"))
                        .roles(Set.of(superAdmin))
                        .build();
                userRepo.save(u);
            }

            // 4) Secure the roles & permissions APIs themselves
            epRepo.save(new SecuredEndpoint(
                    null, "GET",    "/api/roles/**",       superAdmin
            ));
            epRepo.save(new SecuredEndpoint(
                    null, "POST",   "/api/permissions/**", superAdmin
            ));
             */
        };
    }
}
