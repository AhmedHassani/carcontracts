package com.ahd.backend.carcontracts.notification.service;

import com.ahd.backend.carcontracts.appuser.models.AppUser;
import com.ahd.backend.carcontracts.appuser.repository.UserRepository;
import com.ahd.backend.carcontracts.notification.model.AppNotification;
import com.ahd.backend.carcontracts.notification.model.SeenNotification;
import com.ahd.backend.carcontracts.notification.repository.NotificationRepository;
import com.ahd.backend.carcontracts.notification.repository.SeenNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class SeenNotificationService {
    private final SeenNotificationRepository seenRepo;
    private final UserRepository userRepo;
    private final NotificationRepository notificationRepo;

    public void markSeenOneBySeenId(Long seenNotificationId) {
         seenRepo.markSeenById(seenNotificationId);
    }
    public void markSeenForUser(Long Userid) {
        seenRepo.markAllSeenForUser(Userid);
    }


}
