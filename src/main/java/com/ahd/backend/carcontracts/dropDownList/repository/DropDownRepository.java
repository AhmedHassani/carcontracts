package com.ahd.backend.carcontracts.dropDownList.repository;

import com.ahd.backend.carcontracts.dropDownList.model.DropDown;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DropDownRepository extends JpaRepository<DropDown, Long> {

    Optional<DropDown> findByName(String name);

    boolean existsByName(String name);
}