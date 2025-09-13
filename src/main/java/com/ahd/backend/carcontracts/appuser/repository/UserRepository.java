package com.ahd.backend.carcontracts.appuser.repository;


import java.util.Optional;

import com.ahd.backend.carcontracts.appuser.models.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
}
