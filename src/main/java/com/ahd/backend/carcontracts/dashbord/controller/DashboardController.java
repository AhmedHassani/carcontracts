package com.ahd.backend.carcontracts.dashbord.controller;


import com.ahd.backend.carcontracts.contract.dto.ContractRequest;
import com.ahd.backend.carcontracts.contract.dto.ContractResponse;
import com.ahd.backend.carcontracts.contract.dto.ContractSearchCriteria;
import com.ahd.backend.carcontracts.contract.service.ContractService;
import com.ahd.backend.carcontracts.dashbord.enums.DateType;
import com.ahd.backend.carcontracts.dashbord.service.DashbordService;
import com.ahd.backend.carcontracts.util.base.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${application.api.base-path}/Dashbord")
@RequiredArgsConstructor
public class DashboardController  {

    private final DashbordService dashbordService;


    @GetMapping("")
    public ResponseEntity<Map<String, Long>> getDashboardStats(
            @RequestParam LocalDate start,
            @RequestParam DateType dateType) {

        Map<String, Long> stats = dashbordService.getStats(start, dateType);
        return ResponseEntity.ok(stats);
    }

}
