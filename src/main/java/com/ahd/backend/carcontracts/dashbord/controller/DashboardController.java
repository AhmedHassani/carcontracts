package com.ahd.backend.carcontracts.dashbord.controller;


import com.ahd.backend.carcontracts.dashbord.enums.DateType;
import com.ahd.backend.carcontracts.dashbord.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("${application.api.base-path}/dashboard")
@RequiredArgsConstructor
public class DashboardController  {

    private final DashboardService service;


    @GetMapping
    public ResponseEntity<Map<String, Long>> getDashboardStats(
            @RequestParam LocalDate start,
            @RequestParam DateType dateType) {

        Map<String, Long> stats = service.getStats(start, dateType);
        return ResponseEntity.ok(stats);
    }

}
