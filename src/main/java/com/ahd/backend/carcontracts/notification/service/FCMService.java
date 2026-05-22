package com.ahd.backend.carcontracts.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FCMService {

    private final FirebaseMessaging firebaseMessaging;

    public void sendNotification(String token, String title, String body) {
       //log.info("📱 FCM Service: Starting to send notification");
       //log.info("📱 Token (first 30 chars): {}", token != null ? token.substring(0, Math.min(token.length(), 30)) + "..." : "null");
       //log.info("📱 Title: {}", title);
       //log.info("📱 Body: {}", body);
        
        if (token == null || token.isEmpty()) {
           //log.warn("⚠️ Cannot send FCM notification: Token is empty");
            return;
        }
        
        try {
           //log.info("🔨 Building FCM notification message...");
            
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();
            
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(notification)
                    .putData("click_action", "FLUTTER_NOTIFICATION_CLICK")
                    .putData("title", title)
                    .putData("body", body)
                    .putData("timestamp", String.valueOf(System.currentTimeMillis()))
                    .build();
            
           //log.info("📤 Sending message to FCM...");
            String response = firebaseMessaging.send(message);
            
           //log.info("✅✅✅ SUCCESS! FCM message sent! ✅✅✅");
           //log.info("📨 Response: {}", response);
           //log.info("📨 Message ID: {}", response);
            
        } catch (Exception e) {
           //log.error("❌❌❌ FAILED to send FCM message! ❌❌❌");
           //log.error("❌ Error: {}", e.getMessage());
           //log.error("❌ Error type: {}", e.getClass().getSimpleName());
            
            // Check for specific error types
            if (e.getMessage().contains("Invalid JWT Signature")) {
               //log.error("🔑 JWT Signature error - Service account key may be invalid");
            } else if (e.getMessage().contains("Unregistered")) {
               //log.error("📱 Token is not registered - Device may have uninstalled the app");
            } else if (e.getMessage().contains("MismatchSenderId")) {
               //log.error("🏢 Sender ID mismatch - Check Firebase project configuration");
            }
            
           //log.error("❌ Full error details:", e);
        }
    }
}