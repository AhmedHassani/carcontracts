package com.ahd.backend.carcontracts.dashbord.controller;


import com.ahd.backend.carcontracts.contract.dto.ContractRequest;
import com.ahd.backend.carcontracts.contract.dto.ContractResponse;
import com.ahd.backend.carcontracts.contract.dto.ContractSearchCriteria;
import com.ahd.backend.carcontracts.contract.service.ContractService;
import com.ahd.backend.carcontracts.dashbord.enums.DateType;
import com.ahd.backend.carcontracts.dashbord.service.DashboardService;
import com.ahd.backend.carcontracts.payment.enums.InstallmentStatus;
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
@RequestMapping("${application.api.base-path}/dashboard")
@RequiredArgsConstructor
public class DashboardController  {

    private final DashboardService dashbordService;


    @GetMapping("")
    public ResponseEntity<Map<String, Map<String, Long>>> getDashboardStats(
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) DateType dateType) {

        Map<String, Map<String, Long>> stats = dashbordService.getStats();
        return ResponseEntity.ok(stats);
    }
    @GetMapping("/installments")
    public ResponseEntity<?> getInstallmentStats(
            @RequestParam LocalDate start,
            @RequestParam String period) {

        LocalDate end;
        switch (period.toLowerCase()) {
            case "day":
                end = start.plusDays(1); // end = start + 1 day
                Map<LocalDate, Long> daily = dashbordService.getInstallmentsDaily(start, end);
                return ResponseEntity.ok(daily);

            case "week":
                end = start.plusWeeks(1); // end = start + 1 week
                Map<LocalDate, Long> weekly = dashbordService.getInstallmentsDaily(start, end);
                return ResponseEntity.ok(weekly);

            case "month":
                end = start.plusMonths(1);
                Map<LocalDate, Long> monthly = dashbordService.getInstallmentsDaily(start, end);
                return ResponseEntity.ok(monthly);
            case "year":
                end = start.plusMonths(12);
                Map<Integer, Long> year = dashbordService.getInstallmentsMonthly(start, end);
                return ResponseEntity.ok(year);

            default:
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid period parameter. Use 'day', 'week',  'month' or 'year'.");
        }
}
    @GetMapping("/contracts")
    public ResponseEntity<?> getContractsStats(
            @RequestParam LocalDate start,
            @RequestParam String period) {

        LocalDate end;
        switch (period.toLowerCase()) {
            case "day":
                end = start.plusDays(1);
                return ResponseEntity.ok(dashbordService.getContractDaily(start, end));

            case "week":
                end = start.plusWeeks(1);
                return ResponseEntity.ok(dashbordService.getContractDaily(start, end));

            case "month":
                end = start.plusMonths(1);
                return ResponseEntity.ok(dashbordService.getContractDaily(start, end));

            case "year":
                end = start.plusYears(1);
                return ResponseEntity.ok(dashbordService.getContractMonthly(start, end));

            default:
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid period parameter. Use 'day', 'week', 'month', or 'year'.");
        }
    }

}
