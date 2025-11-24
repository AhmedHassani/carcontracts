// src/main/java/com/ahd/backend/carcontracts/template/dto/TemplateListDTO.java
package com.ahd.backend.carcontracts.template.dto;

import java.time.Instant;
import java.time.LocalDateTime;

import lombok.Value;

@Value
public class TemplateListDTO {
    Long id;
    String name;
   // String imageKey;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
