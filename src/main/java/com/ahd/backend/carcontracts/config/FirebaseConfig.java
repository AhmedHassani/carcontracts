package com.ahd.backend.carcontracts.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.Arrays;

@Slf4j
@Configuration
@EnableAsync
public class FirebaseConfig {

    @Value("${firebase.config.file:carcontact-aeaed-firebase-adminsdk-fbsvc-6bdb9533cc.json}")
    private String firebaseConfigPath;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                //log.info("🔥 Initializing Firebase with config file: {}", firebaseConfigPath);
                // Check if file exists
                ClassPathResource resource = new ClassPathResource(firebaseConfigPath);
                if (!resource.exists()) {
                    throw new RuntimeException("❌ Firebase service account file not found: " + firebaseConfigPath);
                }
                // Load credentials with proper scopes
                try (InputStream serviceAccount = resource.getInputStream()) {
                    GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount)
                            .createScoped(Arrays.asList(
                                    "https://www.googleapis.com/auth/firebase.messaging",
                                    "https://www.googleapis.com/auth/firebase.database",
                                    "https://www.googleapis.com/auth/userinfo.email",
                                    "https://www.googleapis.com/auth/cloud-platform"
                            ));
                    //log.info("🔐 Firebase credentials loaded with proper scopes");
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(credentials)
                            .setProjectId("carcontact-aeaed")
                            .build();
                    FirebaseApp app = FirebaseApp.initializeApp(options);
                    //log.info("✅ Firebase FCM initialized successfully");
                    //log.info("🏗️ Project ID: {}", app.getOptions().getProjectId());
                } catch (Exception e) {
                    //log.error("❌ Failed to initialize Firebase: {}", e.getMessage());
                    throw new RuntimeException("Invalid Firebase service account file. Please regenerate the key.", e);
                }

            } else {
                //log.info("🔥 Firebase already initialized");
            }
        } catch (Exception e) {
            //log.error("💥 Failed to initialize Firebase: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        try {
            FirebaseMessaging messaging = FirebaseMessaging.getInstance();
            //log.info("📨 FirebaseMessaging bean created successfully");
            return messaging;
        } catch (Exception e) {
            //log.error("❌ Failed to create FirebaseMessaging bean: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create FirebaseMessaging bean", e);
        }
    }
}