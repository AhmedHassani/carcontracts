package com.ahd.backend.carcontracts.appuser.repository;


import java.util.Optional;

import com.ahd.backend.carcontracts.appuser.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
