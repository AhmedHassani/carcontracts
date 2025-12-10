package com.ahd.backend.carcontracts.dropDownList.repository;

import com.ahd.backend.carcontracts.dropDownList.model.OptionDropDown;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OptionDropDownRepository extends JpaRepository<OptionDropDown, Long> {
    List<OptionDropDown> findBydropDownId(Long dropDownId);
    Optional<OptionDropDown> findByIdAndDropDownId(Long id, Long dropDownId);
}