package com.ahd.backend.carcontracts.appuser.repository;


import java.util.Optional;
import java.util.List;
import java.util.Set;

import com.ahd.backend.carcontracts.appuser.models.Role;
import com.ahd.backend.carcontracts.appuser.models.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    boolean existsByName(String name);
    List<Role> findByRoleType(RoleType roleType);
    @Query("SELECT r FROM Role r WHERE r.company.id = :companyId")
    List<Role> findByCompanyId(@Param("companyId") Long companyId);
    @Query("SELECT r FROM Role r WHERE r.company.id IN :companyIds")
    List<Role> findByCompanyIdIn(@Param("companyIds") Set<Long> companyIds);
    @Query("SELECT r FROM Role r WHERE r.company IS NULL")
    List<Role> findSystemRoles();
}
