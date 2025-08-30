package com.ahd.backend.carcontracts.notification.controller;

import com.ahd.backend.carcontracts.notification.model.AppNotification;
import com.ahd.backend.carcontracts.notification.service.NotificationService;
import com.ahd.backend.carcontracts.util.Messages.*;
import com.ahd.backend.carcontracts.util.base.ApiResponse;
import com.google.firebase.FirebaseApp;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ahd.backend.carcontracts.util.Messages.tr;


@RestController
@RequestMapping("${application.api.base-path}/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @GetMapping
    @PreAuthorize("hasAuthority('GET_NOTIFICATTIONS') or hasRole('SUPER_ADMIN')")
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


    @GetMapping("/test")
    @PreAuthorize("hasAuthority('GET_NOTIFICATTIONS')")
    public ResponseEntity<Map<String, Object>> testFirebase() {
        Map<String, Object> response = new HashMap<>();
        try {
            FirebaseApp app = FirebaseApp.getInstance();
            response.put("status", "success");
            response.put("appName", app.getName());
            response.put("projectId",tr("hello1.tr"));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
