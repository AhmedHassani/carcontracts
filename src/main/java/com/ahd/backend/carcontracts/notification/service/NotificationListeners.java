package com.ahd.backend.carcontracts.notification.service;

import com.ahd.backend.carcontracts.notification.event.NotificationSaveEvent;
import com.ahd.backend.carcontracts.notification.event.NotificationSendEvent;
import com.ahd.backend.carcontracts.notification.model.AppNotification;
import com.ahd.backend.carcontracts.notification.repository.NotificationRepository;
import com.google.firebase.messaging.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
public class NotificationListeners {

    private final NotificationRepository notificationRepository;

    public NotificationListeners(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // Save to DB only after the surrounding TX commits, and do it on a separate thread
    @Async("auditExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSave(NotificationSaveEvent event) {
        notificationRepository.save(event.notification());
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

            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }
}
