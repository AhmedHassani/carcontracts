package com.ahd.backend.carcontracts.notification.service;

import com.ahd.backend.carcontracts.appuser.models.AppUser;
import com.ahd.backend.carcontracts.appuser.repository.UserRepository;
import com.ahd.backend.carcontracts.contract.dto.ContractResponse;
import com.ahd.backend.carcontracts.contract.dto.ContractSearchCriteria;
import com.ahd.backend.carcontracts.contract.mapper.ContractMapper;
import com.ahd.backend.carcontracts.contract.model.Contracts;
import com.ahd.backend.carcontracts.contract.service.ContractSpecification;
import com.ahd.backend.carcontracts.notification.dto.NotificationWithSeenDTO;
import com.ahd.backend.carcontracts.notification.event.NotificationSaveEvent;
import com.ahd.backend.carcontracts.notification.event.NotificationSendEvent;
import com.ahd.backend.carcontracts.notification.model.SeenNotification;
import com.ahd.backend.carcontracts.notification.repository.NotificationRepository;
import com.ahd.backend.carcontracts.notification.repository.SeenNotificationRepository;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import com.ahd.backend.carcontracts.notification.model.AppNotification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;


@Service
@RequiredArgsConstructor
public class NotificationService {
    private final ApplicationEventPublisher publisher;
    private final UserRepository userRepository;
    private final SeenNotificationRepository seenRepo;

    @Autowired
    private NotificationRepository notificationRepository;

    private ZoneId zone() {
        return ZoneId.systemDefault();
    }

    private LocalDateTime startOfToday() {
        return LocalDate.now(zone()).atStartOfDay();
    }

    private LocalDateTime endExclusive(LocalDateTime start, Duration length) {
        return start.plus(length);
    }

    @Transactional(readOnly = true)
    public Page<NotificationWithSeenDTO> getAllNotification(Pageable pageable) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"))
                .getId();

        return notificationRepository.findAllWithSeen(userId, pageable);
    }
    private LocalDateTime startOfWeekISO() {
        LocalDate today = LocalDate.now(zone());
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        return monday.atStartOfDay();
    }

    private LocalDateTime startOfMonth() {
        LocalDate first = LocalDate.now(zone()).with(TemporalAdjusters.firstDayOfMonth());
        return first.atStartOfDay();
    }

    private LocalDateTime startOfYear() {
        LocalDate first = LocalDate.now(zone()).with(TemporalAdjusters.firstDayOfYear());
        return first.atStartOfDay();
    }

    public List<AppNotification> getToday() {
        LocalDateTime start = startOfToday();
        LocalDateTime end   = start.plusDays(1);
        return notificationRepository.findByNotificationDateBetween(start, end);
    }

    public List<AppNotification> getThisWeek() {
        LocalDateTime start = startOfWeekISO();
        LocalDateTime end   = startOfToday();
        return notificationRepository.findByNotificationDateBetween(start, end);
    }

    public List<AppNotification> getThisMonth() {
        LocalDateTime start = startOfMonth();
        LocalDateTime end   = startOfWeekISO();
        return notificationRepository.findByNotificationDateBetween(start, end);
    }

    public List<AppNotification> getThisYear() {
        LocalDateTime start = startOfYear();
        LocalDateTime end   = startOfMonth();
        return notificationRepository.findByNotificationDateBetween(start, end);
    }


//    public void sendNotificationToDevice(String deviceToken, String title, String body) {
//        try {
//            Message message = Message.builder()
//                    .setToken(deviceToken)
//                    .setNotification(Notification.builder()
//                            .setTitle(title)
//                            .setBody(body)
//                            .build())
//                    .build();
//            String response = FirebaseMessaging.getInstance().send(message);
//            System.out.println("Successfully sent message: " + response);
//        } catch (FirebaseMessagingException e) {
//            e.printStackTrace();
//        }
//    }


//    @Async
//    public void insertNotificationAsync(AppNotification notification) {
//        AppNotification saved = notificationRepository.save(notification);
//    }

//    public void sendNotificationToDevice(String title, String body) {
//        try {
//            Message message = Message.builder()
//                    .setTopic("all_users")
//                    .setNotification(com.google.firebase.messaging.Notification.builder()
//                            .setTitle(title)
//                            .setBody(body)
//                            .build())
//                    .build();
//
//            String response = FirebaseMessaging.getInstance().send(message);
//            System.out.println("Successfully sent message: " + response);
//        } catch (FirebaseMessagingException e) {
//            e.printStackTrace();
//        }
//    }
//

    public void sendNotificationToMultipleDevices(List<String> deviceTokens, String title, String body) {
        try {
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(deviceTokens)
                    .setNotification(com.google.firebase.messaging.Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();
            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
            System.out.println("Successfully sent messages: " + response.getSuccessCount());
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendNotificationToTopic(String topic, String title, String body) {
        try {
            Message message = Message.builder()
                    .setTopic(topic)
                    .setNotification(com.google.firebase.messaging.Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Successfully sent message to topic: " + response);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }
//    public NotificationService(ApplicationEventPublisher publisher) {
//        this.publisher = publisher;
//    }

    // Called as before; now just publishes an event
    public void sendNotificationToDevice(String title, String body) {
        publisher.publishEvent(new NotificationSendEvent(title, body));
    }

    // Called as before; now just publishes an event
    public void insertNotificationAsync(AppNotification notification) {
        publisher.publishEvent(new NotificationSaveEvent(notification));
    }
}


/*
 super admin notification
 1- add  , remove , update
 2- Subscription renewal
 3- Subscription expired

 -------------------------------------
 company
 1- Subscription renewal & expired
 2- Contract (Add, delete, update)
 3- Payment (All)
 4- when update Installment
 5- when delete Installment
 7- when create Installment
 */




