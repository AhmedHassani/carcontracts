package com.ahd.backend.carcontracts.audit;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id @GeneratedValue private Long id;
    private Long companyId;
    private Long userId;
    private String operation;
    private String method;

    @Lob
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String params;

    @Lob
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String result;

    private Boolean success;
    @Lob
    private String errorMsg;
    private LocalDateTime timestamp = LocalDateTime.now();
}
