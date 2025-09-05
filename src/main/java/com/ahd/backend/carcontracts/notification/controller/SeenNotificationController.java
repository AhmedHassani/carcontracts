package com.ahd.backend.carcontracts.notification.controller;

import com.ahd.backend.carcontracts.notification.service.SeenNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("${application.api.base-path}/notifications/seen")
public class SeenNotificationController {

    private final SeenNotificationService service;

    @PostMapping("/{seenId}")
    public ResponseEntity<?> markOneSeen(@PathVariable Long seenId) {
        service.markSeenOneBySeenId(seenId);
        return ResponseEntity.ok(Map.of("seenId", seenId, "status", "seen"));
    }

    @PostMapping("/users/{userId}/all")
    public ResponseEntity<?> markAllForUser(@PathVariable Long userId) {
        service.markSeenForUser(userId);
        return ResponseEntity.ok(Map.of("userId", userId, "status", "all seen"));
    }

}
