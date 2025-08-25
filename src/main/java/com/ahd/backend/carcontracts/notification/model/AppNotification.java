package com.ahd.backend.carcontracts.notification.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "AppNotification")
public class AppNotification {
    @Id
    @GeneratedValue
    private Long id;
    private LocalDateTime notificationDate;
    @Nationalized
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String title;
    @Nationalized
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String body;
//    @ManyToOne
//   private Company company;
     private String permisson;

}
