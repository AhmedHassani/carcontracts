package com.ahd.backend.carcontracts.Notification;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
public class NotificationRequestDTO {
    private String message;
    private String link;
    private String firebaseToken;
}
