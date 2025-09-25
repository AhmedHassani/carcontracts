package com.ahd.backend.carcontracts.audit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id @GeneratedValue private Long id;
//    @Column(name = "event_id", insertable = false, updatable = false, nullable = false)
//    private java.util.UUID eventId;
    private Long companyId;
    private Long userId;
    private String operation;
    private String method;


    private Boolean success;
    @Lob
    private String errorMsg;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();


    @Lob
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String params;

    @Lob
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String result;
}

//
//1-check the result and param issue
//        2- make all user servises incldeu logs