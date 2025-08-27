package com.ahd.backend.carcontracts.notification.controller;

import com.ahd.backend.carcontracts.contract.dto.ContractResponse;
import com.ahd.backend.carcontracts.contract.dto.ContractSearchCriteria;
import com.ahd.backend.carcontracts.notification.model.AppNotification;
import com.ahd.backend.carcontracts.notification.service.NotificationService;
import com.ahd.backend.carcontracts.util.base.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("${application.api.base-path}/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

//    @GetMapping("/today")
//    public List<AppNotification> today() {
//        return service.getToday();
//    }
//
//    @GetMapping("/week")
//    public List<AppNotification> week() {
//        return service.getThisWeek();
//    }
//
//    @GetMapping("/month")
//    public List<AppNotification> month() {
//        return service.getThisMonth();
//    }
//
//    @GetMapping("/year")
//    public List<AppNotification> year() {
//        return service.getThisYear();
//    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AppNotification>>> getAllNotification(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<AppNotification> notification = service.getAllNotification( pageable);
        return ResponseEntity.ok(ApiResponse.success(notification));
    }
}
