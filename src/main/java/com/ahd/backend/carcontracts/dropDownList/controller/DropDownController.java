package com.ahd.backend.carcontracts.dropDownList.controller;

import com.ahd.backend.carcontracts.dropDownList.dto.CreateOptionRequest;
import com.ahd.backend.carcontracts.dropDownList.dto.DropDownResponseDTO;
import com.ahd.backend.carcontracts.dropDownList.dto.OptionResponseDTO;
import com.ahd.backend.carcontracts.dropDownList.dto.UpdateOptionRequest;
import com.ahd.backend.carcontracts.dropDownList.service.DropDownService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("${application.api.base-path}/dropdowns")
public class DropDownController {

    private final DropDownService dropDownService;


    @GetMapping

    public ResponseEntity<List<DropDownResponseDTO>> getAllDropDowns() {
        List<DropDownResponseDTO> dropdowns = dropDownService.getAllDropDowns();
        return ResponseEntity.ok(dropdowns);
    }

    @GetMapping("/fetch/{dropDownId}/options/{root}/root")
    public ResponseEntity<List<OptionResponseDTO>> getOptionsByDropDownId(
            @PathVariable Long dropDownId ,
            @PathVariable Long root) {
        List<OptionResponseDTO> options = dropDownService.getOptionsByDropDownId(dropDownId , root);
        return ResponseEntity.ok(options);
    }

    @PostMapping("/options")
    @PreAuthorize("hasRole('SUPER_ADMIN') ")
    public ResponseEntity<OptionResponseDTO> createOption(
             @RequestBody CreateOptionRequest request) {
        log.debug("REST request to create option: {}", request);
        OptionResponseDTO createdOption = dropDownService.createOption(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOption);
    }

    @PutMapping("/options/{optionId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') ")
    public ResponseEntity<OptionResponseDTO> updateOption(
            @PathVariable Long optionId,
            @RequestBody UpdateOptionRequest request) {
        OptionResponseDTO updatedOption = dropDownService.updateOption(optionId, request);
        return ResponseEntity.ok(updatedOption);
    }

    @DeleteMapping("/options/{optionId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') ")
    public ResponseEntity<Void> deleteOption(
            @PathVariable Long optionId) {
        dropDownService.deleteOption(optionId);
        return ResponseEntity.noContent().build();
    }


}