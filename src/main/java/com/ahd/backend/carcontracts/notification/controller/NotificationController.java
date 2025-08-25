package com.ahd.backend.carcontracts.notification.controller;

import com.ahd.backend.carcontracts.notification.model.AppNotification;
import com.ahd.backend.carcontracts.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("${application.api.base-path}/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @GetMapping("/today")
    public List<AppNotification> today() {
        return service.getToday();
    }

    @GetMapping("/week")
    public List<AppNotification> week() {
        return service.getThisWeek();
    }

    @GetMapping("/month")
    public List<AppNotification> month() {
        return service.getThisMonth();
    }

    @GetMapping("/year")
    public List<AppNotification> year() {
        return service.getThisYear();
    }

}
