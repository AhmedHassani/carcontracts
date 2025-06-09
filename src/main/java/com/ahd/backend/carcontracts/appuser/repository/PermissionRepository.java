package com.ahd.backend.carcontracts.appuser.repository;


import java.util.Optional;

import com.ahd.backend.carcontracts.appuser.models.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);
}
