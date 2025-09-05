package com.ahd.backend.carcontracts.notification.service;

import com.ahd.backend.carcontracts.appuser.models.AppUser;
import com.ahd.backend.carcontracts.appuser.repository.UserRepository;
import com.ahd.backend.carcontracts.notification.event.NotificationSaveEvent;
import com.ahd.backend.carcontracts.notification.event.NotificationSendEvent;
import com.ahd.backend.carcontracts.notification.model.AppNotification;
import com.ahd.backend.carcontracts.notification.model.SeenNotification;
import com.ahd.backend.carcontracts.notification.repository.NotificationRepository;
import com.ahd.backend.carcontracts.notification.repository.SeenNotificationRepository;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.util.List;
import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class NotificationListeners {

    private final NotificationRepository notificationRepository;
    private final SeenNotificationRepository seenRepo;
    private final UserRepository userRepo;


    // Save to DB only after the surrounding TX commits, and do it on a separate thread
    @Async("auditExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSave(NotificationSaveEvent event) {
        AppNotification saved = notificationRepository.save(event.notification());
        seedUnseenForAllUsers(saved.getId());
    }

    @Async("auditExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void seedUnseenForAllUsers(Long notificationId) {
        AppNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NoSuchElementException("Notification not found"));

        List<AppUser> users = userRepo.findAll();

        for (AppUser u : users) {
            boolean exists = seenRepo.existsByAppUserIdAndAppNotificationId(u.getId(), notificationId);
            if (!exists) {
                SeenNotification sn = new SeenNotification();
                sn.setAppUser(u);
                sn.setAppNotification(notification);
                sn.setNotificationSeen(false);
                seenRepo.save(sn);
            }
        }
    }
    // Send FCM after commit, async
    @Async("auditExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSend(NotificationSendEvent event) {
        try {
            Message message = Message.builder()
                    .setTopic("all_users")
                    .setNotification(
                            com.google.firebase.messaging.Notification.builder()
                                    .setTitle(event.title())
                                    .setBody(event.body())
                                    .build()
                    )
                    .build();
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Notification sent successfully: " + response);
        } catch (FirebaseMessagingException e) {
            System.err.println("Failed to send notification: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }



}
