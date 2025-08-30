package com.ahd.backend.carcontracts.appuser.repository;


import java.util.List;
import java.util.Optional;

import com.ahd.backend.carcontracts.appuser.models.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);
    List<Permission> findByIdIn(List<Long> ids);
    boolean existsByName(String name);
    @Query("SELECT p FROM Permission p WHERE p.isSystemOnly = false")
    List<Permission> findNonSystemPermissions();
}
