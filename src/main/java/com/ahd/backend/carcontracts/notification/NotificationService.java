package com.ahd.backend.carcontracts.notification;

import com.google.firebase.messaging.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    public void sendNotificationToDevice(String deviceToken, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Successfully sent message: " + response);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendNotificationToMultipleDevices(List<String> deviceTokens, String title, String body) {
        try {
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(deviceTokens)
                    .setNotification(Notification.builder()
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
                    .setNotification(Notification.builder()
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
}
